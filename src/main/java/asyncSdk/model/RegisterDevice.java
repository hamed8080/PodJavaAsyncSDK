package asyncSdk.model;

public class RegisterDevice {

    /// A boolean is set to true if the peerId has never set before.
    Boolean renew;

    /// A boolean is set to true if the peerId has set before and has a value.
    Boolean refresh;

    /// This `appId` will be gained by the configuration.
    String appId;

    /// Device id.
    String deviceId;

    /// A boolean is set to true if the peerId has been set before and has a value, otherwise, the other initializer will be used with the refresh.
    public RegisterDevice(Boolean renew,  String appId, String deviceId) {
        this.renew = renew;
        refresh = null;
        this.appId = appId;
        this.deviceId = deviceId;
    }

    /// A boolean is set to true if the peerId has been set before and has a value, otherwise, the other initializer will be used with renewing.
    public RegisterDevice(String appId, Boolean refresh, String deviceId) {
        // We should set renew to false to retrieve old messages from.
        renew = false;
        this.refresh = refresh;
        this.appId = appId;
        this.deviceId = deviceId;
    }
}
