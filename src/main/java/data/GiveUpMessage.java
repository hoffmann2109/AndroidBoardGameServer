package data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GiveUpMessage {
    private final String type = "GIVE_UP";
    private final String userId;

    @JsonCreator
    public GiveUpMessage(@JsonProperty("userId") String userId) {
        this.userId = userId;
    }

    public String getType() { return type; }
    public String getUserId() { return userId; }
}
