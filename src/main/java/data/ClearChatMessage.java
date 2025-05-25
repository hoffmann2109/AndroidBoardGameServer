package data;

import lombok.Data;

@Data
public class ClearChatMessage {
    private final String type = "CLEAR_CHAT";
    private final String reason = "Game has ended";
}