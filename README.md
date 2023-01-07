# demo project for JBOSS EAP 7.3.X ActiveMQ Artemis Core API


## versions

https://github.com/apache/activemq-artemis

https://access.redhat.com/articles/112673#EAP_7

JBOSS EAP 7.3.X = ActiveMQ Artemis 2.9.0  
JBOSS EAP 7.4.X = ActiveMQ Artemis 2.16.0


## start jboss eap using configuration "standalone full" to that ActiveMQ is available

C:\Data\jboss-eap-7.3\bin>standalone.bat --server-config=standalone-full.xml


## create a queue using jboss cli

C:\Data\jboss-eap-7.3\bin> jboss-cli.bat

connect

jms-queue add --queue-address=TestQueue --entries=java:/jms/queue/TestQueue

reload


## standalone-full.xml should now contain the queue definition

<jms-queue name="TestQueue" entries="java:/jms/queue/TestQueue"/>


## use jboss cli to read queue information

C:\Data\jboss-eap-7.3\bin> jboss-cli.bat

connect

/subsystem=messaging-activemq/server=default/runtime-queue=*:read-resource


## to send a message, open this url in a web browser

http://localhost:8080/activemq-1.0-SNAPSHOT/api/v1/queue/send2


## use jboss cli to remove the queue

C:\Data\jboss-eap-7.3\bin> jboss-cli.bat

connect

jms-queue remove --queue-address=TestQueue


## ActiveMQ Artemis docs

https://activemq.apache.org/components/artemis/documentation/

Apache ActiveMQ Artemis core is a messaging system with its own API. We call this the core API.

If you don't want to use the JMS API or any of the other supported protocols you can use the core API directly. The core API provides all the functionality of JMS but without much of the complexity. It also provides features that are not available using JMS.

Some of the core messaging concepts are similar to JMS concepts, but core messaging concepts are also different in some ways as well. In general the core API is simpler than the JMS API, since we remove distinctions between queues, topics and subscriptions. 

...

ClientSession, ClientProducer and ClientConsumer instances are designed to be re-used.

It's an anti-pattern to create new ClientSession, ClientProducer and ClientConsumer instances for each message you produce or consume. If you do this, your application will perform very poorly.

...

While it is possible to configure a JMS topic and queue with the same name, it is not a recommended configuration for use with cross protocol.

...

Apache ActiveMQ Artemis supports using a single port for all protocols, Apache ActiveMQ Artemis will automatically detect which protocol is being used CORE, AMQP, STOMP, MQTT or OPENWIRE and use the appropriate Apache ActiveMQ Artemis handler. It will also detect whether protocols such as HTTP or Web Sockets are being used and also use the appropriate decoders. Web Sockets are supported for AMQP, STOMP, and MQTT.

...

