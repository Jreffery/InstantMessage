'''
Created on 2016/10/6

@author: Jeffery
'''

from twisted.internet import reactor
from cc.appweb.dims import register, server

if __name__ == '__main__':
    reactor.connectTCP('localhost',8000, register.RegisterFactory(reactor))
    # 进入事件循环
    reactor.run()
    
