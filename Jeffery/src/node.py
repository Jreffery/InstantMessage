#coding=utf-8

from twisted.internet.protocol import Factory
from twisted.internet import reactor, protocol
import sys, threading, socket

class ServerNodeProtocol(protocol.Protocol):
    def __init__(self, factory):
        self.factory = factory
    def connectionMade(self):
        self.factory.addConnections()
        self.transport.write("You have connected server node.\n")

class ServerNodeFactory(Factory):
    def __init__(self):
        self.clientNum = 0
        print "Server node run using port " + sys.argv[1] 
    def buildProtocol(self, addr):
        return ServerNodeProtocol(self)
    def addConnections(self):
        self.clientNum += 1;
        #ConnectMain(1, self.clientNum).start()
    def removeConnections(self):
        self.clientNum -= 1;
        #ConnectMain(1, self.clientNum).start()

class ConnectMain(threading.Thread):
    def __init__(self, type, data):
        self.type = type;
        self.data = data;        
    def run(self):
        if self.type == 1:    #修改连接数
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.connect(("127.0.0.1",8013))
            s.send("002\n"+self.data)
            
if len(sys.argv) != 2:
    print "Please point out the port"
    exit(1)

reload(sys)
sys.setdefaultencoding('utf8')
reactor.listenTCP(int(sys.argv[1]), ServerNodeFactory())
reactor.run()