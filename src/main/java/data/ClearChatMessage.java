package data;

import lombok.Data;

@Data
public class ClearChatMessage {
    private static final String type = "CLEAR_CHAT";
    private static final String reason = "Game has ended";

    private ClearChatMessage() {}

    public static ClearChatMessage create() {
        return new ClearChatMessage();
    }
}
