package asyncSdk.model;

import lombok.*;

/**
 * {@param peerName } name of receiver peer
 * {@param receivers} array of receiver peer ids (if you use this, peerName will be ignored)
 * {@param priority} priority of message 1-10, lower has more priority
 * {@param ttl} time to live for message in millisecond
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    private String peerName;
    private String content;
    private long[] receivers;
    private int priority;
    private long messageId;
    private long ttl;
    private String uniqueId;
}
