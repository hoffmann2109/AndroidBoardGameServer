package at.aau.serg.monopoly.websoket;

import model.Player;
import model.properties.HouseableProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentCollectionServiceTest {

    @Mock
    private PropertyService propertyService;

    @Mock
    private RentCalculationService rentCalculationService;

    private RentCollectionService rentCollectionService;
    private Player renter;
    private Player owner;
    private HouseableProperty property;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        rentCollectionService = new RentCollectionService(propertyService, rentCalculationService);
        
        // Setup test players
        renter = new Player("renter", "Renter");
        owner = new Player("owner", "Owner");
        
        // Setup test property
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
        property.setOwnerId(owner.getId());
    }

    @Test
    void canCollectRent_WhenPropertyNotOwned_ReturnsFalse() {
        property.setOwnerId(null);
        assertFalse(rentCollectionService.canCollectRent(renter, property));
    }

    @Test
    void canCollectRent_WhenPropertyOwnedByRenter_ReturnsFalse() {
        property.setOwnerId(renter.getId());
        assertFalse(rentCollectionService.canCollectRent(renter, property));
    }

    @Test
    void canCollectRent_WhenPropertyMortgaged_ReturnsFalse() {
        property.setMortgaged(true);
        assertFalse(rentCollectionService.canCollectRent(renter, property));
    }

    @Test
    void canCollectRent_WhenValidConditions_ReturnsTrue() {
        assertTrue(rentCollectionService.canCollectRent(renter, property));
    }

    @Test
    void collectRent_WhenValidConditions_TransfersMoney() {
        // Setup
        int rentAmount = 50;
        renter.setMoney(100);
        owner.setMoney(100);
        when(rentCalculationService.calculateRent(property, owner, renter)).thenReturn(rentAmount);

        // Execute
        boolean result = rentCollectionService.collectRent(renter, property, owner);

        // Verify
        assertTrue(result);
        assertEquals(50, renter.getMoney());
        assertEquals(150, owner.getMoney());
        verify(rentCalculationService).calculateRent(property, owner, renter);
    }

    @Test
    void collectRent_WhenInsufficientFunds_ReturnsFalse() {
        // Setup
        int rentAmount = 150;
        renter.setMoney(100);
        owner.setMoney(100);
        when(rentCalculationService.calculateRent(property, owner, renter)).thenReturn(rentAmount);

        // Execute
        boolean result = rentCollectionService.collectRent(renter, property, owner);

        // Verify
        assertFalse(result);
        assertEquals(100, renter.getMoney());
        assertEquals(100, owner.getMoney());
    }

    @Test
    void collectRent_WhenOwnerIsNull_ReturnsFalse() {
        // Execute
        boolean result = rentCollectionService.collectRent(renter, property, null);

        // Verify
        assertFalse(result);
    }
} 