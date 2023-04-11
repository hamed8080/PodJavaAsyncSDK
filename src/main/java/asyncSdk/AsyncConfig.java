package asyncSdk;

import lombok.Builder;
import lombok.Getter;

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
}
