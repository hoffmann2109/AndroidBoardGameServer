package data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DiceRollMessage {
    public String type = "DICE_ROLL";
    public String playerId;
    public int   value;
    public boolean isManual;
    @JsonProperty("isPasch")
    private boolean isPasch;
    
    public DiceRollMessage(String pid, int val) {
        this.playerId = pid;
        this.value    = val;
        this.isManual = false;
    }

    public DiceRollMessage(String pid, int val, boolean isManual, boolean isPasch) {
        this.playerId = pid;
        this.value    = val;
        this.isManual = isManual;
        this.isPasch = isPasch;
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

    public boolean isPasch() { return isPasch; }
}

