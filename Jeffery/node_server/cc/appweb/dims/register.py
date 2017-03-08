# encoding: utf-8
'''
Created on 2016/10/6

@author: Jeffery
'''
from twisted.internet import protocol
import json, server

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
        return RegisterProtocol(RegisterWorker())

    # 连接失败
    def clientConnectionFailed(self, connector, reason):
        print 'Connection failed:', reason.getErrorMessage()
        
    # 断开连接    
    def clientConnectionLost(self, connector, reason):
        print "Run Server"
        # 启动服务
        self.reactor.listenTCP(8010, server.NodeFactory(), interface='')
    
# 注册工人--真正执行注册的核心    
class RegisterWorker():
    def run(self, protocol):
        # json协议，端口可配
        regMsg = {}
        regMsg['type'] = 8000
        regMsg['port'] = 8010
        protocol.transport.write(json.dumps(regMsg))
    
    # 执行    
    def handle(self, protocol, data):
        print data
        resMsg = json.loads(data)
        if resMsg['code'] == 200:
            # 成功后断开连接
            protocol.transport.loseConnection()

        else:
            print resMsg['errMsg']
            