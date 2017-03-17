# encoding: utf-8
'''
Created on 2016/10/7

@author: Jeffery
'''

from twisted.internet import protocol
from abstract import ProtocolRunnable
from cc.appweb.utils import byteEncode
import json

# 对用户服务协议
class NodeProtocol(protocol.Protocol):
    def __init__(self, factory, addr):
        self.host = addr.host
        self.port = addr.port
        self.factory = factory
        
    def connectionMade(self):
        print 'Connection ' + self.host + ':' + str(self.port) + ' connected.'
        
    def connectionLost(self, reason=protocol.connectionDone):
        print 'Connection ' + self.host + ':' + str(self.port) + ' lost.'
        postToMain = {}
        postToMain['type'] = 8011
        postToMain['usr'] = self.usr
        postToMain['appid'] = self.appid
        self.factory.postMsgToMain(json.dumps(postToMain))
        
    def dataReceived(self, data):
        print data
        try:
            resolver = ProtocolResolver(self, data)  
            self.response = resolver.getRunnable()
            self.response.run()
            responseData = self.response.getResponse()
            if not responseData == None:              
                self.transport.write(self.response.getResponse())   # 接收请求后需要响应的
            self.response.afterResponse()    
        except Exception, ex:
            response = {}
            response['code'] = 503
            response['errMsg'] = str(ex)
            responseData = json.dumps(response)
            self.transport.write(responseData)


class NodeFactory(protocol.Factory):
    def __init__(self, registerProtocol):
        self.registerProtocol = registerProtocol
        self.nodeSet = {}
        
    def buildProtocol(self, addr):
        return NodeProtocol(self, addr)
    
    def postMsgToMain(self, data):
        self.registerProtocol.transport.write(data) 
    
    def postMsgToUser(self, appid, usr, data):
        # 判断用户是否在线
        if self.nodeSet.has_key(appid+'-'+usr) == None:
            # 不在线，应该缓存起来
            # 暂时不实现
            pass
        else :
            targetProtocol = self.nodeSet[appid+'-'+usr]
            targetProtocol.transport.write(byteEncode.getMsgStart(data)+data)
    
class ProtocolResolver():
    def __init__(self, protocol, data):
        self.protocol = protocol
        self.data = data
        self.resolveMsg()
        
    def getRunnable(self):
        return self.service
        
    def resolveMsg(self):
        msg = json.loads(self.data)
        if msg['type'] == 8002:
            self.service = AuthService(msg, self.protocol)
        elif msg['type'] == 8003:
            self.service = TransferService(msg, self.protocol)
        elif msg['type'] == 8004:
            self.service = NoResponseService()
        else:
            raise Exception, "no such protocol"

# 认证服务，将用户加入集群
class AuthService(ProtocolRunnable):
    def __init__(self, data, protocol):
        self.protocol = protocol
        self.data = data
        self.response = {}
    def run(self):
        self.appid = self.protocol.appid =  self.data['appid']
        self.usr = self.protocol.usr = self.data['usr']
        self.pwd = self.data['pwd']
        self.protocol.factory.nodeSet[self.appid + '-' + self.usr] = self.protocol
        self.response['type'] = 7002
        self.response['code'] = 200
        print self.appid, self.usr, self.pwd
        
    def getResponse(self):
        responseData = json.dumps(self.response)
        return byteEncode.getMsgStart(responseData) + responseData
    
    def afterResponse(self):
        # 向主服务器提交新增用户
        postToMain = {}
        postToMain['type'] = 8010
        postToMain['usr'] = self.usr
        postToMain['appid'] = self.appid
        self.protocol.factory.postMsgToMain(json.dumps(postToMain))
        
# 转发服务
class TransferService(ProtocolRunnable):
    def __init__(self, data, protocol):
        self.protocol = protocol
        self.data = data
        self.response = {}
        self.needResponse = True
    
    def run(self):
        if self.protocol.factory.nodeSet.has_key(self.protocol.appid +'-' +self.data['receiver']):
            receiver = self.protocol.factory.nodeSet[self.protocol.appid +'-' +self.data['receiver']]
            transferData = {}
            transferData['type'] = 7103
            transferData['sender'] = self.protocol.usr
            transferData['data'] = self.data['data']
            tranferMsg = json.dumps(transferData)
            receiver.transport.write(byteEncode.getMsgStart(tranferMsg) + tranferMsg)   # 转发
        else:
            # 不在当前节点服务器上
            # 需要转发给主服务器
            postToMain = {}
            postToMain['type'] = 8020
            postToMain['receiver'] = self.data['receiver']
            postToMain['appid'] = self.protocol.appid
            postToMain['data'] = self.data['data']
            postToMain['sender'] = self.protocol.usr
            self.protocol.factory.postMsgToMain(json.dumps(postToMain))
        self.response['type'] = 7003
        self.response['msgID'] = self.data['msgID']
        self.response['code'] = 200
        
    def getResponse(self):
        if self.needResponse:
            responseData = json.dumps(self.response)
            return byteEncode.getMsgStart(responseData) + responseData
        else:
            return None
    
    # 响应完需要完成的工作
    def afterResponse(self):
        pass

# 不响应服务
# 心跳
class NoResponseService(ProtocolRunnable):
    # nothing
    def run(self):
        pass
    
    def getResponse(self):
        return None
    
    # 响应完需要完成的工作
    def afterResponse(self):
        pass         
        