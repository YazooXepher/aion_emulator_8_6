#!/usr/bin/env python3
"""Timestamp-aware Aion decoder: reassembles a game TCP stream per direction while tracking each
byte's frame time, decrypts both directions, splits into packets, and prints a merged timeline of
client (C) and server (S) packets with opcodes + times. Lets us find the server packet that follows
a deliberate client action (e.g. opening the Daeva Pass after an 8s pause)."""
import re, struct, sys, subprocess

TSHARK = r"C:\Program Files\Wireshark\tshark.exe"
STATIC_KEY = b"nKO/WctQ0AVLbpzfBkS6NevDYT8ourG5CRlmdjyJ72aswx4EPq1UgZhFMXH?3iI9"
CLIENT_CODE = 0x75

def fkb(fk): return ((fk & 0xFFFFFFFF) - 0x3FF2CCDF & 0xFFFFFFFF ^ 0xCD92E4D9) & 0xFFFFFFFF
def basekey(b):
    b &= 0xFFFFFFFF
    return bytearray([b&0xFF,(b>>8)&0xFF,(b>>16)&0xFF,(b>>24)&0xFF,0xa1,0x6c,0x54,0x87])
def ku(k):
    v=0
    for i in range(8): v|=(k[i]&0xFF)<<(8*i)
    return v & 0xFFFFFFFFFFFFFFFF
def ub(v):
    v&=0xFFFFFFFFFFFFFFFF
    return bytearray([(v>>(8*i))&0xFF for i in range(8)])

def load_dir(path, stream, want_srcport):
    """Return (bytes, times[]) for one direction, reassembled by TCP seq (sorted + deduped)."""
    out = subprocess.run([TSHARK,"-r",path,"-Y",f"tcp.stream=={stream}",
        "-T","fields","-e","frame.time_relative","-e","tcp.srcport","-e","tcp.seq","-e","tcp.payload"],
        capture_output=True,text=True,errors="replace").stdout
    segs={}  # seq -> (time, bytes)  (dedupe retransmits, keep first time)
    for ln in out.splitlines():
        f=ln.split('\t')
        if len(f)<4 or not f[3]: continue
        t=float(f[0]); sp=f[1]; seq=int(f[2]); payload=f[3].replace(':','')
        if sp!=str(want_srcport): continue
        b=bytes.fromhex(payload)
        if seq not in segs or len(b)>len(segs[seq][1]):
            segs[seq]=(t,b)
    data=bytearray(); times=[]
    for seq in sorted(segs):
        t,b=segs[seq]
        data.extend(b); times.extend([t]*len(b))
    return bytes(data), times

def split_with_time(stream, times):
    pkts=[]; i=0; n=len(stream)
    while i+2<=n:
        ln=stream[i]|(stream[i+1]<<8)
        if ln<2 or i+ln>n: break
        pkts.append((stream[i:i+ln], times[i] if i<len(times) else -1)); i+=ln
    return pkts

def dec(pkt, key):
    body=bytearray(pkt[2:]); size=len(body)
    if size==0: return bytes(pkt), key, False
    prev=body[0]; body[0]^=key[0]&0xFF
    for i in range(1,size):
        c=body[i]&0xFF; body[i]^=(STATIC_KEY[i&63]&0xFF)^(key[i&7]&0xFF)^prev; prev=c
    ok = size>=5 and (body[0]|(body[1]<<8))==((~(body[3]|(body[4]<<8)))&0xFFFF) and body[2]==CLIENT_CODE
    nk=ub((ku(key)+size)&0xFFFFFFFFFFFFFFFF)
    return bytes(pkt[:2]+bytes(body)), nk, ok

def dsc(w): return ((w^0xD9)-0xD8)&0xFFFF

def main():
    path, stream, port = sys.argv[1], sys.argv[2], sys.argv[3]
    tmin = float(sys.argv[4]) if len(sys.argv)>4 else 0
    sbytes, stimes = load_dir(path, stream, port)       # server->client
    cbytes, ctimes = load_dir(path, stream, None)       # placeholder
    # client->server = frames whose srcport != game port; reload with the client srcport
    # find client srcport from the stream
    info = subprocess.run([TSHARK,"-r",path,"-Y",f"tcp.stream=={stream}","-T","fields","-e","tcp.srcport","-e","tcp.dstport"],
        capture_output=True,text=True,errors="replace").stdout
    csrc=None
    for ln in info.splitlines():
        a=ln.split('\t')
        if len(a)>=2 and a[1]==str(port): csrc=a[0]; break
    cbytes, ctimes = load_dir(path, stream, csrc)
    sp = split_with_time(sbytes, stimes)
    cp = split_with_time(cbytes, ctimes)
    if not sp or len(sp[0][0])<11:
        print("no SM_KEY"); return
    skey = basekey(fkb(struct.unpack('<I', sp[0][0][7:11])[0]))
    ckey = basekey(fkb(struct.unpack('<I', sp[0][0][7:11])[0]))
    timeline=[]
    for pkt,t in sp[1:]:
        b=bytearray(pkt[2:]); size=len(b)
        if size:
            prev=b[0]; b[0]^=skey[0]&0xFF
            for i in range(1,size):
                c=b[i]&0xFF; b[i]^=(STATIC_KEY[i&63]&0xFF)^(skey[i&7]&0xFF)^prev; prev=c
            skey=ub((ku(skey)+size)&0xFFFFFFFFFFFFFFFF)
            op=b[0]|(b[1]<<8) if size>=2 else 0
            timeline.append((t,'S',dsc(op),len(pkt),bytes(b)))
    for pkt,t in cp:
        plain,ckey,ok=dec(pkt,ckey)
        if ok and len(plain)>=4:
            timeline.append((t,'C',plain[2]|(plain[3]<<8),len(pkt),plain))
    timeline.sort(key=lambda x:x[0])
    target = int(sys.argv[5],16) if len(sys.argv)>5 else None
    for t,d,op,ln,body in timeline:
        if t<tmin: continue
        if target is not None:
            if op==target:
                print(f"t={t:7.2f} {d} 0x{op:04X} len={ln} body={body[7:].hex()}")
        else:
            print(f"t={t:7.2f} {d} 0x{op:04X} len={ln}")

if __name__=="__main__": main()
