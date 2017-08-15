#!/usr/bin/python

"""
This is a simple example that demonstrates multiple links
between nodes.
"""

from mininet.cli import CLI
from mininet.log import setLogLevel
from mininet.net import Mininet
from mininet.topo import Topo
from mininet.link import TCLink, TCIntf, Link
from time import sleep


def runMultiLink():
    "Create and run multiple link network"
    topo = simpleMultiLinkTopo( n=2 )
    net = Mininet( topo=topo )
    net.start()
    return net


def runApps(net):
    "Run cmds in command.txt"
    print "Running applications"
    with open("command.txt") as f:
        for line in f:
            splitedLine = line.split(' ', 1)
            print splitedLine[0]
            host = net.get(splitedLine[0])
            print splitedLine[1]
            host.cmd(splitedLine[1])

class simpleMultiLinkTopo( Topo ):
    "Simple topology with multiple links"

    def __init__( self, n, **kwargs ):
        Topo.__init__( self, **kwargs )

        h1 = self.addHost('h1', ip='10.0.0.1')
        h2 = self.addHost('h2', ip='10.0.0.2')
    
        s1 = self.addSwitch('s1')
        s2 = self.addSwitch('s2')
    
        self.addLink(s1,s2,intf=TCIntf, params1 = { 'bw': 10 , 'delay' : '5ms' , 'loss' : 0}, params2 = { 'bw': 10 , 'delay' : '5ms' , 'loss' : 0})
        self.addLink(h1,s1,intf=TCIntf, params1 = { 'bw': 10 , 'delay' : '5ms' , 'loss' : 0}, params2 = { 'bw': 10 , 'delay' : '5ms' , 'loss' : 0})
        self.addLink(h2,s2,intf=TCIntf, params1 = { 'bw': 10 , 'delay' : '5ms' , 'loss' : 0}, params2 = { 'bw': 10 , 'delay' : '5ms' , 'loss' : 0})
    
if __name__ == '__main__':
    setLogLevel( 'info' )
    net = runMultiLink()
    runApps(net)
    sleep(10)
    #CLI( net )
    net.stop()
