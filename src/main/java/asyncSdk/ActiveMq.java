package asyncSdk;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.qpid.amqp_1_0.jms.impl.QueueImpl;

import javax.jms.*;
import javax.jms.Message;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created By Khojasteh on 7/24/2019
 */
public class ActiveMq implements AsyncProvider {
    private static Logger logger = LogManager.getLogger(ActiveMq.class);
    private MessageProducer producer;
    private MessageConsumer consumer;

    /**
     * Producer Session
     */
    private Session proSession;

    /**
     * Consumer Session
     */
    private Session conSession;


    /**
     * Producer Connection
     */
    private Connection proConnection;

    /**
     * Consumer Connection
     */
    private Connection conConnection;
    private final Destination inputQueue;
    private final Destination outputQueue;
    private AsyncConfig config;
    private final AtomicBoolean reconnect = new AtomicBoolean(false);
    ConnectionFactory factory;
    AsyncProviderListener listener;

    public ActiveMq(AsyncConfig config, AsyncProviderListener listener) {
        this.listener = listener;
        this.config = config;
        inputQueue = new QueueImpl(config.getQueueInput());
        outputQueue = new QueueImpl(config.getQueueOutput());
        factory = new ActiveMQConnectionFactory(
                config.getQueueUserName(),
                config.getQueuePassword(),
                new StringBuilder()
                        .append("failover:(tcp://")
                        .append(config.getQueueServer())
                        .append(":")
                        .append(config.getQueuePort())
                        .append(")?jms.useAsyncSend=true")
                        .append("&jms.sendTimeout=").append(config.getQueueReconnectTime())
                        .toString());

        if (factory != null) {
            connect();
        } else {
            logger.error("An asyncSdk.exception occurred...");
        }
    }

    public void connect() {
        if (reconnect.compareAndSet(false, true)) {
            while (true) {
                try {
                    this.proConnection = factory.createConnection(
                            config.getQueueUserName(),
                            config.getQueuePassword());
                    proConnection.start();
                    this.conConnection = factory.createConnection(
                            config.getQueueUserName(),
                            config.getQueuePassword());
                    conConnection.start();
                    proSession = proConnection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
                    conSession = conConnection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
                    producer = proSession.createProducer(outputQueue);
                    consumer = conSession.createConsumer(inputQueue);
                    consumer.setMessageListener(new QueueMessageListener());
                    conConnection.setExceptionListener(new QueueExceptionListener());
                    proConnection.setExceptionListener(new QueueExceptionListener());
                    proConnection.setExceptionListener(new QueueExceptionListener());
                    logger.info("connection established");
                    break;

                } catch (JMSException exception) {
                    logger.error("Reconnecting asyncSdk.exception");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e1) {
                        logger.error(e1);
                    }
                    close();
                }
            }
            reconnect.set(false);
        }
    }

    @Override
    public void send(String messageWrapperVO) {
        try {
            byte[] bytes = messageWrapperVO.getBytes(StandardCharsets.UTF_8);
            BytesMessage bytesMessage = proSession.createBytesMessage();
            bytesMessage.writeBytes(bytes);
            producer.send(bytesMessage);
        } catch (Exception e) {
            logger.error("An asyncSdk.exception in sending message" + e);
        }
    }

    public void shutdown() throws JMSException {
        this.conConnection.close();
        this.proConnection.close();
        this.conSession.close();
        this.proSession.close();
    }

    private class QueueMessageListener implements MessageListener {
        @Override
        public void onMessage(Message message) {
            try {
                message.acknowledge();
                if (message instanceof BytesMessage) {
                    BytesMessage bytesMessage = (BytesMessage) message;
                    byte[] buffer = new byte[(int) bytesMessage.getBodyLength()];
                    int readBytes = bytesMessage.readBytes(buffer);
                    if (readBytes != bytesMessage.getBodyLength()) {
                        throw new IOException("Inconsistent message length");
                    }
                    String json = new String(buffer, StandardCharsets.UTF_8);
                    listener.onMessage(json);
                }
            } catch (JMSException s) {
                try {
                    throw s;
                } catch (JMSException e) {
                    showErrorLog("jms Exception: " + e);
                }
            } catch (Throwable e) {
                showErrorLog("An asyncSdk.exception occurred: " + e);
            }
        }
    }

    private class QueueExceptionListener implements ExceptionListener {
        @Override
        public void onException(JMSException exception) {
            close();
            showErrorLog("JMSException occurred: " + exception);
            try {
                Thread.sleep(config.getQueueReconnectTime());
                connect();
            } catch (InterruptedException e) {
                showErrorLog("An asyncSdk.exception occurred: " + e);
            }
        }
    }

    public void close() {
        try {
            producer.close();
            consumer.close();
            proSession.close();
            conSession.close();
            conConnection.close();
            proConnection.close();
        } catch (JMSException e) {
            listener.onError(new Exception("An asyncSdk.exception occurred at closing"));
        }
    }

    private void showErrorLog(String e) {
        if (config.isLoggable()) logger.error("\n \n" + e);
    }
}
