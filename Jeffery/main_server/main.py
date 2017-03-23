# encoding: utf-8
'''
Created on 2016/10/6

@author: Jeffery
'''

from twisted.internet import reactor
from cc.appweb.dims.factory import DimsFactory
from cc.appweb.utils.configure import config
from cc.appweb.utils.logger import logger
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
    except:
        pass

# 主程序，主服务器的入口
if __name__ == '__main__':
    __initConfigure__()
    # 监听端口号8000，后期需可配置
    reactor.listenTCP(config.mainServerPort, DimsFactory())
    logger.info('The Reactor listen at port: %s!',config.mainServerPort)
    # 进入事件循环
    reactor.run()
    
