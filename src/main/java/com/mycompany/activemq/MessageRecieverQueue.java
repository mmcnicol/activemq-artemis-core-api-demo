package com.mycompany.activemq;

import java.util.Iterator;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.apache.activemq.artemis.api.core.ActiveMQBuffer;
import org.apache.activemq.artemis.api.core.ActiveMQBuffers;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.Message;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.MessageHandler;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.core.protocol.core.Packet;
import org.apache.activemq.artemis.core.protocol.core.impl.wireformat.SessionReceiveMessage;
import org.apache.activemq.artemis.spi.core.protocol.RemotingConnection;
import org.jboss.logging.Logger;

//@ApplicationScoped
@Singleton
@Startup
public class MessageRecieverQueue {

    private final Logger log = Logger.getLogger(MessageRecieverQueue.class);
    private ServerLocator locator;
    private ClientSessionFactory factory;
    private ClientSession session;
    private ClientConsumer consumer;
    //private ClientProducer producer;

    @PostConstruct
    public void init() {
        log.info("MessageRecieverQueue in init()");
        locator = null;
        factory = null;
        session = null;
        consumer = null;
        //producer = null;

        try {
            // tcp://localhost:5445
            // tcp://remote-host:5445?ssl-enabled=true
            // tcp://localhost:61617
            // The default value for this property is 61616
            locator = ActiveMQClient.createServerLocator("vm://0");
            locator.addIncomingInterceptor((Packet p, RemotingConnection rc) -> {
                //p.isRequiresConfirmations();
                //System.out.format("IncomingInterceptor isRequiresConfirmations: %b \n", p.isRequiresConfirmations());
                //System.out.format("IncomingInterceptor getClass().getName(): %s \n", p.getClass().getName());
                //Subject subject = rc.getSubject();
                //System.out.format("IncomingInterceptor subject: \n", subject);
                /*
                if (p instanceof SessionSendMessage) {
                    SessionSendMessage realPacket = (SessionSendMessage) p;
                    Message msg = realPacket.getMessage();
                    //msg.putStringProperty(new SimpleString("newproperty"), new SimpleString("Hello from interceptor!"));
                    System.out.println("IncomingInterceptor message: " + msg.getBodyBuffer().readString());
                }
                 */
                if (p instanceof SessionReceiveMessage) {
                    SessionReceiveMessage realPacket = (SessionReceiveMessage) p;
                    Message msg = realPacket.getMessage();
                    //msg.putStringProperty(new SimpleString("newproperty"), new SimpleString("Hello from interceptor!"));
                    log.infof("MessageRecieverQueue IncomingInterceptor message: >>%s<<", msg.getBodyBuffer().readString());
                }
                // We return true which means "call next interceptor" (if there is one) or target.
                // If we returned false, it means "abort call" - no more interceptors would be called and neither would
                // the target
                return true;
                /*
                The returned boolean value is important:
                if true is returned, the process continues normally
                //if false is returned, the process is aborted, no other interceptors will be called and the packet will not be processed further by the server.
                if an interceptor returns false then the sending of the packet is aborted which means that no other interceptors are be called and the packet is not be processed further by the client.
                 */
            });

            locator.addOutgoingInterceptor((Packet p, RemotingConnection rc) -> {
                //System.out.format("OutgoingInterceptor isRequiresConfirmations: \n", p.isRequiresConfirmations());
                //System.out.format("OutgoingInterceptor isResponse(): %b \n", p.isResponse());
                log.infof("OutgoingInterceptor getClass().getName(): %s \n", p.getClass().getName());
                return true;
            });
            factory = locator.createSessionFactory();
            session = factory.createSession();
            log.infof("MessageHandler onMessage session isAutoCommitAcks: %b", session.isAutoCommitAcks());
            consumer = session.createConsumer("jms.queue.TestQueue");
            //producer = session.createProducer("jms.queue.TestQueue");
            session.start();

            consumer.setMessageHandler(new MessageHandler() {
                @Override
                public void onMessage(ClientMessage clientMessage) {
                    try {
                        ActiveMQBuffer buffer = clientMessage.getDataBuffer();
                        ActiveMQBuffer result = ActiveMQBuffers.fixedBuffer(clientMessage.getBodyBufferSize());
                        buffer.readBytes(result);
                        //String version = result.readString();
                        String surname = result.readString();
                        String forename = result.readNullableString();
                        //log.infof("MessageRecieverQueue MessageHandler onMessage \nversion: >>%s<<", version);
                        log.infof("MessageRecieverQueue MessageHandler onMessage \nsurname: >>%s<<", surname);
                        log.infof("MessageRecieverQueue MessageHandler onMessage \nforename: >>%s<<", forename);
                        Set<SimpleString> properties = clientMessage.getPropertyNames();
                        Iterator<SimpleString> newData = properties.iterator();
                        log.info("MessageRecieverQueue MessageHandler onMessage \nproperties: ");
                        while (newData.hasNext()) {
                            log.info(newData.next());
                        }
                        String version = clientMessage.getStringProperty("version");
                        String subject = clientMessage.getStringProperty("subject");
                        log.infof("MessageRecieverQueue MessageHandler onMessage property\nversion: >>%s<<", version);
                        log.infof("MessageRecieverQueue MessageHandler onMessage property\nsubject: >>%s<<", subject);
                        clientMessage.acknowledge();
                        //producer.send(clientMessage.acknowledge());
                        session.commit();

                    } catch (Exception e) {
                        log.errorf("MessageRecieverQueue caught exception: %s\n", e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            log.errorf("MessageRecieverQueue caught exception: %s\n", e.getMessage());
        } finally {

        }
        log.info("MessageRecieverQueue in init() END");
    }

    @PreDestroy
    public void preDestroy() {
        log.info("MessageRecieverQueue in preDestroy()");

        if (consumer != null) {
            try {
                consumer.close();
            } catch (ActiveMQException e) {
                log.errorf("MessageRecieverQueue caught exception: %s\n", e.getMessage());
            }
        }
        /*
        if (producer != null) {
            try {
                producer.close();
            } catch (ActiveMQException e) {
                log.errorf("MessageRecieverQueue caught exception: %s\n", e.getMessage());
            }
        }
         */
        if (session != null) {
            try {
                session.close();
            } catch (ActiveMQException e) {
                log.errorf("MessageRecieverQueue caught exception: %s\n", e.getMessage());
            }
        }
        if (factory != null) {
            factory.close();
        }

        if (locator != null) {
            locator.close();
        }
    }

}
