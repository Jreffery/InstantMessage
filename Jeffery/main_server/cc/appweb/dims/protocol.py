# encoding: utf-8
'''
Created on 2016/10/6
@author: Jeffery

'''
from twisted.internet import protocol
from abstract import ProtocolRunnable
from cc.appweb.utils.logger import logger
import json

class DimsProtocol(protocol.Protocol):
    
    isNode = False              # 是否节点服务器            
    
    # 初始化构造函数
    def __init__(self, factory, addr):
        # ip地址、端口
        self.host = addr.host
        self.keepport = addr.port
        self.factory = factory                # 生成协议的工厂
    
    # 创建连接时调用
    def connectionMade(self):
        #print 'Connection ' + self.host + ':' + str(self.keepport) + ' connected!' 
        logger.info('Connection %s : %s connected!',self.host,str(self.keepport))
        
    # 接收数据时调用
    def dataReceived(self, data):
        print data
        try:
            resolver = ProtocolResolver(self, data)             # 根据协议解析数据
            self.response = resolver.getRunnable()              # 接收请求后执行
            self.response.run()
            responseData = self.response.getResponse()
            if not responseData == None:              
                self.transport.write(self.response.getResponse())   # 接收请求后需要响应的
            self.response.afterResponse()
            
        # 异常捕获
        except Exception, ex:
            response = {}
            response['code'] = 503
            response['errMsg'] = str(ex)
            self.transport.write(json.dumps(response))
            
    
    # 连接断开时调用        
    def connectionLost(self, reason=protocol.connectionDone):
        #print 'Connection ' + self.host + ':' + str(self.keepport) + ' lost.'
        logger.info('Connection %s : %s lost.',self.host,str(self.keepport))
        # 如果是节点服务器，则从服务集群中删除该服务器
        if self.isNode:
            self.factory.removeNode(self)
        
# 协议的解析者
# 对json数据中的请求码作一层解析
class ProtocolResolver():
    def __init__(self, protocol, data):
        self.protocol = protocol              # 协议
        self.data = data                      # 数据
        self.resolveMsg()                     # 解析协议
        
    def getRunnable(self):
        return self.service                   # 返回服务
        
    # # 解析协议
    def resolveMsg(self):
        msg = json.loads(self.data)
        if msg['type'] == 8000:
            self.protocol.port = msg['port']
            self.service = JoinService(self.protocol)
        elif msg['type'] == 8001:
            appid = msg['appid']
            self.service = AccessService(self.protocol, appid)
        elif msg['type'] == 8010:
            usr = msg['usr']
            appid = msg['appid']
            self.service = UpdateUserNodeMapService(self.protocol, usr, appid, True)
        elif msg['type'] == 8011:
            usr = msg['usr']
            appid = msg['appid']
            self.service = UpdateUserNodeMapService(self.protocol, usr, appid, False)
        elif msg['type'] == 8020:
            self.service = MainTransferService(msg, self.protocol)
        else:
            logger.exception('no such protocol')
            raise Exception, "no such protocol"

#--------------------------------------Service--------------------------------
# 每增加一种服务，都需要继承ProtocolRunnable
# 并重写里面的两个重要方法
    
# 具体服务定义 --加入节点服务   
class JoinService(ProtocolRunnable):
    def __init__(self, protocol):
        self.protocol = protocol
    def run(self):
        self.protocol.isNode = True
        self.protocol.factory.addNode(self.protocol)
        self.protocol.userCount = 0
        self.response={}
        self.response['type'] = 7000
        self.response['code']=200
    def getResponse(self):
        return json.dumps(self.response)
    
    def afterResponse(self):
        pass


# 具体服务定义 -- 接入用户服务  
class AccessService(ProtocolRunnable):
    def __init__(self, protocol, appId):
        self.appId = appId
        self.protocol = protocol
        
    def run(self):
        nodeInfo = self.protocol.factory.dispatch(self.appId)
        self.response = {}
        if nodeInfo == None:
            self.response['code'] = 503
        else:
            self.response['code'] = 200
            self.response['nodeMsg'] = nodeInfo
        
    def getResponse(self):
        return json.dumps(self.response)

    def afterResponse(self):
        self.protocol.transport.loseConnection()        #对用户来说，响应完就断开连接

# 更新用户--nodeMap
class UpdateUserNodeMapService(ProtocolRunnable):
    def __init__(self, protocol, usr, appid, flag):
        self.protocol = protocol
        self.usr = usr
        self.appid = appid
        self.flag = flag
        
    def run(self):
        self.protocol.factory.updateUserNodeMap(self.appid+'-'+self.usr, self.protocol, self.flag)
        
    def getResponse(self):
        pass
    
    def afterResponse(self):
        pass
        
# 主服务器的转发服务
class MainTransferService(ProtocolRunnable):
    def __init__(self, data, protocol):
        self.data = data
        self.protocol = protocol
        
    def run(self):
        self.recevier = self.data['receiver']
        self.appid = self.data['appid']
        self.msg = self.data['data']
        self.sender = self.data['sender']
        # 先检查用户是否在线
        self.targetProtocol = self.protocol.factory.findNodeWithUser(self.appid+'-'+self.recevier)
        
    def getResponse(self):
        return None
    
    def afterResponse(self):
        if self.targetProtocol == None:
            # 按道理这里应该是缓存起来
            # 等具体用户上线后推送给它
            # 这里先暂时不要处理
            pass
        else:
            responseData = {}
            responseData['type'] = 7020
            responseData['receiver'] = self.recevier
            responseData['appid'] = self.appid
            responseData['data'] = self.msg
            responseData['sender'] = self.sender
            self.targetProtocol.transport.write(json.dumps(responseData))  #转发
            