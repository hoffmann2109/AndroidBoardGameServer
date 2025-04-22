package at.aau.serg.monopoly.websoket;
import model.Player;
import model.properties.BaseProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.logging.Logger;

@Service
public class PropertyTransactionService {

    private static final Logger logger = Logger.getLogger(PropertyTransactionService.class.getName());

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
     * Executes the purchase of a property for a player.
     * Assumes validation (canBuyProperty) has already passed.
     * @param player The player buying the property.
     * @param propertyId The ID of the property being bought.
     * @return true if the purchase was successful, false otherwise.
     */
    public boolean buyProperty(Player player, int propertyId) {
        BaseProperty property = findPropertyById(propertyId);

        // Double-check conditions in case state changed
        if (property == null || property.getOwnerId() != null || player.getMoney() < property.getPurchasePrice()) {
             logger.warning("Attempted to buy property " + propertyId + " by player " + player.getId() + " failed pre-check.");
            return false; // Should not happen if canBuyProperty was called first, but good practice
        }

        try {
            // Convert player ID string to Integer for ownerId
            // THIS MIGHT FAIL if player.getId() is not a parsable integer!
            // TODO: Confirm if player IDs are integers or if BaseProperty.ownerId needs to be String
            Integer playerIdInt = Integer.parseInt(player.getId()); 

            // Perform transaction
            player.subtractMoney(property.getPurchasePrice());
            property.setOwnerId(playerIdInt);

            logger.info("Player " + player.getId() + " successfully bought property " + propertyId + 
                        ". New balance: " + player.getMoney());
            return true;

        } catch (NumberFormatException e) {
            // Log error if player ID cannot be parsed to Integer
             logger.severe("Failed to parse player ID '" + player.getId() + "' to Integer for property ownership.");
            return false;
        } catch (Exception e) {
             logger.severe("An unexpected error occurred during property purchase: " + e.getMessage());
            // Potentially revert changes if partial transaction occurred, though unlikely here.
            return false;
        }
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