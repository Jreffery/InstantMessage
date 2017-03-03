# encoding: utf-8
'''
Created on 2016/10/6

@author: Jeffery
'''

from twisted.internet.protocol import Factory
from protocol import DimsProtocol

class DimsFactory(Factory):
    def __init__(self):
        self.numConnection = 0                 # 连接数
        self.nodeConnections = {}              # 节点服务器字典
        self.appidConnections = {}             # appid字典
        print 'Factory init'
        
    def buildProtocol(self, addr):
        # 返回具体协议
        return DimsProtocol(self, addr)