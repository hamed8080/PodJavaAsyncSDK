package asyncSdk.model;

import asyncSdk.model.AsyncMessageType;

/**
     * {@param type } type of message based on type of content
     * {@param trackerId} tracker id of message that received from platform previously
     */
public class MessageWrapperVo {
    private int type;
    private String content;
    private long trackerId;

    public int getType() {
        return type;
    }

    public void setType(AsyncMessageType type) {
        this.type = type.value;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTrackerId() {
        return trackerId;
    }

    public void setTrackerId(long trackerId) {
        this.trackerId = trackerId;
    }
}
