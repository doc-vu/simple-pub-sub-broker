Steps
====
1) Ensure the PADS App is running and mininet is installed for this example to run.
2) Create a new project and import the template file from this directory called: pads_mininet_simple_pubsubbroker.webgmex
3) One open click --> Experiments --> TestModel --> PubSubNetwork
4) Client on the Execute arrow -> on the left top tab and click CreateTopology and Execute
5) GenerateFiles zip folder will be created
6) Extract the folder contents.
7) create a result folder.
8) Copy the required files from this repo: runPub.sh runSub.sh runBroker.sh *.jar into the extracted folder directory
9) Update the topology.py file so that it automatically runs the commands from the command.txt on 
all the hosts. S
```
if __name__ == '__main__':
    setLogLevel( 'info' )
    net = runMultiLink()
    runApps(net)
    sleep(10)
    #CLI(net)
```
10) Issue the command 
```
sudo python topology.py
```

11) Results will be stored in the result/ directory
