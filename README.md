# # FanapPodAsyncSDK

<img src="https://gitlab.com/hamed8080/fanappodasyncsdk/-/raw/gl-pages/.docs/favicon.svg"  width="64" height="64">
<br />
<br />

Fanap's POD Async iOS SDK

## Features

- [x] Simplify Socket connection to Async server

<br />

## Intit

```swift
@Builder
@Getter
public class AsyncConfig {
    private boolean isSocketProvider;
    private String token;
    private String serverName;
    private String ssoHost;
    private String queueServer;
    private String queuePort;
    private String queueInput;
    private String queueOutput;
    private String queueUserName;
    private String queuePassword;
    private int queueReconnectTime;
    private String socketAddress;
    private boolean isLoggable;
    private String appId = "POD-Chat";
    
    
 private Async(AsyncConfig config) {
        this.config = config;
        if (config.isSocketProvider()) {
            provider = new SocketProvider(config, this);
        } else {
            provider = new ActiveMq(config, this);
        }
    }        
```

## Connection State

Notice: Use the connection only it's in <b>ASYNC_READY</b> state

```swift
  public void sendMessage(String textContent, AsyncMessageType messageType) {
        try {
            if (state == AsyncState.AsyncReady) { ... }
```

<br/>
<br/>

## Send data

```swift
async.provider.send(jsonMessageWrapperVo);
```

<br/>
<br/>
