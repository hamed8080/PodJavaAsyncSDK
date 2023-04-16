package asyncSdk.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public final class Device {
    private boolean current;
    private String uid;
}
