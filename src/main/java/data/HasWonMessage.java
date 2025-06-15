package data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HasWonMessage {
    private static final String type = "HAS_WON";
    private final String userId;

    @JsonCreator
    public HasWonMessage(@JsonProperty("userId") String userId) {
        this.userId = userId;
    }

    public String getType()  { return type; }
    public String getUserId(){ return userId; }
}

