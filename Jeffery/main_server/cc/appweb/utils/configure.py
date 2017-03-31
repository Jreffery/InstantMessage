#encoding: utf-8
'''
Created on 2017年3月20日

@author: Administrator
'''
# 配置模块
# 有需要进行文件配置的值
# 在该类上添加字段
# 并且在主函数的初始化函数中赋值

class Config():
    mainServerIp = '127.0.0.1'
    mainServerPort = 8001
    nodeServerIp = '127.0.0.1'
    nodeServerPort = 8002
    
    # hardcode 的配置
    mainServerLog = "mainserverlog.txt"
    mainServerLogLevel = 1
    mainServerLogName = "MainServerLog"
    def __init__(self):
        pass
    
config = Config()