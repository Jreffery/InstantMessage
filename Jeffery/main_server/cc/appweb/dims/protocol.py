'''
Created on 2016/10/6
@author: Jeffery

'''
from twisted.internet import protocol
import json

class DimsProtocol(protocol.Protocol):
    
    # 初始化构造函数
    def __init__(self, factory, addr):
        self.addr = addr                      # 连接方的地址
        self.factory = factory                # 生成协议的工厂
    
    # 创建连接时调用
    def connectionMade(self):
        self.factory.numConnection += 1;      # 连接数    
        
    # 接收数据时调用
    def dataReceived(self, data):
        try:
            resolver = ProtocolResolver(self, data)             # 根据协议解析数据
            self.response = resolver.getRunnable()              # 接收请求后需要做的
            self.response.run()                              
            self.transport.write(self.response.getResponse())   # 接收请求后需要响应的
        
        # 异常捕获
        except Exception, ex:
            response = {}
            response['code'] = 503
            response['errMsg'] = str(ex)
            self.transport.write(json.dumps(response))
            
        # 主动断开连接
        self.transport.loseConnection()    
    
    # 连接断开时调用        
    def connectionLost(self, reason=protocol.connectionDone):
        print reason
        self.factory.numConnection -= 1
        
# 协议的解析者
class ProtocolResolver():
    def __init__(self, dimsprotocol, data):
        self.dims = dimsprotocol              # 协议
        self.data = data                      # 数据
        self.resolveMsg()                     # 解析协议
        
    def getRunnable(self):
        return self.service                   # 返回服务
        
    # # 解析协议
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

# 抽象对象
class ProtocolRunable():
    def run(self):
        pass
    def getResponse(self):
        pass
    
# 具体服务定义 --加入节点服务   
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

# 具体服务定义 -- 接入用户服务  
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



