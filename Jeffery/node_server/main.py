# encoding: utf-8
'''
Created on 2016/10/6

@author: Jeffery
'''

from twisted.internet import reactor
from cc.appweb.dims.register import RegisterFactory

if __name__ == '__main__':
    # 向主服务器注册节点服务器
    # ip端口 以后可配
    reactor.connectTCP('127.0.0.1', 8001, RegisterFactory(reactor))
    # 进入事件循环
    reactor.run()
    
