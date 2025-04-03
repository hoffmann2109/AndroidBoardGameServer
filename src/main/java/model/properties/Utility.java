package model.properties;
import lombok.Getter;
import lombok.Setter;

public class Utility extends BaseProperty {
    @Getter
    @Setter
    private int rentOneUtilityMultiplier;
    @Getter
    @Setter
    private int rentTwoUtilitiesMultiplier;

    public Utility() {
        super();
    }

    public Utility(int id, Integer ownerId, String name, int purchasePrice,
                   int rentOneUtilityMultiplier, int rentTwoUtilitiesMultiplier,
                   int mortgageValue, boolean isMortgaged) {
        super(id, ownerId, name, purchasePrice, mortgageValue, isMortgaged);
        this.rentOneUtilityMultiplier = rentOneUtilityMultiplier;
        this.rentTwoUtilitiesMultiplier = rentTwoUtilitiesMultiplier;
    }
}