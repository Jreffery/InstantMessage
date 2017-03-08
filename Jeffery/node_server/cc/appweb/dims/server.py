# encoding: utf-8
'''
Created on 2016/10/7

@author: Jeffery
'''

from twisted.internet import protocol
from abstract import ProtocolRunnable
import json

# 对用户服务协议
class NodeProtocol(protocol.Protocol):
    def __init__(self, factory, addr):
        self.addr = addr
        self.factory = factory
        
    def connectionMade(self):
        print 'Connection ' + self.addr.host + ':' + str(self.addr.port) + ' connected.'
        self.factory.connectionNum += 1
        
    def connectionLost(self, reason=protocol.connectionDone):
        print 'Connection ' + self.addr.host + ':' + str(self.addr.port) + ' lost.'
        self.factory.connectionNum -= 1
        
    def dataReceived(self, data):
        print data
        try:
            resolver = ProtocolResolver(self, data)
            self.response = resolver.getRunnable()
            self.response.run(self)
            self.transport.write(self.response.getResponse())
        except Exception, ex:
            response = {}
            response['code'] = 503
            response['errMsg'] = str(ex)
            self.transport.write(json.dumps(response))

class NodeFactory(protocol.Factory):
    def __init__(self):
        self.connectionNum = 0
        self.nodeSet = {}
        
    def buildProtocol(self, addr):
        return NodeProtocol(self, addr) 
    
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
        elif msg['type'] == 8003:
            self.service = TransferService(msg)
        elif msg['type'] == 8004:
            pass
        else:
            raise Exception, "no such protocol"

# 认证服务，将用户加入集群
class AuthService(ProtocolRunnable):
    def __init__(self, data):
        self.data = data
        self.response = {}
    def run(self, protocol):
        appid = protocol.appid =  self.data['appid']
        usr = protocol.usr = self.data['usr']
        pwd = self.data['pwd']
        protocol.factory.nodeSet[appid + '-' + usr] = protocol
        self.response['type'] = 7002
        self.response['code'] = 200
        print appid, usr, pwd
    def getResponse(self):
        return json.dumps(self.response)
        
# 转发服务
class TransferService(ProtocolRunnable):
    def __init__(self, data):
        self.data = data
        self.response = {} 
    
    def run(self, protocol):
        if(protocol.factory.nodeSet[protocol.appid +'-' +self.data['receiver']] is not None):
            receiver = protocol.factory.nodeSet[protocol.appid +'-' +self.data['receiver']]
            transferData = {}
            transferData['type'] = 7103
            transferData['sender'] = protocol.usr
            transferData['data'] = self.data['data']
            receiver.transport.write(json.dumps(transferData))   # 转发
            self.response['type'] = 7003
            self.response['msgID'] = self.data['msgID']
            self.response['code'] = 200
        else:
            self.response['code'] = 503
        
    def getResponse(self):
        return json.dumps(self.response)
        
        