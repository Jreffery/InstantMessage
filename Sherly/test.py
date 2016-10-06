# client  
  
import socket  
  
address = ('127.0.0.1', 8000)  
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)  
s.connect(address)  

s.send('{"type":8001,"appid":"1111111111111111"}')  
  
data = s.recv(512)  
print 'the data received is',data  
  

s.close()  