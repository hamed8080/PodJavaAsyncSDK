# # FanapPodAsyncSDK

<img src="https://gitlab.com/hamed8080/fanappodasyncsdk/-/raw/gl-pages/.docs/favicon.svg"  width="64" height="64" alt="">
<br />
<br />

Fanap's POD Async iOS SDK

## Features

- [x] Simplify Socket connection to Async server

<br />

## Intit

```swift
AsyncConfig asyncConfig = AsyncConfig
                .builder()
                .isSocketProvider(isSocket)
                .socketAddress(socketAddress)
                .serverName(serverName)
                .queuePassword(queuePassword)
                .queueUserName(queueUserName)
                .queueInput(queueInput)
                .queueOutput(queueOutput)
                .queueServer(queueServer)
                .queuePort(queuePort)
                .isLoggable(true)
                .appId("PodChat")
                .build();
                
if (asyncconfig.isSocketProvider()) 
{
     provider = new SocketProvider(config, this);
} 
else 
{
     provider = new ActiveMq(config, this);
}                
```

## Connection State

Notice: Use the connection only it's in <b>ASYNC_READY</b> state

```swift
if (clientMessage.getSenderName().equals(config.getServerName())) 
{
           isServerRegistered = true;
           listener.onSocketReady();
}           
```

<br/>
<br/>

## Send data

```swift
async.provider.send(jsonMessageWrapperVo);
```

<br/>
<br/>
