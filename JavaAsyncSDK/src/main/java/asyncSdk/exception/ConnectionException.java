package asyncSdk.exception;

public class ConnectionException extends ChatException {
    public ConnectionException(ConnectionExceptionType connectionExceptionType) {
        super(connectionExceptionType.getKey(), connectionExceptionType.getValue());
    }

    public enum ConnectionExceptionType {
        ACTIVE_MQ_CONNECTION("An error occurred at connecting to activeMq", 101),
        ACTIVE_MQ_SENDING_MESSAGE("An asyncSdk.exception occurred at sending message", 102);

        private final String key;
        private final Integer value;

        ConnectionExceptionType(String key, Integer value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Integer getValue() {
            return value;
        }
    }
}
