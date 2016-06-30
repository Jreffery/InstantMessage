#coding=utf-8

from twisted.internet.protocol import Factory
from twisted.internet import reactor, protocol


class ServerProtocol(protocol.Protocol):
    def __init__(self, factory):
        self.factory = factory
    def connectionMade(self):
        self.factory.numConnections += 1
        print "client connected"
    def dataReceived(self, data):
        self.transport.write(data)
        type = int(data[0:3])       #类型
        print data
        if type == 1:
            self.user = data[3:]    #指定用户
            self.factory.registerClient(self, self.user)
        elif type == 2:
            flag = data.find('\n')
            receiver = data[3:flag]
            self.factory.transportByName(receiver, flag, data)
        #else  未定义
    def connectionLost(self, reason):
        self.factory.logoutClient(self.user)
        self.factory.numConnections -= 1

class ServerFactory(Factory):
    numConnections = 0
    clientSet = {}
    def __init__(self):
        print "Server run"
        
    def buildProtocol(self, addr):
        return ServerProtocol(self)
    def registerClient(self, clientInstance, name):
        self.clientSet[name] = clientInstance
        print "Register user " + name
    def logoutClient(self, name):
        self.clientSet.pop(name)
    def transportByName(self, flag, data):
        print "send data to " + data[3:flag]
        self.clientSet[data[3:flag]].transport.write(data[flag+1:])

if __name__ == "__main__":
    reactor.listenTCP(8013, ServerFactory())
    reactor.run()
    