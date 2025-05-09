package at.aau.serg.monopoly.websoket;

import model.Player;
import model.properties.BaseProperty;
import model.properties.HouseableProperty;
import model.properties.TrainStation;
import model.properties.Utility;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class RentCalculationService {
    private static final Logger logger = Logger.getLogger(RentCalculationService.class.getName());

    private final PropertyService propertyService;

    public RentCalculationService(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    /**
     * Calculates the rent for a property based on its type and owner
     * @param property The property to calculate rent for
     * @param owner The owner of the property
     * @param renter The player who landed on the property
     * @return The calculated rent amount
     */
    public int calculateRent(BaseProperty property, Player owner, Player renter) {
        if (property == null || owner == null || renter == null) {
            logger.log(Level.WARNING, "Invalid parameters for rent calculation");
            return 0;
        }

        // For now, just return base rent for houseable properties
        if (property instanceof HouseableProperty) {
            return ((HouseableProperty) property).getBaseRent();
        }

        return 0;
    }
} 