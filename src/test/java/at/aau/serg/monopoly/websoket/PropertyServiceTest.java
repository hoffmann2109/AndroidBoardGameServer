package at.aau.serg.monopoly.websoket;
import model.properties.HouseableProperty;
import model.properties.TrainStation;
import model.properties.Utility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PropertyServiceTest {

    private PropertyService propertyService;

    @BeforeEach
    void setUp() {
        propertyService = new PropertyService();
        propertyService.init();
    }

    @Test
    void testHouseablePropertiesLoaded() {
        List<HouseableProperty> houseableProperties = propertyService.getHouseableProperties();
        assertNotNull(houseableProperties, "Houseable properties list should not be null");
    }

    @Test
    void testTrainStationsLoaded() {
        List<TrainStation> trainStations = propertyService.getTrainStations();
        assertNotNull(trainStations, "Train stations list should not be null");
        assertEquals(4, trainStations.size());
    }

    @Test
    void testUtilitiesLoaded() {
        List<Utility> utilities = propertyService.getUtilities();
        assertNotNull(utilities, "Utilities list should not be null");
        assertEquals(2, utilities.size());
    }
    @Test
    void testGetHouseablePropertyById_Found() {
        HouseableProperty property = propertyService.getHouseablePropertyById(1);
        assertNotNull(property, "Expected a HouseableProperty with id 1 to be found");
        assertEquals(1, property.getId());
    }

    @Test
    void testGetHouseablePropertyById_NotFound() {
        // Trying to get a property with an id that doesn't exist
        HouseableProperty property = propertyService.getHouseablePropertyById(-1);
        assertNull(property, "Expected no property for a non-existent id");
    }

    @Test
    void testInitException() {
        PropertyService brokenService = new PropertyService() {
            @Override
            public void init() {
                try {
                    throw new IOException("Simulated IO Exception");
                } catch (IOException e) {
                    throw new RuntimeException("Failed to initialize property data", e);
                }
            }
        };

        RuntimeException exception = assertThrows(RuntimeException.class, brokenService::init);
        assertTrue(exception.getMessage().contains("Failed to initialize property data"));
    }

}
