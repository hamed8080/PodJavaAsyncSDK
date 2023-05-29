package asyncSdk;

import asyncSdk.model.*;
import com.google.gson.Gson;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("unused")
@ClientEndpoint
public class SocketProvider implements AsyncProvider {
    private final AsyncConfig config;
    private Session session;
    private final AsyncProviderListener listener;
    private final Gson gson = new Gson();
    private String deviceId;
    private boolean isServerRegistered;
    private Integer peerId;

    public SocketProvider(AsyncConfig config, AsyncProviderListener listener) {
        this.config = config;
        this.listener = listener;
    }

    public void connect() {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.setDefaultMaxTextMessageBufferSize(10*1024*1024);
        try {
            session = container.connectToServer(this, new URI(config.getSocketAddress()));
            onOpen(session);
        } catch (DeploymentException | IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(String message) {
        session.getAsyncRemote().sendText(message);
    }

    @Override
    public void close() {
        listener.onClose();
    }

    @OnOpen
    public void onOpen(Session session) {
        listener.onOpen();
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        listener.onMessage(message);
        AsyncMessage asyncMessage = gson.fromJson(message, AsyncMessage.class);
        AsyncMessageType type = asyncMessage.getType();
        prepareTimerForNextPing();
        switch (type) {
            case Ping:
                onPingMessage(asyncMessage);
                break;
            case ServerRegister:
                onServerRegisteredMessage(asyncMessage);
                break;
            case DeviceRegister:
                onDeviceRegisteredMessage(asyncMessage);
                break;
        }
    }

    private void prepareTimerForNextPing() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendPing();
            }
        }, 10);
    }

    private void sendPing() {
        AsyncMessage asyncMessage = new AsyncMessage();
        asyncMessage.setType(AsyncMessageType.Ping);
        String message = gson.toJson(asyncMessage);
        send(message);
    }

    private void registerDevice() {
        String deviceId = this.deviceId == null ? "" : this.deviceId;
        RegisterDevice register = peerId == null ? new RegisterDevice(true, config.getAppId(), deviceId) : new RegisterDevice(config.getAppId(), true, deviceId);
        String asyncString = getMessageWrapper(AsyncMessageType.DeviceRegister, gson.toJson(register));
        send(asyncString);
    }

    private void registerServer() {
        RegisterServer register = new RegisterServer(config.getServerName());
        String asyncString = getMessageWrapper(AsyncMessageType.ServerRegister, gson.toJson(register));
        send(asyncString);
    }

    private String getMessageWrapper(AsyncMessageType serverRegister, String content) {
        if (content != null) {
            AsyncMessage asyncMessage = new AsyncMessage();
            asyncMessage.setContent(content);
            asyncMessage.setType(serverRegister);
            return gson.toJson(asyncMessage);
        } else {
            return null;
        }
    }

    @OnClose
    public void close(Session session, CloseReason reason) {
        listener.onClose();
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        listener.onError(new Exception(throwable.getCause().getMessage()));
    }

    private void onPingMessage(AsyncMessage asyncMessage) {
        if (asyncMessage.getContent() != null) {
            if (deviceId == null) {
                deviceId = asyncMessage.getContent();
            }
            registerDevice();
        }
    }

    private void onServerRegisteredMessage(AsyncMessage asyncMessage) {
        if (asyncMessage.getSenderName().equals(config.getServerName())) {
            isServerRegistered = true;
            listener.onSocketReady();
        } else {
            registerServer();
        }
    }

    private void onDeviceRegisteredMessage(AsyncMessage asyncMessage) {
        Integer oldPeerId = peerId;
        if (asyncMessage.getContent() != null) {
            peerId = Integer.parseInt(asyncMessage.getContent());
        }

        if (isServerRegistered && peerId.equals(oldPeerId)) {
            listener.onSocketReady();
        } else {
            registerServer();
        }
    }
}
