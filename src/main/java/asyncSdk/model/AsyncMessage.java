package asyncSdk.model;

import lombok.*;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
@Setter
public class AsyncMessage {
    private Long id;
    private int type;
    private Long senderMessageId;
    private Long senderId;
    private Long trackerId;
    private String senderName;
    private String content;
    private String address;
    private String origin;

    public AsyncMessageType getType() {
        return AsyncMessageType.from(type);
    }

    public void setType(AsyncMessageType type) {
        this.type = type.ordinal();
    }
}
