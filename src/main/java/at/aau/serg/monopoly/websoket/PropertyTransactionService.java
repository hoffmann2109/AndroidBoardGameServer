package at.aau.serg.monopoly.websoket;
import model.Player;
import model.properties.BaseProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PropertyTransactionService {

    @Autowired
    private PropertyService propertyService;

    /**
     * Checks if a player can buy a specific property
     * @param player The player attempting to buy
     * @param propertyId The ID of the property to buy
     * @return true if the player can buy the property, false otherwise
     */
    public boolean canBuyProperty(Player player, int propertyId) {
        BaseProperty property = findPropertyById(propertyId);

        if (property == null) {
            return false;
        }

        // Check if property is unowned and player has enough money
        return property.getOwnerId() == null &&
                player.getMoney() >= property.getPurchasePrice();
    }

    /**
     * Helper method to find a property by its ID across all property types
     */
    private BaseProperty findPropertyById(int propertyId) {
        // Check houseable properties
        BaseProperty property = propertyService.getHouseablePropertyById(propertyId);
        if (property != null) {
            return property;
        }

        // Check train stations
        property = propertyService.getTrainStations().stream()
                .filter(p -> p.getId() == propertyId)
                .findFirst()
                .orElse(null);
        if (property != null) {
            return property;
        }

        // Check utilities
        return propertyService.getUtilities().stream()
                .filter(p -> p.getId() == propertyId)
                .findFirst()
                .orElse(null);
    }
}