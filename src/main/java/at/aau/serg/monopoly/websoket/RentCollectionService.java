package at.aau.serg.monopoly.websoket;

import model.Player;
import model.properties.BaseProperty;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class RentCollectionService {
    private static final Logger logger = Logger.getLogger(RentCollectionService.class.getName());

    private final PropertyService propertyService;
    private final RentCalculationService rentCalculationService;

    public RentCollectionService(PropertyService propertyService, RentCalculationService rentCalculationService) {
        this.propertyService = propertyService;
        this.rentCalculationService = rentCalculationService;
    }

    /**
     * Validates if rent can be collected from a player
     * @param renter The player who landed on the property
     * @param property The property to collect rent for
     * @return true if rent can be collected, false otherwise
     */
    boolean canCollectRent(Player renter, BaseProperty property) {
        if (renter == null || property == null) {
            logger.log(Level.WARNING, "Invalid parameters for rent collection validation");
            return false;
        }

        // Check if property is owned by another player
        if (property.getOwnerId() == null || property.getOwnerId().equals(renter.getId())) {
            logger.log(Level.INFO, "Property {0} is not owned by another player", property.getName());
            return false;
        }

        // Check if property is mortgaged
        if (property.isMortgaged()) {
            logger.log(Level.INFO, "Property {0} is mortgaged, no rent can be collected", property.getName());
            return false;
        }

        return true;
    }

    /**
     * Collects rent from a player who landed on a property
     * @param renter The player who landed on the property
     * @param property The property to collect rent for
     * @return true if rent was collected successfully, false otherwise
     */
    public boolean collectRent(Player renter, BaseProperty property) {
        if (!canCollectRent(renter, property)) {
            return false;
        }

        // Get the property owner
        Player owner = propertyService.getPlayerById(property.getOwnerId());
        if (owner == null) {
            logger.log(Level.WARNING, "Property owner not found for property {0}", property.getName());
            return false;
        }

        // Calculate rent amount
        int rentAmount = rentCalculationService.calculateRent(property, owner, renter);
        
        // Check if renter has enough money
        if (renter.getMoney() < rentAmount) {
            logger.log(Level.INFO, "Player {0} has insufficient funds to pay rent", renter.getId());
            return false;
        }

        // Process rent payment
        try {
            renter.subtractMoney(rentAmount);
            owner.addMoney(rentAmount);
            
            logger.log(Level.INFO, "Rent of {0} collected from player {1} for property {2}", 
                new Object[]{rentAmount, renter.getId(), property.getName()});
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing rent payment: {0}", e.getMessage());
            return false;
        }
    }
} 