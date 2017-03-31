#encoding: utf-8
'''
Created on 2017年3月26日

@author: Ransecy
'''
import logging
from configure import config

class Logger():
    def __init__(self, logname, loglevel, logger):
        format_dict = {
            1 : logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s'),
            2 : logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s'),
            3 : logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s'),
            4 : logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s'),
            5 : logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
        }
        self.logger = logging.getLogger(logger)
        self.logger.setLevel(logging.DEBUG)
        fh = logging.FileHandler(logname)
        fh.setLevel(logging.DEBUG)
        ch = logging.StreamHandler()
        ch.setLevel(logging.DEBUG)
        formatter = format_dict[int(loglevel)]
        fh.setFormatter(formatter)
        ch.setFormatter(formatter)
        self.logger.addHandler(fh)
        self.logger.addHandler(ch)
        
    def getlog(self):
        return self.logger

# 路径在configure 默认配置
logger = Logger(config.mainServerLog, config.mainServerLogLevel, config.mainServerLogName).getlog()  