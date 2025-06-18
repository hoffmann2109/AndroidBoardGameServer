package model.properties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Utility extends BaseProperty {
    private int rentOneUtilityMultiplier;
    private int rentTwoUtilitiesMultiplier;

    public Utility(int id, String ownerId, String name, int purchasePrice,
                   int rentOneUtilityMultiplier, int rentTwoUtilitiesMultiplier,
                   int mortgageValue, boolean isMortgaged, String image, int position) {
        super(id, ownerId, name, purchasePrice, mortgageValue, image, position, isMortgaged);
        this.rentOneUtilityMultiplier = rentOneUtilityMultiplier;
        this.rentTwoUtilitiesMultiplier = rentTwoUtilitiesMultiplier;
    }
}
