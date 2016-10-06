'''
Created on 2016/10/6

@author: Jeffery
'''

from twisted.internet.protocol import Factory
from protocol import DimsProtocol

class DimsFactory(Factory):
    def __init__(self):
        self.numConnection = 0
        self.nodeConnections = {}
        self.appidConnections = {}
        print 'Factory init'
        
    def buildProtocol(self, addr):
        return DimsProtocol(self, addr)