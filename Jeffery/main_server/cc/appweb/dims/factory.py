# encoding: utf-8
'''
Created on 2016/10/6

@author: Jeffery
'''

from twisted.internet.protocol import Factory
from protocol import DimsProtocol
from cc.appweb.utils.logger import logger

class DimsFactory(Factory):
    nodeList = []            #只能存储节点服务器的连接对象
    
    def __init__(self):
        #print 'DIMS Main server run!'
        logger.info('DIMS Main server run!')
        self.disparcher = DispatchUserNode(self.nodeList)
        
    def buildProtocol(self, addr):
        # 返回具体协议
        return DimsProtocol(self, addr)
    
    def addNode(self, nodeServerProtocol):
        self.nodeList.append(nodeServerProtocol)
        
    def removeNode(self, nodeServerProtocol):
        self.nodeList.remove(nodeServerProtocol)
    
    def dispatch(self, appId):
        return self.disparcher.dispatch(appId)
    
    def updateUserNodeMap(self, appid_usr, protocol, flag):
        self.disparcher.updateUserNodeMap(appid_usr, protocol, flag)
    
    def findNodeWithUser(self, appid_usr):
        return self.disparcher.findNodeWithUser(appid_usr)
        
    
# 用户分发调度者
class DispatchUserNode():
    # 初始状态下，对应的appId所用的nodeServer
    appIdMap = {}
    
    # 用户所在nodeServer
    userMap = {}
    
    # 参数：
    # nodeList可用节点服务器列表
    def __init__(self, nodeList):
        self.nodeList = nodeList
        
    def dispatch(self, appId):
        # 先查找Map是否有对应
        if self.appIdMap.has_key(appId):
            # 检查服务器负载是否到阈值
            nodeServerProtocol = self.appIdMap[appId]
            if nodeServerProtocol.userCount < 1:              # 阈值1000
                return nodeServerProtocol.host + '-' + str(nodeServerProtocol.port)   # 返回节点信息
            else:
                node = self.__findMinUserCount()
                return node.host + '-' + str(node.port)
        # 遍历列表，查找最小负荷的nodeServer
        else:
            node = self.__findMinUserCount()
            self.appIdMap[appId] = node
            return node.host + '-' + str(node.port)
    
    def __findMinUserCount(self):
        size = len(self.nodeList)
        if size == 0:
            return None
        minCount = 10000000
        minIndex = -1
        for i in range(size):
            if minCount >= self.nodeList[i].userCount:
                minCount = self.nodeList[i].userCount
                minIndex = i
        return self.nodeList[minIndex]
        
    # flag 为True时代表新增
    # flag 为False时代表删除
    def updateUserNodeMap(self, appid_usr, protocol, flag):
        if flag == True:
            self.userMap[appid_usr] = protocol
            protocol.userCount += 1
        else:
            try:
                del(self.userMap[appid_usr])
                protocol.userCount -= 1
            except:
                pass  
    
    def findNodeWithUser(self, appid_usr):
        if self.userMap.has_key(appid_usr):
            return self.userMap[appid_usr]
        else:
            return None