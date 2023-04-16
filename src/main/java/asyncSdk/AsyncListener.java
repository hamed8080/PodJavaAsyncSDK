package asyncSdk;

import asyncSdk.model.AsyncMessage;
import asyncSdk.model.AsyncState;

public interface AsyncListener {
    void onReceivedMessage(AsyncMessage message);

    void onError(Exception exception);

    void onStateChanged(AsyncState state, Async async);

}
