package data;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RentPaymentMessage {
    private String type = "RENT_PAYMENT";
    private String playerId;
    private String ownerId;
    private int propertyId;
    private String propertyName;
    private int amount;

    public RentPaymentMessage(String playerId, String ownerId, int propertyId, String propertyName, int amount) {
        this.playerId = playerId;
        this.ownerId = ownerId;
        this.propertyId = propertyId;
        this.propertyName = propertyName;
        this.amount = amount;
    }
} 