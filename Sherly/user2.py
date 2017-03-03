# encoding: utf-8
# client  
  
import socket, json  
  
address = ('127.0.0.1', 8000)  
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)  
s.connect(address)  

s.send('{"type":8001,"appid":"1111111111111111","usr":"ranscey","pwd":"xxxxxx"}')  
  
data = s.recv(512)  
print 'the data received is', data  

rep = json.loads(data)
if rep['code'] == 200:
	nodeInfo = rep['nodeMsg']
	host = nodeInfo[0:nodeInfo.find('-')]
	port = int(nodeInfo[nodeInfo.find('-')+1:])
	address = (host,port)
	s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)  
	s.connect(address)
	s.send('{"type":8002,"appid":"1111111111111111","usr":"ranscey","pwd":"xxxxxx"}')  
	data = s.recv(512)  
	print 'the data received is', data   

data = s.recv(512)  
print 'the data received is', data 

s.close()  