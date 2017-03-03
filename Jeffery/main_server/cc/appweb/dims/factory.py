'''
Created on 2016/10/6

@author: Jeffery
'''

from twisted.internet.protocol import Factory
from protocol import DimsProtocol

class DimsFactory(Factory):
    def __init__(self):
        self.numConnection = 0                 # 连接数
        self.nodeConnections = {}              # 节点服务器集合
        self.appidConnections = {}             # appid集合
        print 'Factory init'
        
    def buildProtocol(self, addr):
        # 返回具体协议
        return DimsProtocol(self, addr)