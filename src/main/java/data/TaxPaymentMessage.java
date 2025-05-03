package data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class TaxPaymentMessage {
    private String type = "TAX_PAYMENT";
    private String playerId;
    private int amount;
    private String taxType;  // "EINKOMMENSTEUER" or "ZUSATZSTEUER"
    
    @JsonCreator
    public TaxPaymentMessage(
            @JsonProperty("playerId") String playerId,
            @JsonProperty("amount") int amount,
            @JsonProperty("taxType") String taxType) {
        this.playerId = playerId;
        this.amount = amount;
        this.taxType = taxType;
    }

}