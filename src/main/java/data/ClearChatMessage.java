package data;

import lombok.Data;

@Data
public class ClearChatMessage {
    private static final String TYPE = "CLEAR_CHAT";
    private static final String REASON = "Game has ended";
}