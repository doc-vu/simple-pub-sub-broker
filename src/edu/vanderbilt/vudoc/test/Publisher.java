package edu.vanderbilt.vudoc.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeromq.ZMQ;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Killian on 7/24/17.
 */
public class Publisher{
    String myID;
    String myIP;
    private ZMQ.Context zmqContext;
    // key: topic, value: a list of senders corresponding to this topic
    HashMap<String, ArrayList<Sender>> senders = new HashMap<>();

    // logger config
    private static final Logger logger = LogManager.getLogger(Publisher.class.getName());

    public Publisher(String myID, String myIP){
        this.myID = myID;
        this.myIP = myIP;
        this.zmqContext = ZMQ.context(1);
    }

    public void createSender(String topic, String destAddr){
        Sender newSender = new Sender(topic, destAddr, zmqContext);
        ArrayList<Sender> senderList = senders.getOrDefault(topic, new ArrayList<>());
        senderList.add(newSender);
        senders.put(topic, senderList);
        Thread t = new Thread(newSender);
        t.start();
        logger.debug("New Sender Created. topic: {}, destAddr: {}", topic, destAddr);
    }

    public void stopSender(String topic, String destAddr){
        ArrayList<Sender> senderList = senders.get(topic);
        if(senderList != null && !senderList.isEmpty()){
            for(int i=0; i<senderList.size(); i++){
                Sender cur = senderList.get(i);
                if(cur.getDestAddr().equals(destAddr)){
                    cur.stop();
                    senderList.remove(i--);
                }
            }
        }
    }

    public void send(String topic, String msg){
        ArrayList<Sender> senderList = senders.get(topic);
        if(senderList != null){
            for(Sender sender: senderList){
                sender.send(msg);
            }
        }
    }

    /**
     * To avoid lose any messages, publisher reconnect should always follows the following pattern:
     * 1. create a new publisher with new destination address
     * 2. wait for a proper amount of time for connection to be stable
     * 3. stop the old publisher with old destination address
     * In this way, reconnection will hopefully not lose any messages, but may have some duplication messages,
     * we could easily take care of duplicates whenever we want.
     * @param topic
     * @param oldDestAddr
     * @param newDestAddr
     */
    public void reconnect(String topic, String oldDestAddr, String newDestAddr){
        createSender(topic, newDestAddr);
        try{
            Thread.sleep(500);
        }catch (InterruptedException ie){
            logger.warn("interrupted during publisher reconfiguration! {}", ie.getMessage());
        }
        stopSender(topic, oldDestAddr);
    }

    public static void main(String[] args) throws Exception {
        String brokerIP = args[0];
        // hard coded config
        String pubID = "pub0";
        String pubIP = "129.59.105.148";
        String[] topics = new String[1];
        topics[0] = "t0";
        String[] destAddrs = new String[1];
        destAddrs[0] = brokerIP + ":5000";
        //destAddrs[0] = "129.59.105.148:5000";
        //destAddrs[0] = "127.0.0.1:5000";
        int[] sendIntervals = new int[1];
        sendIntervals[0] = 10;


        testBasic(pubID, pubIP, topics, destAddrs, sendIntervals);
    }

    static void testBasic(String pubID, String pubIP, String[] topics, String[] destAddrs, int[] sendIntervals) throws Exception{
        Publisher pub = new Publisher(pubID, pubIP);
        for(int i=0; i< topics.length; i++){
            pub.createSender(topics[i], destAddrs[i]);
        }
        for(int i=0; i<topics.length; i++){
            Thread t = new Thread(new SenderFeed(pub, topics[i], sendIntervals[i]));
            t.start();
        }
    }

}
