# encoding: utf-8
'''
Created on 2016/10/6

@author: Jeffery
'''

from twisted.internet import reactor
from cc.appweb.dims import register, server

if __name__ == '__main__':
    # 向主服务器注册节点服务器
    # ip端口 以后可配
    reactor.connectTCP('192.168.253.1', 8000, register.RegisterFactory(reactor))
    # 进入事件循环
    reactor.run()
    
