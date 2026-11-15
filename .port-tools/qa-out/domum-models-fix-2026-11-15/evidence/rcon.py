import socket,struct,sys,time

def command(cmd):
 s=socket.create_connection(('127.0.0.1',29362),timeout=10)
 def send(i,t,msg):
  b=struct.pack('<ii',i,t)+msg.encode()+b'\0\0';s.sendall(struct.pack('<i',len(b))+b)
 def recv():
  def exact(n):
   b=b''
   while len(b)<n:
    a=s.recv(n-len(b))
    if not a: raise EOFError()
    b+=a
   return b
  n=struct.unpack('<i',exact(4))[0]; b=exact(n);return struct.unpack('<ii',b[:8]),b[8:-2].decode()
 send(1,3,'domum-models-local');recv();send(2,2,cmd);res=recv();s.close();return res
if __name__=='__main__':
 for cmd in sys.argv[1:]:
  print(cmd,command(cmd),flush=True)
