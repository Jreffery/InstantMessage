# encoding: utf-8
'''
Created on 2016/10/6

@author: Jeffery
'''

from twisted.internet import reactor
from cc.appweb.dims.factory import DimsFactory

# 主程序，主服务器的入口
if __name__ == '__main__':
    # 监听端口号8000，后期需可配置
    reactor.listenTCP(8001, DimsFactory())
    # 进入事件循环
    reactor.run()