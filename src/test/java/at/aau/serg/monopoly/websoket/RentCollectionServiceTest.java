package at.aau.serg.monopoly.websoket;

import model.Player;
import model.properties.BaseProperty;
import model.properties.HouseableProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentCollectionServiceTest {

    @Mock
    private PropertyService propertyService;

    @Mock
    private RentCalculationService rentCalculationService;

    @InjectMocks
    private RentCollectionService rentCollectionService;

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
    void canCollectRent_WithValidParameters_ReturnsTrue() {
        // Act
        boolean result = rentCollectionService.canCollectRent(renter, property);

        // Assert
        assertTrue(result, "Should be able to collect rent with valid parameters");
    }

    @Test
    void canCollectRent_WithNullRenter_ReturnsFalse() {
        // Act
        boolean result = rentCollectionService.canCollectRent(null, property);

        // Assert
        assertFalse(result, "Should not be able to collect rent with null renter");
    }

    @Test
    void canCollectRent_WithNullProperty_ReturnsFalse() {
        // Act
        boolean result = rentCollectionService.canCollectRent(renter, null);

        // Assert
        assertFalse(result, "Should not be able to collect rent with null property");
    }

    @Test
    void canCollectRent_WithUnownedProperty_ReturnsFalse() {
        // Arrange
        property.setOwnerId(null);

        // Act
        boolean result = rentCollectionService.canCollectRent(renter, property);

        // Assert
        assertFalse(result, "Should not be able to collect rent for unowned property");
    }

    @Test
    void canCollectRent_WhenRenterOwnsProperty_ReturnsFalse() {
        // Arrange
        property.setOwnerId(renter.getId());

        // Act
        boolean result = rentCollectionService.canCollectRent(renter, property);

        // Assert
        assertFalse(result, "Should not be able to collect rent when renter owns the property");
    }

    @Test
    void canCollectRent_WithMortgagedProperty_ReturnsFalse() {
        // Arrange
        property.setMortgaged(true);

        // Act
        boolean result = rentCollectionService.canCollectRent(renter, property);

        // Assert
        assertFalse(result, "Should not be able to collect rent for mortgaged property");
    }

    @Test
    void collectRent_WithValidParameters_ReturnsTrue() {
        // Arrange
        when(propertyService.getPlayerById(owner.getId())).thenReturn(owner);
        when(rentCalculationService.calculateRent(property, owner, renter)).thenReturn(10);
        renter.addMoney(20); // Ensure renter has enough money

        // Act
        boolean result = rentCollectionService.collectRent(renter, property);

        // Assert
        assertTrue(result, "Should successfully collect rent");
        assertEquals(10, owner.getMoney() - 1500, "Owner should receive rent amount");
        assertEquals(1510, renter.getMoney(), "Renter should pay rent amount"); // 1500 (initial) + 20 (added) - 10 (rent)
    }

    @Test
    void collectRent_WhenCannotCollectRent_ReturnsFalse() {
        // Arrange
        property.setMortgaged(true);

        // Act
        boolean result = rentCollectionService.collectRent(renter, property);

        // Assert
        assertFalse(result, "Should not collect rent when cannot collect rent");
        verify(propertyService, never()).getPlayerById(any());
        verify(rentCalculationService, never()).calculateRent(any(), any(), any());
    }

    @Test
    void collectRent_WhenOwnerNotFound_ReturnsFalse() {
        // Arrange
        when(propertyService.getPlayerById(owner.getId())).thenReturn(null);

        // Act
        boolean result = rentCollectionService.collectRent(renter, property);

        // Assert
        assertFalse(result, "Should not collect rent when owner not found");
        verify(rentCalculationService, never()).calculateRent(any(), any(), any());
    }

    @Test
    void collectRent_WhenRenterHasInsufficientFunds_ReturnsFalse() {
        // Arrange
        when(propertyService.getPlayerById(owner.getId())).thenReturn(owner);
        when(rentCalculationService.calculateRent(property, owner, renter)).thenReturn(2000);
        renter.subtractMoney(renter.getMoney()); // Set renter's money to 0

        // Act
        boolean result = rentCollectionService.collectRent(renter, property);

        // Assert
        assertFalse(result, "Should not collect rent when renter has insufficient funds");
        assertEquals(1500, owner.getMoney(), "Owner's money should not change");
        assertEquals(0, renter.getMoney(), "Renter's money should not change");
    }
} 