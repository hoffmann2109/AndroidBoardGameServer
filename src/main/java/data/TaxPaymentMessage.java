package data;

public class TaxPaymentMessage {
    public String type = "TAX_PAYMENT";
    public String playerId;
    public int amount;
    public String taxType;  // "EINKOMMENSTEUER" or "ZUSATZSTEUER"
    
    public TaxPaymentMessage(String playerId, int amount, String taxType) {
        this.playerId = playerId;
        this.amount = amount;
        this.taxType = taxType;
    }

    public String getPlayerId() {
        return playerId;
    }

    public int getAmount() {
        return amount;
    }

    public String getTaxType() {
        return taxType;
    }
} 