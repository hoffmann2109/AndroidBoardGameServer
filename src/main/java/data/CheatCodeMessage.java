package data;

import lombok.Data;

@Data
public class CheatCodeMessage {
    private String message;
    private String playerId;
    private String type = "CHEAT_MESSAGE";
}
