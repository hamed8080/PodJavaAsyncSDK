package asyncSdk.model;

public final class RegisterDevice {

    /// A boolean is set to true if the peerId has never set before.
    final Boolean renew;

    /// A boolean is set to true if the peerId has set before and has a value.
    final Boolean refresh;

    /// This `appId` will be gained by the configuration.
    final String appId;

    /// Device id.
    final String deviceId;

    /// A boolean is set to true if the peerId has been set before and has a value, otherwise, the other initializer will be used with the refresh.
    public RegisterDevice(Boolean renew,  String appId, String deviceId) {
        this.renew = renew;
        refresh = null;
        this.appId = appId;
        this.deviceId = deviceId;
    }

    /// A boolean is set to true if the peerId has been set before and has a value, otherwise, the other initializer will be used with renewing.
    public RegisterDevice(String appId, Boolean refresh, String deviceId) {
        // We should set renew to false for retrieving old messages.
        renew = false;
        this.refresh = refresh;
        this.appId = appId;
        this.deviceId = deviceId;
    }
}
