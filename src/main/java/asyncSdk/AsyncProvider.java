package asyncSdk;
@SuppressWarnings("unused")
public interface AsyncProvider {
    AsyncProviderListener listener = null;

    void connect() throws Exception;

    void close();

    void send(String message);
}