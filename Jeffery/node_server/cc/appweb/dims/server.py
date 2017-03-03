'''
Created on 2016/10/7

@author: Jeffery
'''

from twisted.internet import protocol
import json

# 对用户服务协议
class NodeProtocol(protocol.Protocol):
    def __init__(self, factory):
        self.factory = factory
    def connectionMade(self):
        self.factory.connectionNum += 1
    def connectionLost(self, reason=protocol.connectionDone):
        self.factory.connectionNum -= 1
    def dataReceived(self, data):
        try:
            resolver = ProtocolResolver(self, data)
            self.response = resolver.getRunnable()
            self.response.run()
            self.transport.write(self.response.getResponse())
        except Exception, ex:
            response = {}
            response['code'] = 503
            response['errMsg'] = str(ex)
            self.transport.write(json.dumps(response))
        self.transport.loseConnection()  

class NodeFactory(protocol.Factory):
    def __init__(self):
        self.connectionNum = 0
    def buildProtocol(self, addr):
        return NodeProtocol(self) 
    
    
class ProtocolResolver():
    def __init__(self, dimsprotocol, data):
        self.dims = dimsprotocol
        self.data = data
        self.resolveMsg()
        
    def getRunnable(self):
        return self.service
        
    def resolveMsg(self):
        msg = json.loads(self.data)
        if msg['type'] == 8002:
            self.service = AuthService(msg)
        else:
            raise Exception, "no such protocol"

class ProtocolRunable():
    def run(self):
        pass
    def getResponse(self):
        pass
        
class AuthService(ProtocolRunable):
    def __init__(self, data):
        self.data = data
        self.response = {}
    def run(self):
        appid = self.data['appid']
        usr = self.data['usr']
        pwd = self.data['pwd']
        print appid, usr, pwd
    def getResponse(self):
        self.response['code'] = 200
        return json.dumps(self.response)
        
        