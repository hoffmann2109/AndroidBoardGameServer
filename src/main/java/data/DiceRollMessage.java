package data;

public class DiceRollMessage {
    public String type = "DICE_ROLL";
    public String playerId;
    public int   value;
    public DiceRollMessage(String pid, int val) {
        this.playerId = pid;
        this.value    = val;
    }

    public String getUserId() {
        return playerId;
    }

    public int getRoll() {
        return value;
    }
}

