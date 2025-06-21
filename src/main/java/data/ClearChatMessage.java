package data;

import lombok.Data;

@Data
public class ClearChatMessage {
    private String type = "CLEAR_CHAT";
    private String reason = "Game has ended";
}
