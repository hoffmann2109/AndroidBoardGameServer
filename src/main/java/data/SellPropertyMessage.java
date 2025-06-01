package data;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellPropertyMessage {
    private String type = "SELL_PROPERTY";
    private String playerId;
    private int propertyId;
} 