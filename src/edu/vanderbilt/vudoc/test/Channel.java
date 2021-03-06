package edu.vanderbilt.vudoc.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;


/**
 * Created by Killian on 7/24/17.
 */
public class Channel implements Runnable {
    // channel attrs
    private String topic;
    private String myIP;
    private String recPort;
    private String sendPort;
    private volatile boolean stop = false;

    // zmq config
    private ZMQ.Context zmqContext;
    private ZMQ.Socket sendSocket;
    private ZMQ.Socket recSocket;

    // logger config
    private static final Logger logger = LogManager.getLogger(Channel.class.getName());

    public Channel(String topic, String myIP, String recPort, String sendPort, ZMQ.Context zmqContext){
        this.topic = topic;
        this.myIP = myIP;
        this.recPort = recPort;
        this.sendPort = sendPort;

        // init zmq socket
        this.zmqContext = zmqContext;
        this.recSocket = zmqContext.socket(ZMQ.SUB);
        this.sendSocket = zmqContext.socket(ZMQ.PUB);
    }

    @Override
    public void run(){
        recSocket.bind("tcp://*:" + recPort);
        recSocket.subscribe(topic.getBytes());
        sendSocket.bind("tcp://*:" + sendPort);
        logger.info("Channel Started. topic: {}", topic);
        try{
            while(!stop){
                // rec 1 msg
                ZMsg receivedMsg = ZMsg.recvMsg(recSocket);
                String msgTopic = new String(receivedMsg.getFirst().getData());
                byte[] msgContent = receivedMsg.getLast().getData();
                // send it out
                sendSocket.sendMore(msgTopic);
                sendSocket.send(msgContent);
                // log
                logger.info("Message Transmitted. topic: {} content: {}", msgTopic, msgContent);
            }
        }catch(Exception e){
            logger.debug("Exception: {}",e.getMessage());
        }finally {
            recSocket.close();
            sendSocket.close();
            logger.debug("Channel Stopped. topic: {}", topic);
        }
    }

    public void stop(){
        stop = true;
        recSocket.close();
        sendSocket.close();
    }
}
