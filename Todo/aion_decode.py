#!/usr/bin/env python3
"""Rebuilt Aion 8.6 packet decoder: decrypts CLIENT->SERVER packets from a pcapng
game stream and reports the wire opcodes seen. Mirrors EncryptionKeyPair.decrypt()."""
import re, struct, sys, subprocess

TSHARK = r"C:\Program Files\Wireshark\tshark.exe"
STATIC_KEY = b"nKO/WctQ0AVLbpzfBkS6NevDYT8ourG5CRlmdjyJ72aswx4EPq1UgZhFMXH?3iI9"
STATIC_CLIENT_CODE = 0x75

def false_key_to_base(false_key):
    fk = false_key & 0xFFFFFFFF
    return ((fk - 0x3FF2CCDF) & 0xFFFFFFFF ^ 0xCD92E4D9) & 0xFFFFFFFF

def base_keybytes(base):
    b = base & 0xFFFFFFFF
    return bytearray([b & 0xFF,(b>>8)&0xFF,(b>>16)&0xFF,(b>>24)&0xFF,0xa1,0x6c,0x54,0x87])

def key_u64(k):
    v=0
    for i in range(8): v |= (k[i]&0xFF)<<(8*i)
    return v & 0xFFFFFFFFFFFFFFFF

def u64_bytes(v):
    v&=0xFFFFFFFFFFFFFFFF
    return bytearray([(v>>(8*i))&0xFF for i in range(8)])

def load_stream(path, stream_id):
    out = subprocess.run([TSHARK,"-r",path,"-q","-z",f"follow,tcp,hex,{stream_id}"],
                         capture_output=True,text=True,errors="replace").stdout
    server=bytearray(); client=bytearray()
    for line in out.splitlines():
        m = re.match(r'^(\t?)([0-9A-Fa-f]{8})  ', line)
        if not m: continue
        indented = bool(m.group(1))
        hexpart = line[len(m.group(1))+10:len(m.group(1))+10+48]
        data = bytes(int(h,16) for h in re.findall(r'[0-9A-Fa-f]{2}', hexpart))
        # tab-indented = server->client (first hello is tab-indented in our streams)
        (server if indented else client).extend(data)
    return bytes(server), bytes(client)

def split_packets(stream):
    pkts=[]; i=0; n=len(stream)
    while i+2<=n:
        ln = stream[i] | (stream[i+1]<<8)
        if ln<2 or i+ln>n: break
        pkts.append(stream[i:i+ln]); i+=ln
    return pkts

def decrypt_client(pkt, key):
    """pkt = full on-wire client packet (2-byte len prefix + encrypted body). Server reads the
    length first, then decrypts the body from offset 2. Returns (plain_full, ok, newkey)."""
    body = bytearray(pkt[2:])
    size = len(body)
    if size == 0:
        return bytes(pkt), False, key
    prev = body[0]
    body[0] ^= (key[0] & 0xFF)
    for i in range(1,size):
        curr = body[i] & 0xFF
        body[i] ^= (STATIC_KEY[i&63]&0xFF) ^ (key[i&7]&0xFF) ^ prev
        prev = curr
    # validateClientPacket: getShort(0)==~getShort(3) && get(2)==0x75 (offsets within the body)
    ok = (size>=5 and (body[0]|(body[1]<<8)) == ((~(body[3]|(body[4]<<8)))&0xFFFF) and body[2]==STATIC_CLIENT_CODE)
    nk = u64_bytes((key_u64(key)+size)&0xFFFFFFFFFFFFFFFF) if ok else key
    return bytes(pkt[:2]+bytes(body)), ok, nk

def main():
    path = sys.argv[1]
    # find game stream (port 7777 or 13001)
    streams = subprocess.run([TSHARK,"-r",path,"-T","fields","-e","tcp.stream","-e","tcp.dstport","-e","tcp.srcport"],
                             capture_output=True,text=True,errors="replace").stdout
    game_streams=set()
    for ln in streams.splitlines():
        parts=ln.split('\t')
        if len(parts)>=3 and ('7777' in parts[1:] or '13001' in parts[1:]):
            game_streams.add(parts[0])
    opcodes={}
    for sid in sorted(game_streams, key=lambda s:int(s) if s.isdigit() else 0):
        server,client = load_stream(path, sid)
        spkts = split_packets(server)
        if not spkts: continue
        first = spkts[0]
        if len(first)<11: continue
        false_key = struct.unpack('<I', first[7:11])[0]
        key = base_keybytes(false_key_to_base(false_key))
        cpkts = split_packets(client)
        good=0
        for p in cpkts:
            plain,ok,key = decrypt_client(p, key)
            if ok and len(plain)>=4:
                # plain = [len(2)][opcode(2)][0x75][~opcode(2)]...  opcode at offset 2-3
                op = plain[2] | (plain[3]<<8)
                opcodes[op]=opcodes.get(op,0)+1
                good+=1
        print(f"stream {sid}: client pkts={len(cpkts)} decoded_ok={good}", file=sys.stderr)
    print("=== client wire opcodes seen (opcode_hex: count) ===")
    for op,c in sorted(opcodes.items(), key=lambda x:-x[1])[:40]:
        print(f"0x{op:04X}: {c}")

if __name__=="__main__":
    main()

def dump_opcode(path, target_op):
    streams = subprocess.run([TSHARK,"-r",path,"-T","fields","-e","tcp.stream","-e","tcp.dstport","-e","tcp.srcport"],
                             capture_output=True,text=True,errors="replace").stdout
    gs=set()
    for ln in streams.splitlines():
        p=ln.split('\t')
        if len(p)>=3 and ('7777' in p[1:] or '13001' in p[1:]): gs.add(p[0])
    found=[]
    for sid in sorted(gs, key=lambda s:int(s) if s.isdigit() else 0):
        server,client = load_stream(path, sid)
        spkts=split_packets(server)
        if not spkts or len(spkts[0])<11: continue
        key=base_keybytes(false_key_to_base(struct.unpack('<I',spkts[0][7:11])[0]))
        for p in split_packets(client):
            plain,ok,key = decrypt_client(p,key)
            if ok and len(plain)>=4:
                op=plain[2]|(plain[3]<<8)
                if op==target_op:
                    body=plain[7:]  # skip len(2)+opcode(2)+0x75(1)+~op(2)
                    found.append(body.hex())
    return found

if __name__=="__main__" and len(sys.argv)>=3 and sys.argv[2].startswith("0x"):
    tgt=int(sys.argv[2],16)
    for i,h in enumerate(dump_opcode(sys.argv[1],tgt)):
        print(f"[{i}] {h}")
    sys.exit(0)
