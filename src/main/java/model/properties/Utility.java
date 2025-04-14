package model.properties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Utility extends BaseProperty {
    private int rentOneUtilityMultiplier;
    private int rentTwoUtilitiesMultiplier;

    public Utility(int id, Integer ownerId, String name, int purchasePrice,
                   int rentOneUtilityMultiplier, int rentTwoUtilitiesMultiplier,
                   int mortgageValue, boolean isMortgaged, String image) {
        super(id, ownerId, name, purchasePrice, mortgageValue, image, isMortgaged);
        this.rentOneUtilityMultiplier = rentOneUtilityMultiplier;
        this.rentTwoUtilitiesMultiplier = rentTwoUtilitiesMultiplier;
    }
}
