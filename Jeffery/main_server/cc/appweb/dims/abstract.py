#encoding: utf-8
'''
Created on 2017年3月3日

@author: Jeffery
'''

# 抽象协议的执行者
class ProtocolRunnable():
    # 协议在服务器上进行的操作
    def run(self):
        pass
    
    # 协议数据的返回
    def getResponse(self):
        pass
    
    # 响应完需要完成的工作
    def afterResponse(self):
        pass