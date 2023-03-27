package asyncSdk;

import asyncSdk.model.AsyncState;

public interface AsyncListener {
    void onReceivedMessage(String textMessage);

//    void onStateChanged(AsyncState state);

    void onError(Exception exception);

    void onStateChanged(AsyncState state);

//    void onStateChanged(AsyncState state);

//    void onStateChanged(AsyncState state);
}
