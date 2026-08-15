#!/usr/bin/env python3
"""Decode Aion 8.6 SERVER->CLIENT packets from a pcapng game stream.

Mirrors EncryptionKeyPair.encrypt() inverse. The server key starts at the base key
(derived from the raw SM_KEY packet, which is the first server packet and is NOT
encrypted / does NOT advance the key) and advances by each encrypted packet's size.

Usage:
  python decode_server.py <pcapng>                 # server opcode histogram
  python decode_server.py <pcapng> find <hexpat>   # dump server packets whose body contains hexpat
  python decode_server.py <pcapng> op 0xNNNN        # dump server packets with given raw opcode word
"""
import re, struct, sys, subprocess

TSHARK = r"C:\Program Files\Wireshark\tshark.exe"
STATIC_KEY = b"nKO/WctQ0AVLbpzfBkS6NevDYT8ourG5CRlmdjyJ72aswx4EPq1UgZhFMXH?3iI9"

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
        (server if indented else client).extend(data)
    return bytes(server), bytes(client)

def split_packets(stream):
    pkts=[]; i=0; n=len(stream)
    while i+2<=n:
        ln = stream[i] | (stream[i+1]<<8)
        if ln<2 or i+ln>n: break
        pkts.append(stream[i:i+ln]); i+=ln
    return pkts

def decrypt_xor(pkt, key):
    """Generic XOR decrypt (identical for client & server, differs only by which key)."""
    body = bytearray(pkt[2:])
    size = len(body)
    if size == 0:
        return bytes(pkt), key
    prev = body[0]
    body[0] ^= (key[0] & 0xFF)
    for i in range(1,size):
        curr = body[i] & 0xFF
        body[i] ^= (STATIC_KEY[i&63]&0xFF) ^ (key[i&7]&0xFF) ^ prev
        prev = curr
    nk = u64_bytes((key_u64(key)+size)&0xFFFFFFFFFFFFFFFF)
    return bytes(pkt[:2]+bytes(body)), nk

def game_streams(path):
    streams = subprocess.run([TSHARK,"-r",path,"-T","fields","-e","tcp.stream","-e","tcp.dstport","-e","tcp.srcport"],
                             capture_output=True,text=True,errors="replace").stdout
    gs=set()
    for ln in streams.splitlines():
        p=ln.split('\t')
        if len(p)>=3 and ('7777' in p[1:] or '13001' in p[1:] or '13000' in p[1:]):
            gs.add(p[0])
    return sorted(gs, key=lambda s:int(s) if s.isdigit() else 0)

def decode_server_packets(path):
    """Yield (stream_id, index, plain_pkt) for every decrypted server packet."""
    for sid in game_streams(path):
        server,_ = load_stream(path, sid)
        spkts = split_packets(server)
        if len(spkts) < 2 or len(spkts[0]) < 11:
            continue
        false_key = struct.unpack('<I', spkts[0][7:11])[0]
        key = base_keybytes(false_key_to_base(false_key))
        # spkts[0] is the raw SM_KEY (unencrypted, does not advance key). Start at spkts[1].
        for idx, p in enumerate(spkts[1:], start=1):
            plain, key = decrypt_xor(p, key)
            yield sid, idx, plain

def raw_op(plain):
    if len(plain) < 4: return None
    return plain[2] | (plain[3]<<8)

def main():
    path = sys.argv[1]
    mode = sys.argv[2] if len(sys.argv) > 2 else "hist"
    if mode == "find":
        pat = bytes.fromhex(sys.argv[3])
        for sid, idx, plain in decode_server_packets(path):
            if pat in plain:
                print(f"[stream {sid} #{idx}] len={len(plain)} op=0x{(raw_op(plain) or 0):04X} body={plain[2:].hex()}")
    elif mode == "op":
        tgt = int(sys.argv[3],16)
        for sid, idx, plain in decode_server_packets(path):
            if raw_op(plain) == tgt:
                print(f"[stream {sid} #{idx}] len={len(plain)} body={plain[2:].hex()}")
    else:
        hist={}
        total=0
        for sid, idx, plain in decode_server_packets(path):
            op = raw_op(plain)
            if op is not None:
                hist[op]=hist.get(op,0)+1; total+=1
        print(f"=== server packets decoded: {total} ; distinct raw opcodes: {len(hist)} ===")
        for op,c in sorted(hist.items(), key=lambda x:-x[1])[:50]:
            print(f"0x{op:04X}: {c}")

if __name__=="__main__":
    main()
