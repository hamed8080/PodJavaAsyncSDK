package asyncSdk;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import asyncSdk.model.*;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public final class Async implements AsyncProviderListener {
    private static final Logger logger = LogManager.getContext().getLogger("SDK_LOGGER");
    private static AsyncProvider provider;
    private static final String TAG = "asyncSdk.Async" + " ";
    static final Gson gson = new Gson();
    private AsyncState state;
    private final AsyncConfig config;
    private AsyncListener listener;
    private Timer connectionCheckTimer;
    private Date lastReceivedMessage;
    private Long reconnectCount = 1L;

    public Async(AsyncConfig config) {
        this.config = config;
        if (config.isSocketProvider()) {
            provider = new SocketProvider(config, this);
        } else {
            provider = new ActiveMQProvider(config, this);
        }
    }

    @SuppressWarnings("unused")
    @Override
    public void onOpen() {
        if (config.isSocketProvider()) {
            setState(AsyncState.Connected);
        } else {
            setState(AsyncState.AsyncReady);
        }
    }

    @SuppressWarnings("unused")
    @Override
    public void onClose() {
        setState(AsyncState.Closed);
    }

    @SuppressWarnings("unused")
    @Override
    public void onSocketReady() {
        setState(AsyncState.AsyncReady);
    }

    /**
     * @param textMessage that received when socket send message to asyncSdk.Async
     */
    @SuppressWarnings("unused")
    @Override
    public void onMessage(String textMessage) {
        lastReceivedMessage = new Date();
        AsyncMessage asyncMessage = gson.fromJson(textMessage, AsyncMessage.class);
        if (connectionCheckTimer == null) {
            scheduleConnectionTimer();
        }
        AsyncMessageType type = asyncMessage.getType();
        logger.info("ASYNC_SDK " + "On ReceiveMessage: " + type.toString()  + "\n" + textMessage + "\n");
        switch (type) {
            case Ack:
                handleOnAck(asyncMessage);
                break;
            case ErrorMessage:
                handleOnErrorMessage(asyncMessage);
                break;
            case MessageAckNeeded:
            case MessageSenderAckNeeded:
                handleOnMessageAckNeeded(asyncMessage);
                break;
            case Message:
                handleOnMessage(asyncMessage);
                break;
            case PeerRemoved:
                break;
        }
    }

    private void scheduleConnectionTimer() {
        if (provider instanceof ActiveMQProvider) {return;}
        stopTimer();
        reconnectCount = 1L;
        connectionCheckTimer = new Timer();
        connectionCheckTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                reconnectIfPossible();
            }
        }, config.getReconnectInterval(), config.getReconnectInterval());
    }

    private void reconnectIfPossible() {
        long triggerTime = lastReceivedMessage.getTime() + config.getCheckConnectionLastMessageInterval();
        boolean isPastTriggerTime = triggerTime < new Date().getTime();
        if (reconnectCount <= config.getMaxReconnectCount() && isPastTriggerTime && state != AsyncState.AsyncReady) {
            reconnect();
        } else if (reconnectCount > config.getMaxReconnectCount()) {
            stopTimer();
            listener.onStateChanged(AsyncState.Closed, this);
        }
    }

    private void reconnect() {
        try {
            logger.info("Reconnecting for " + reconnectCount + " times");
            reconnectCount++;
            connect();
        } catch (Exception e) {
            logger.error("Error on reconnecting ", e);
        }
    }

    private void stopTimer() {
        if (connectionCheckTimer != null) {
            connectionCheckTimer.cancel();
            connectionCheckTimer = null;
        }
    }

    @SuppressWarnings("unused")
    @Override
    public void onError(Exception exception) {
        logger.info("Error is : " + exception);
    }

    @SuppressWarnings("unused")
    public void connect() throws Exception {
        setState(AsyncState.Connecting);
        provider.connect();
    }

    /**
     * First we are checking the state of the socket then we send the message
     */
    @SuppressWarnings("unused")
    public void sendMessage(String textContent, AsyncMessageType messageType, long[] receiversId) {
        try {
            if (state == AsyncState.AsyncReady) {
                long ttl = new Date().getTime();
                Message message = new Message();
                message.setContent(textContent);
                message.setPriority(1);
                message.setPeerName(config.getServerName());
                message.setTtl(ttl);
                message.setReceivers(receiversId);
                String messageContent = gson.toJson(message);
                AsyncMessage asyncMessage = new AsyncMessage();
                asyncMessage.setContent(messageContent);
                asyncMessage.setType(messageType);
                String json = gson.toJson(asyncMessage);
                provider.send(json);
                logger.info("ASYNC_SDK Send With type " + messageType + ": \n" + json  + "\n");
            } else {
                showErrorLog(TAG + "Socket is not connected");
            }
        } catch (Exception e) {
            listener.onError(e);
            if (e.getMessage() != null) {
                showErrorLog("asyncSdk.Async: connect", e.getMessage());
            }
        }
    }

    private void handleOnAck(AsyncMessage asyncMessage) {
        listener.onReceivedMessage(asyncMessage);
    }

    @SuppressWarnings("all")
    private void sendInternalMessage(String message, AsyncMessageType type) {
        AsyncMessage asyncMessage = new AsyncMessage();
        asyncMessage.setContent(message);
        asyncMessage.setType(type);
        String json = gson.toJson(asyncMessage);
        provider.send(json);
        logger.info("Send an internal Message " + json);
    }

    private void handleOnErrorMessage(AsyncMessage asyncMessage) {
        showErrorLog(TAG + "OnErrorMessage", asyncMessage.getContent());
    }

    private void handleOnMessage(AsyncMessage asyncMessage) {
        listener.onReceivedMessage(asyncMessage);
    }

    private void handleOnMessageAckNeeded(AsyncMessage asyncMessage) {
        try {
            if (provider != null) {
                handleOnMessage(asyncMessage);
                Message messageSenderAckNeeded = new Message();
                messageSenderAckNeeded.setMessageId(asyncMessage.getId());
                sendInternalMessage(gson.toJson(messageSenderAckNeeded), AsyncMessageType.Ack);
            } else {
                showErrorLog("WebSocket Is Null ");
            }
        } catch (Exception e) {
            showErrorLog(e.getCause().getMessage());
        }
    }

    @SuppressWarnings("unused")
    public AsyncState getState() {
        return state;
    }

    private void setState(AsyncState state) {
        this.state = state;
        listener.onStateChanged(this.state, this);
    }

    private void showErrorLog(String i, String json) {
        if (config.isLoggable()) logger.error(i + "\n \n" + json);
    }

    private void showErrorLog(String e) {
        if (config.isLoggable()) logger.error("\n \n" + e);
    }

    @SuppressWarnings("unused")
    public void setListener(AsyncListener listener) {
        this.listener = listener;
    }
}