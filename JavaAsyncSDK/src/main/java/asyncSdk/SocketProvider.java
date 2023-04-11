package asyncSdk;

import com.google.gson.Gson;
import asyncSdk.model.*;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


@ClientEndpoint
public class SocketProvider implements AsyncProvider {
    private final AsyncConfig config;
    private Session session;
    private AsyncProviderListener listener;
    private final Gson gson = new Gson();
    private String deviceId;
    private boolean isServerRegistered;
    private boolean isDeviceRegistered;
    private Integer peerId;
    private ClientMessage clientMessage;

    public SocketProvider(AsyncConfig config, AsyncProviderListener listener) {
        this.config = config;
        this.listener = listener;
    }

    public void connect() {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
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
    private void onOpen(Session session) {
        listener.onOpen();
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        listener.onMessage(message);
        clientMessage = gson.fromJson(message, ClientMessage.class);
        AsyncMessageType type = clientMessage.getType();
        switch (type) {
            case Ping:
                onPingMessage(clientMessage);
                break;
            case ServerRegister:
                onServerRegisteredMessage(clientMessage);
                break;
            case DeviceRegister:
                onDeviceRegisteredMessage(clientMessage);
                break;
        }
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
            MessageWrapperVo messageWrapperVo = new MessageWrapperVo();
            messageWrapperVo.setContent(content);
            messageWrapperVo.setType(serverRegister);
            return gson.toJson(messageWrapperVo);
        } else {
            return null;
        }
    }

    @OnClose
    private void close(Session session, CloseReason reason) {
        listener.onClose();
    }

    @OnError
    private void onError(Session session, Throwable throwable) {
        listener.onError(new Exception(throwable.getCause().getMessage()));
    }

    private void onPingMessage(ClientMessage clientMessage) {
        if (clientMessage.getContent() != null) {
            if (deviceId == null) {
                deviceId = clientMessage.getContent();
            }
            registerDevice();
        }
    }

    private void onServerRegisteredMessage(ClientMessage clientMessage) {
        if (clientMessage.getSenderName().equals(config.getServerName())) {
            isServerRegistered = true;
            listener.onSocketReady();
        } else {
            registerServer();
        }
    }

    private void onDeviceRegisteredMessage(ClientMessage clientMessage) {
        isDeviceRegistered = true;
        Integer oldPeerId = peerId;
        if (clientMessage.getContent() != null) {
            peerId = Integer.parseInt(clientMessage.getContent());
        }

        if (isServerRegistered && peerId.equals(oldPeerId)) {
            listener.onSocketReady();
        } else {
            registerServer();
        }
    }
}
