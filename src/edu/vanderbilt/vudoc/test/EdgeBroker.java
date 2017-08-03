package edu.vanderbilt.vudoc.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeromq.ZMQ;

import java.util.HashMap;

/**
 * Created by Killian on 7/24/17.
 */
public class EdgeBroker {
    private String myID;
    private String myIP;
    private ZMQ.Context zmqContext;
    // key: topic, value: msg channel for this topic
    private HashMap<String, Channel> channels = new HashMap<>();

    // logger config
    private static final Logger logger = LogManager.getLogger(EdgeBroker.class.getName());

    public EdgeBroker(String myID, String myIP) {
        this.myID = myID;
        this.myIP = myIP;

        zmqContext = ZMQ.context(1);
    }

    public void createChannel(String topic, String recPort, String sendPort) {
        Channel newChannel = new Channel(topic, myIP, recPort, sendPort, zmqContext);
        channels.put(topic, newChannel);
        Thread t = new Thread(newChannel);
        t.start();
        logger.debug("New Channel Created. topic: {}", topic);
    }

    public void closeChannel(String topic) {
        Channel channel = channels.get(topic);
        if (channel != null) channel.stop();
        channels.remove(topic);
    }

    public static void main(String[] args) throws Exception {
        // hard coded config
        String brokerID = "broker0";
        String brokerIP = "10.0.0.1";
        String[] topics = new String[1];
        topics[0] = "t0";
        String[] recPorts = new String[1];
        recPorts[0] = "5000";
        String[] sendPorts = new String[1];
        sendPorts[0] = "6000";
        testBasic(brokerID, brokerIP, topics, recPorts, sendPorts);
    }


    static void testBasic(String brokerID, String brokerIP, String[] topics, String[] recPorts, String[] sendPorts) throws Exception {
        EdgeBroker broker = new EdgeBroker(brokerID, brokerIP);
        for (int i = 0; i < topics.length; i++) {
            broker.createChannel(topics[i], recPorts[i], sendPorts[i]);
        }
    }
}