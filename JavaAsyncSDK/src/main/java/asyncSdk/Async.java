package asyncSdk;

import com.google.gson.Gson;
import asyncSdk.exception.ConnectionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import asyncSdk.model.*;
import java.util.Date;

public class Async implements AsyncProviderListener {
    private static final Logger logger = LogManager.getLogger(Async.class);
    private static AsyncProvider provider;
    private static final String TAG = "asyncSdk.Async" + " ";
    private static Async instance;
    private MessageWrapperVo messageWrapperVo;
    private static Gson gson;
    private AsyncState state;
    private final AsyncConfig config;
    private AsyncListener listener;

    private Async(AsyncConfig config) {
        this.config = config;
        if (config.isSocketProvider()) {
            provider = new SocketProvider(config, this);
        } else {
            provider = new ActiveMq(config, this);
        }
    }

    public static Async getInstance(AsyncConfig config) {
        if (instance == null) {
            gson = new Gson();
            instance = new Async(config);
        }
        return instance;
    }

    @Override
    public void onOpen() {
        if (config.isSocketProvider()) {
            setState(AsyncState.Connected);
        } else {
            setState(AsyncState.AsyncReady);
        }
    }

    @Override
    public void onClose() {
        setState(AsyncState.Closed);
    }

    @Override
    public void onSocketReady() {
        setState(AsyncState.AsyncReady);
    }

    /**
     * @param textMessage that received when socket send message to asyncSdk.Async
     */

    @Override
    public void onMessage(String textMessage) {
        ClientMessage clientMessage = gson.fromJson(textMessage, ClientMessage.class);
        AsyncMessageType type = clientMessage.getType();
        switch (type) {
            case Ack:
                handleOnAck(clientMessage);
                break;
            case ErrorMessage:
                handleOnErrorMessage(clientMessage);
                break;
            case MessageAckNeeded:
            case MessageSenderAckNeeded:
                handleOnMessageAckNeeded(clientMessage);
                break;
            case Message:
                handleOnMessage(clientMessage);
                break;
            case PeerRemoved:
                break;
        }
    }

    @Override
    public void onError(Exception exception) {

    }

    public void connect() throws ConnectionException {
        setState(AsyncState.Connecting);
        provider.connect();
    }

    /**
     * @Param textContent
     * @Param messageType it could be 3, 4, 5
     * @Param []receiversId the Id's that we want to send
     */
    public void sendMessage(String textContent, AsyncMessageType messageType, long[] receiversId) {
        try {
            Message message = new Message();
            message.setContent(textContent);
            message.setReceivers(receiversId);
            String jsonMessage = gson.toJson(message);
            String wrapperJsonString = getMessageWrapper(gson, jsonMessage, messageType);
            sendData(wrapperJsonString);
        } catch (Exception e) {
            listener.onError(e);
            showErrorLog("asyncSdk.Async: connect", e.getCause().getMessage());
        }
    }

    /**
     * First we checking the state of the socket then we send the message
     */
    public void sendMessage(String textContent, AsyncMessageType messageType) {
        try {
            if (state == AsyncState.AsyncReady) {
                long ttl = new Date().getTime();
                Message message = new Message();
                message.setContent(textContent);
                message.setPriority(1);
                message.setPeerName(config.getServerName());
                message.setTtl(ttl);
                String json = gson.toJson(message);
                messageWrapperVo = new MessageWrapperVo();
                messageWrapperVo.setContent(json);
                messageWrapperVo.setType(messageType);
                String json1 = gson.toJson(messageWrapperVo);
                sendData(json1);
            } else {
                showErrorLog(TAG + "Socket Is Not Connected");
            }
        } catch (Exception e) {
            listener.onError(e);
            showErrorLog("asyncSdk.Async: connect", e.getCause().getMessage());
        }
    }

    /**
     * Connect webSocket to the asyncSdk.Async
     *
     * @Param socketServerAddress
     * @Param appId
     */
    private void handleOnAck(ClientMessage clientMessage) {
        listener.onReceivedMessage(clientMessage.getContent());
    }

    private void sendData(String jsonMessageWrapperVo) {
        provider.send(jsonMessageWrapperVo);
    }

    private void handleOnErrorMessage(ClientMessage clientMessage) {
        showErrorLog(TAG + "OnErrorMessage", clientMessage.getContent());
    }

    private void handleOnMessage(ClientMessage clientMessage) {
        listener.onReceivedMessage(clientMessage.getContent());
    }

    private void handleOnMessageAckNeeded(ClientMessage clientMessage) {
        try {
            if (provider != null) {
                handleOnMessage(clientMessage);
                Message messageSenderAckNeeded = new Message();
                messageSenderAckNeeded.setMessageId(clientMessage.getId());
                String jsonSenderAckNeeded = gson.toJson(messageSenderAckNeeded);
                String jsonSenderAckNeededWrapper = getMessageWrapper(gson, jsonSenderAckNeeded, AsyncMessageType.Ack);
                sendData(jsonSenderAckNeededWrapper);
            } else {
                showErrorLog("WebSocket Is Null ");
            }
        } catch (Exception e) {
            showErrorLog(e.getCause().getMessage());
        }
    }

    private String getMessageWrapper(Gson gson, String json, AsyncMessageType messageType) {
        try {
            messageWrapperVo = new MessageWrapperVo();
            messageWrapperVo.setContent(json);
            messageWrapperVo.setType(messageType);
            return gson.toJson(messageWrapperVo);
        } catch (Exception e) {
            showErrorLog(e.getCause().getMessage());
        }
        return null;
    }

    public AsyncState getState() {
        return state;
    }

    private void setState(AsyncState state) {
        this.state = state;
        listener.onStateChanged(this.state);
    }

    private void showErrorLog(String i, String json) {
        if (config.isLoggable()) logger.error(i + "\n \n" + json);
    }

    private void showErrorLog(String e) {
        if (config.isLoggable()) logger.error("\n \n" + e);
    }

    public void setListener(AsyncListener listener) {
        this.listener = listener;
    }
}

