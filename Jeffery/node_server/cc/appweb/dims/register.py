# encoding: utf-8
'''
Created on 2016/10/6

@author: Jeffery
'''
from twisted.internet import protocol
from server import NodeFactory
from cc.appweb.utils.configure import config
import json, random

# 注册者协议
class RegisterProtocol(protocol.Protocol):
    def __init__(self, runnable):
        self.run = runnable
        
    def connectionMade(self):
        self.run.run(self)
    
    def dataReceived(self, data):
        self.run.handle(self, data)

# 注册者工厂        
class RegisterFactory(protocol.ClientFactory):
    def __init__(self, reactor):
        self.reactor = reactor
        
    def buildProtocol(self, addr):
        # 注册协议
        return RegisterProtocol(RegisterWorker(self.reactor))

    # 连接失败
    def clientConnectionFailed(self, connector, reason):
        print 'Connection failed: ' + reason.getErrorMessage()
        
    # 断开连接    
    def clientConnectionLost(self, connector, reason):
        print 'Connection lost: ' + reason.getErrorMessage()
    
# 注册工人--真正执行注册的核心    
class RegisterWorker():
    listenPort = config.nodeServerPort
    
    def __init__(self, reactor):
        self.reactor = reactor
        
    def run(self, protocol):
        self.protocol = protocol
        regMsg = {}
        regMsg['type'] = 8000
        regMsg['port'] = self.listenPort
        self.protocol.transport.write(json.dumps(regMsg))
    
    # 处理数据   
    def handle(self, protocol, data):
        print data
        resMsg = json.loads(data)
        if resMsg['type'] == 7000:
            # 加入服务的响应
            if resMsg['code'] == 200:
                print 'Dims Node server run!'
                self.nodeFactory = NodeFactory(self.protocol)
                self.reactor.listenTCP(self.listenPort, self.nodeFactory)
            else:
                print resMsg['errMsg']
        if resMsg['type'] == 7020:
            appid = resMsg['appid']
            usr = resMsg['receiver']
            transferData = {}
            transferData['type'] = 7103
            transferData['data'] = resMsg['data']
            transferData['sender'] = resMsg['sender']
            self.nodeFactory.postMsgToUser(appid, usr, json.dumps(transferData))
            
            