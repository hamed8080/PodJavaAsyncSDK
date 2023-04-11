package asyncSdk;

import asyncSdk.AsyncListener;

public interface AsyncProviderListener {
    AsyncListener listener = null;
    void onOpen();
    void onClose();
    void onSocketReady();
    void onMessage(String message);
    void onError(Exception exception);
}
