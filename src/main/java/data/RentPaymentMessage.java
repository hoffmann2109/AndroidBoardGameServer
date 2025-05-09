package data;

import lombok.Data;

@Data
public class RentPaymentMessage {
    private String type = "RENT_PAYMENT";
    private String renterId;
    private String ownerId;
    private int propertyId;
    private String propertyName;
    private int amount;

    public RentPaymentMessage(String renterId, String ownerId, int propertyId, String propertyName, int amount) {
        this.renterId = renterId;
        this.ownerId = ownerId;
        this.propertyId = propertyId;
        this.propertyName = propertyName;
        this.amount = amount;
    }
} 