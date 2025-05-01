package data;

public class DiceRollMessage {
    public String type = "DICE_ROLL";
    public String playerId;
    public int   value;
    public boolean isManual;
    
    public DiceRollMessage(String pid, int val) {
        this.playerId = pid;
        this.value    = val;
        this.isManual = false;
    }

    public DiceRollMessage(String pid, int val, boolean isManual) {
        this.playerId = pid;
        this.value    = val;
        this.isManual = isManual;
    }

    public String getUserId() {
        return playerId;
    }

    public int getRoll() {
        return value;
    }

    public boolean isManual() {
        return isManual;
    }
}

