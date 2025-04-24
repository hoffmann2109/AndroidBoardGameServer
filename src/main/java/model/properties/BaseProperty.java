package model.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

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
}

