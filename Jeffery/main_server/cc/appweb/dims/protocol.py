'''
Created on 2016/10/6
@author: Jeffery

'''
from twisted.internet import protocol
import json

class DimsProtocol(protocol.Protocol):
    def __init__(self, factory, addr):
        self.addr = addr
        self.factory = factory
    
    def connectionMade(self):
        self.factory.numConnection += 1;
        
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
            
    def connectionLost(self, reason=protocol.connectionDone):
        print reason
        self.factory.numConnection -= 1
        

class ProtocolResolver():
    def __init__(self, dimsprotocol, data):
        self.dims = dimsprotocol
        self.data = data
        self.resolveMsg()
        
    def getRunnable(self):
        return self.service
        
    def resolveMsg(self):
        msg = json.loads(self.data)
        if msg['type'] == 8000:
            self.dims.port = msg['port']
            self.service = JoinService(self.dims)
        elif msg['type'] == 8001:
            appid = msg['appid']
            self.service = AccessService(self.dims, appid)
        else:
            raise Exception, "no such protocol"

class ProtocolRunable():
    def run(self):
        pass
    def getResponse(self):
        pass
    
class JoinService(ProtocolRunable):
    def __init__(self, dims):
        self.dims = dims
    def run(self):
        key = self.dims.addr.host + '-' + str(self.dims.port)   # 127.0.0.1-8010
        self.dims.factory.nodeConnections[key] = 0
        self.response={}
        self.response['code']=200
    def getResponse(self):
        return json.dumps(self.response)


    
class AccessService(ProtocolRunable):
    def __init__(self, dims, appid):
        self.appid = appid
        self.dims = dims
    def run(self):
        #check appid
        #database
        
        #---------------- core ----------------
        #accept
        appidDict = self.dims.factory.appidConnections
        connectionDict = self.dims.factory.nodeConnections
        self.response = {}
        if appidDict.has_key(self.appid):
            self.response['code'] = 200
            self.response['nodeMsg'] = appidDict[self.appid]
        else:
            mMin = 10000000         # INF
            mkey = None
            for key in connectionDict:
                if mMin >= connectionDict[key]:
                    mMin = connectionDict[key]
                    mkey = key
            if mkey is None:
                self.response['code'] = 503
            else:
                self.response['code'] = 200
                self.response['nodeMsg'] = mkey
        
    def getResponse(self):
        return json.dumps(self.response)



