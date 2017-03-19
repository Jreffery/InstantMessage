# encoding: utf-8
'''
Created on 2016/10/6

@author: Jeffery
'''

from twisted.internet import reactor
from cc.appweb.dims.register import RegisterFactory
from cc.appweb.utils.configure import config
import sys, json

def __initConfigure__():
    try:
        if len(sys.argv) == 2:
            # 配置文件
            con = json.load(open(sys.argv[1], 'r'))
        else:
            # 默认
            con = json.load(open('configure.json', 'r'))
        if con.has_key('mainserverip'):
            config.mainServerIp = con['mainserverip']
        if con.has_key('mainserverport'):
            config.mainServerPort = con['mainserverport']
        if con.has_key('nodeserverport'):
            config.nodeServerPort = con['nodeserverport']
    except:
        pass

if __name__ == '__main__':
    __initConfigure__()
    # 向主服务器注册节点服务器
    # ip端口 以后可配
    reactor.connectTCP(config.mainServerIp, config.mainServerPort, RegisterFactory(reactor))
    # 进入事件循环
    reactor.run()
    
