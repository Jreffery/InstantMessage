'''
Created on 2016/10/6

@author: Jeffery
'''

from twisted.internet import reactor
from cc.appweb.dims import register
if __name__ == '__main__':
    reactor.connectTCP('localhost',8000, register.RegisterFactory(reactor))
    reactor.run()
    print 'reactor stop'
    
    # run node server waiting user