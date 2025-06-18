package model.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import model.Player;
import at.aau.serg.monopoly.websoket.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;

@Data
@NoArgsConstructor
public class Utility extends BaseProperty {
    private int rentOneUtilityMultiplier;
    private int rentTwoUtilitiesMultiplier;

    @Autowired
    private PropertyService propertyService;

    public Utility(int id, String ownerId, String name, int purchasePrice,
                   int rentOneUtilityMultiplier, int rentTwoUtilitiesMultiplier,
                   int mortgageValue, boolean isMortgaged, String image, int position) {
        super(id, ownerId, name, purchasePrice, mortgageValue, image, position, isMortgaged);
        this.rentOneUtilityMultiplier = rentOneUtilityMultiplier;
        this.rentTwoUtilitiesMultiplier = rentTwoUtilitiesMultiplier;
    }

    @Override
    public int calculateRent(Player owner, Player renter) {
        if (propertyService == null) {
            return rentOneUtilityMultiplier; // Default to single utility rent if service not available
        }

        // Get the number of utilities owned by the owner
        int ownedUtilities = propertyService.getUtilities().stream()
                .filter(u -> owner.getId().equals(u.getOwnerId()))
                .toList()
                .size();

        // Calculate rent based on number of utilities owned
        return ownedUtilities == 1 ? 
            rentOneUtilityMultiplier : 
            rentTwoUtilitiesMultiplier;
    }
}
