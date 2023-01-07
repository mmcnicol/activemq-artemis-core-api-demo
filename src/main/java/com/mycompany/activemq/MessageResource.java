package com.mycompany.activemq;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.jboss.logging.Logger;

@Path("v1")
public class MessageResource {

    private final Logger log = Logger.getLogger(MessageResource.class);

    @GET
    @Path("/queue/send2")
    @Produces("application/json")
    public Response queueSend2() {
        log.info("MessageResource in queueSend2()");
        ServerLocator locator = null;
        ClientSessionFactory factory = null;
        ClientSession session = null;
        try {
            locator = ActiveMQClient.createServerLocator("vm://0");
            factory = locator.createSessionFactory();
            session = factory.createSession();

            //session.createQueue("testQueue", RoutingType.ANYCAST, "testQueue", true);
            
            // You can't set confirmationHandler on a connection with confirmation-window-size < 0. Look at the documentation for more information.
            /*
            session.setSendAcknowledgementHandler((Message message) -> {
                log.info("Received send acknowledgement for a message");
            });
            */
            
            ClientProducer producer = session.createProducer("jms.queue.TestQueue");
            ClientMessage message = session.createMessage(true);
            //message.getBodyBuffer().writeNullableSimpleString(new SimpleString("Hello Queue"));
            //message.getBodyBuffer().writeString("1"); // version
            message.getBodyBuffer().writeString("smith"); // surname
            message.getBodyBuffer().writeNullableString("tim"); // forename
            message.putStringProperty("version", "1");
            message.putStringProperty("subject", "test123");

            producer.send(message);
            log.info("MessageResource in queueSend() SENT");
            session.commit();
            
            ResponseBuilder response = Response.ok();
            return response.build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (ActiveMQException e) {
                    log.errorf("MessageResource caught exception: %s\n", e.getMessage());
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

}
