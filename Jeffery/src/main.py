#coding=utf-8

from twisted.internet.protocol import Factory
from twisted.internet import reactor, protocol
import sys, xml.dom.minidom

class ServerProtocol(protocol.Protocol):
    def __init__(self, factory):
        self.factory = factory
    def dataReceived(self, data):
        if data == '001':  # client 获取 连接 参数
            serverNode = self.factory.getSchedule()
            self.transport.write((serverNode.host+'\n'+serverNode.port).encode('utf8'))
            self.transport.loseConnection()
        #else  未定义

class ServerFactory(Factory):
    def __init__(self, serverNodes):
        self.serverNodes = serverNodes
        print "Server run using configure file " + sys.argv[1] 
    def buildProtocol(self, addr):
        return ServerProtocol(self)
    def getSchedule(self):  #调度 算法
        min = 10000000
        for server in self.serverNodes:
            if server.clientNum < min :
                minServer = server    #最小负载
                min = server.clientNum  
        return minServer

class serverNode():
    clientNum = 0
    host = None
    port = None
    name = None
    def __init__(self, host, port, name):
        self.host = host
        self.port = port
        self.name = name
    def addClient(self):
        self.clientNum += 1
    def removeClient(self):
        self.clientNum -= 1 

def getServers(fileName):
    dom = xml.dom.minidom.parse(fileName)
    root = dom.documentElement
    #server
    serverSet = []
    for server in root.getElementsByTagName('server'):
        serverSet.append(serverNode(server.getAttribute("host"),server.getAttribute("port"),server.nodeName))
    return serverSet
    
if len(sys.argv) != 2:
    print "Please point out the configure file"
    exit(1)


reload(sys)
sys.setdefaultencoding('utf8')
reactor.listenTCP(8013, ServerFactory(getServers(sys.argv[1])))
reactor.run()
    