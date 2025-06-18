package at.aau.serg.monopoly.websoket;

import model.Player;
import model.properties.HouseableProperty;
import model.properties.TrainStation;
import model.properties.Utility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RentCalculationServiceTest {

    @Mock
    private PropertyService propertyService;

    @InjectMocks
    private RentCalculationService rentCalculationService;

    private Player owner;
    private Player renter;
    private HouseableProperty property;

    @BeforeEach
    void setUp() {
        // Setup test data
        owner = new Player("owner123", "Owner");
        renter = new Player("renter123", "Renter");

        property = new HouseableProperty(
            1,                // id
            owner.getId(),    // ownerId
            "Test Street",    // name
            100,             // purchasePrice
            10,              // baseRent
            50,              // rent1House
            150,             // rent2Houses
            450,             // rent3Houses
            625,             // rent4Houses
            750,             // rentHotel
            50,              // housePrice
            50,              // hotelPrice
            50,              // mortgageValue
            false,           // isMortgaged
            "test_image",    // image
            1                // position
        );
    }

    @Test
    void calculateRent_WithValidParameters_ReturnsBaseRent() {
        // Act
        int rent = rentCalculationService.calculateRent(property, owner, renter);

        // Assert
        assertEquals(10, rent, "Rent should be equal to base rent for houseable property");
    }

    @Test
    void calculateRent_WithNullProperty_ReturnsZero() {
        // Act
        int rent = rentCalculationService.calculateRent(null, owner, renter);

        // Assert
        assertEquals(0, rent, "Rent should be zero when property is null");
    }

    @Test
    void calculateRent_WithNullOwner_ReturnsZero() {
        // Act
        int rent = rentCalculationService.calculateRent(property, null, renter);

        // Assert
        assertEquals(0, rent, "Rent should be zero when owner is null");
    }

    @Test
    void calculateRent_WithNullRenter_ReturnsZero() {
        // Act
        int rent = rentCalculationService.calculateRent(property, owner, null);

        // Assert
        assertEquals(0, rent, "Rent should be zero when renter is null");
    }
    @Test
    void calculateRent_WithTrainStation_ReturnsCorrectRent() {
        TrainStation station = new TrainStation(
                2, "owner123", "Test Station", 200, 50, 100, 150, 200,
                100, false, "suedbahnhof_bild", 5
        );

        int rent = rentCalculationService.calculateRent(station, owner, renter);

        assertEquals(50, rent);
    }
    @Test
    void calculateRent_WithUtility_ReturnsCorrectRent() {
        Utility utility = new Utility(
                3, "owner123", "Test Utility", 150,
                4, 10, 75, false, "utility_image", 12
        );

        int rent = rentCalculationService.calculateRent(utility, owner, renter);

        assertTrue(rent >= 0);
    }
} 