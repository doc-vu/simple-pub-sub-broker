package edu.vanderbilt.vudoc.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeromq.ZMQ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by Killian on 7/24/17.
 */
public class Subscriber{
    private String myID;
    private String myIP;
    private ZMQ.Context zmqContext;
    // key: topic, value: a list of senders corresponding to this topic
    private HashMap<String, ArrayList<Receiver>> receivers = new HashMap<>();
    // msg buffer
    private BlockingQueue<String> msgBuffer = new PriorityBlockingQueue<>();

    // logger config
    private static final Logger logger = LogManager.getLogger(Subscriber.class.getName());
    private static final Logger resLogger = LogManager.getLogger("TestResult");

    public Subscriber(String myID, String myIP){
        this.myID = myID;
        this.myIP = myIP;
        this.zmqContext = ZMQ.context(1);
    }

    public void createReceiver(String topic, String srcAddr){
        Receiver newReceiver = new Receiver(topic, srcAddr, msgBuffer, zmqContext);
        ArrayList<Receiver> receiverList = receivers.getOrDefault(topic, new ArrayList<>());
        receiverList.add(newReceiver);
        receivers.put(topic, receiverList);
        Thread t = new Thread(newReceiver);
        t.start();
        logger.debug("New Sender Created. topic: {}, srcAddr: {}", topic, srcAddr);
    }

    public void stopReceiver(String topic, String srcAddr){
        ArrayList<Receiver> receiverList = receivers.get(topic);
        if(receiverList != null && !receiverList.isEmpty()){
            for(int i=0; i<receiverList.size(); i++){
                Receiver cur = receiverList.get(i);
                if(cur.getSrcAddr().equals(srcAddr)){
                    cur.stop();
                    receiverList.remove(i--);
                }
            }
        }
    }

    /**
     * To avoid lose any messages, subscriber reconnect should always follows the following pattern:
     * 1. create a new receiver with new source address
     * 2. wait for a proper amount of time for connection to be stable
     * 3. stop the old receiver with old source address
     * In this way, reconnection will hopefully not lose any messages, but may have some duplication messages,
     * we could easily take care of duplicates whenever we want.
     * @param topic
     * @param oldSrcAddr
     * @param newSrcAddr
     */
    public void reconnect(String topic, String oldSrcAddr, String newSrcAddr){
        createReceiver(topic, newSrcAddr);
        try{
            Thread.sleep(500);
        }catch (InterruptedException ie){
            logger.warn("interrupted during subscriber reconfiguration! {}", ie.getMessage());
        }
        stopReceiver(topic, oldSrcAddr);
    }

    public BlockingQueue<String> getMsgBuffer() {
        return msgBuffer;
    }

    public String getMyID() {
        return myID;
    }

    public ArrayList<String> getCleanedBuffer(boolean removeDup){
        BlockingQueue<String> buff = getMsgBuffer();
        ArrayList<String> res = new ArrayList<>();
        try{
            while(!buff.isEmpty()){
                res.add(buff.take());
            }
        }catch(Exception e){

        }
        res.sort((String m1, String m2)->{
            String[] data1 = m1.split(",");
            String[] data2 = m2.split(",");
            String t1 = data1[0];
            String t2 = data2[0];
            String pubID1 = data1[1];
            String pubID2 = data2[1];
            String msgID1 = data1[2];
            String msgID2 = data2[2];
            if(!t1.equals(t2)){
                return t1.compareTo(t2);
            }else if(!pubID1.equals(pubID2)){
                return pubID1.compareTo(pubID2);
            }else{
                return Integer.compare(Integer.parseInt(msgID1), Integer.parseInt(msgID2));
            }
        });
        if(removeDup){
            // remove dups
            if(res.size() <= 1) return res;
            String prev = res.get(0);
            for(int i=1; i<res.size(); i++){
                String cur = res.get(i);
                if(cur.equals(prev)) res.remove(i--);
                prev = cur;
            }
        }
        return res;
    }

    public static void main(String[] args) throws Exception {
        String brokerIP = args[0];
        // hard coded config
        String subID = "sub0";
        String subIP = "129.59.105.128";
        String[] topics = new String[1];
        topics[0] = "t0";
        String[] srcAddrs = new String[1];
        srcAddrs[0] = brokerIP + ":6000";
        //srcAddrs[0] = "129.59.105.148:6000";
        //srcAddrs[0] = "127.0.0.1:6000";

        testBasic(subID, subIP, topics, srcAddrs);
    }
    static void testBasic(String subID, String subIP, String[] topics, String[] srcAddrs) throws Exception{
        Subscriber sub = new Subscriber(subID, subIP);
        for(int i=0; i< topics.length; i++){
            sub.createReceiver(topics[i], srcAddrs[i]);
        }
    }
}
