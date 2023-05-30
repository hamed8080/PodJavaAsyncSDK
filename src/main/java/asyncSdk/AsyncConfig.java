package asyncSdk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
@SuppressWarnings("all")
@NoArgsConstructor
@AllArgsConstructor
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
    @Builder.Default
    private String appId = "POD-Chat";

    @Builder.Default
    private Long maxReconnectCount = 5L;
    @Builder.Default
    private Long reconnectInterval = 5000L;
    @Builder.Default
    private Long checkConnectionLastMessageInterval = 5000L;
}
