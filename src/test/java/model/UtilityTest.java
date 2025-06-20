package model;

import at.aau.serg.monopoly.websoket.PropertyService;
import model.properties.Utility;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UtilityTest {
    @Test
    void calculateRent_ReturnsSingleMultiplier_WhenPropertyServiceIsNull() {
        Utility utility = new Utility();
        utility.setRentOneUtilityMultiplier(4);
        utility.setRentTwoUtilitiesMultiplier(10);
        // propertyService is null by default
        Player owner = mock(Player.class);
        Player renter = mock(Player.class);
        int rent = utility.calculateRent(owner, renter);
        assertEquals(4, rent);
    }

    @Test
    void calculateRent_ReturnsSingleMultiplier_WhenOwnerHasOneUtility() {
        Utility utility = new Utility();
        utility.setRentOneUtilityMultiplier(4);
        utility.setRentTwoUtilitiesMultiplier(10);
        PropertyService propertyService = mock(PropertyService.class);
        Utility ownedUtility = mock(Utility.class);
        Player owner = mock(Player.class);
        Player renter = mock(Player.class);
        when(owner.getId()).thenReturn("owner1");
        when(ownedUtility.getOwnerId()).thenReturn("owner1");
        when(propertyService.getUtilities()).thenReturn(List.of(ownedUtility));
        utility.setPropertyService(propertyService);
        int rent = utility.calculateRent(owner, renter);
        assertEquals(4, rent);
    }

    @Test
    void calculateRent_ReturnsDoubleMultiplier_WhenOwnerHasTwoUtilities() {
        Utility utility = new Utility();
        utility.setRentOneUtilityMultiplier(4);
        utility.setRentTwoUtilitiesMultiplier(10);
        PropertyService propertyService = mock(PropertyService.class);
        Utility ownedUtility1 = mock(Utility.class);
        Utility ownedUtility2 = mock(Utility.class);
        Player owner = mock(Player.class);
        Player renter = mock(Player.class);
        when(owner.getId()).thenReturn("owner1");
        when(ownedUtility1.getOwnerId()).thenReturn("owner1");
        when(ownedUtility2.getOwnerId()).thenReturn("owner1");
        when(propertyService.getUtilities()).thenReturn(List.of(ownedUtility1, ownedUtility2));
        utility.setPropertyService(propertyService);
        int rent = utility.calculateRent(owner, renter);
        assertEquals(10, rent);
    }
} 