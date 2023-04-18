package asyncSdk.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@SuppressWarnings("unused")
public final class Device {
    private boolean current;
    private String uid;
}
