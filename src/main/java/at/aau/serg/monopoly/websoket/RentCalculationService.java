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
     * Calculates the rent amount for a property based on its type and owner's holdings
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

        try {
            if (property instanceof HouseableProperty) {
                return calculateHouseablePropertyRent((HouseableProperty) property, owner);
            } else if (property instanceof TrainStation) {
                return calculateTrainStationRent((TrainStation) property, owner);
            } else if (property instanceof Utility) {
                return calculateUtilityRent((Utility) property, owner, renter);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error calculating rent: " + e.getMessage());
        }

        return 0;
    }

    private int calculateHouseablePropertyRent(HouseableProperty property, Player owner) {
        // TODO: Implement houseable property rent calculation
        return 0;
    }

    private int calculateTrainStationRent(TrainStation property, Player owner) {
        // TODO: Implement train station rent calculation
        return 0;
    }

    private int calculateUtilityRent(Utility property, Player owner, Player renter) {
        // TODO: Implement utility rent calculation
        return 0;
    }
} 