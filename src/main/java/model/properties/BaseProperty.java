package model.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import model.Player;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseProperty {
    protected int id;
    protected String ownerId; // Changed from Integer to String
    protected String name;
    protected int purchasePrice;
    protected int mortgageValue;
    protected String image;
    protected int position;

    @JsonProperty("isMortgaged")
    protected boolean isMortgaged;

    /**
     * Calculates the rent for this property based on its type and owner
     * @param owner The owner of the property
     * @param renter The player who landed on the property
     * @return The calculated rent amount
     */
    public abstract int calculateRent(Player owner, Player renter);
}

