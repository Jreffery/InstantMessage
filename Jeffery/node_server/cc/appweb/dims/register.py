'''
Created on 2016/10/6

@author: Jeffery
'''
from twisted.internet import protocol
import json

class RegisterProtocol(protocol.Protocol):
    def __init__(self, runnable):
        self.run = runnable
        
    def connectionMade(self):
        self.run.run(self)
    
    def dataReceived(self, data):
        self.run.handle(self,data)
        
class RegisterFactory(protocol.ClientFactory):
    def __init__(self, reactor):
        self.reactor = reactor
    def buildProtocol(self, addr):
        return RegisterProtocol(RegisterWorker())
    def clientConnectionFailed(self, connector, reason):
        print 'Connection failed:', reason.getErrorMessage()
    def clientConnectionLost(self, connector, reason):
        print "Connection lost:", reason.getErrorMessage()
        #stop to run
        self.reactor.stop()
    
class RegisterWorker():
    def run(self, protocol):
        regMsg = {}
        regMsg['type'] = 8000
        regMsg['port'] = 8010
        protocol.transport.write(json.dumps(regMsg))
    def handle(self, protocol, data):
        print data
        resMsg = json.loads(data)
        if resMsg['code'] == 200:
            pass
        else:
            print resMsg['errMsg']
            