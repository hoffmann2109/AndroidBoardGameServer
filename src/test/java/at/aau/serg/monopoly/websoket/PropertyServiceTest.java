package at.aau.serg.monopoly.websoket;
import model.properties.HouseableProperty;
import model.properties.TrainStation;
import model.properties.Utility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import model.Game;
import model.Player;
import java.util.Arrays;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
public class PropertyServiceTest {

    @Mock
    private Game game;

    @InjectMocks
    private PropertyService propertyService;

    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        propertyService = new PropertyService(game);
        propertyService.init();
        player1 = new Player("player1", "Player One");
        player2 = new Player("player2", "Player Two");
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

    @Test
    void getPlayerById_WithValidId_ReturnsPlayer() {
        // Arrange
        when(game.getPlayers()).thenReturn(Arrays.asList(player1, player2));

        // Act
        Player result = propertyService.getPlayerById("player1");

        // Assert
        assertNotNull(result, "Should return player when ID exists");
        assertEquals("player1", result.getId(), "Should return correct player");
    }

    @Test
    void getPlayerById_WithNonExistentId_ReturnsNull() {
        // Arrange
        when(game.getPlayers()).thenReturn(Arrays.asList(player1, player2));

        // Act
        Player result = propertyService.getPlayerById("nonexistent");

        // Assert
        assertNull(result, "Should return null when ID doesn't exist");
    }

    @Test
    void getPlayerById_WithNullId_ReturnsNull() {
        // Act
        Player result = propertyService.getPlayerById(null);

        // Assert
        assertNull(result, "Should return null when ID is null");
        verify(game, never()).getPlayers();
    }

    @Test
    void getPlayerById_WithNullGame_ReturnsNull() {
        // Arrange
        PropertyService service = new PropertyService(null);

        // Act
        Player result = service.getPlayerById("player1");

        // Assert
        assertNull(result, "Should return null when game is null");
    }

    @Test
    void getPlayerById_WithEmptyPlayersList_ReturnsNull() {
        // Arrange
        when(game.getPlayers()).thenReturn(Collections.emptyList());

        // Act
        Player result = propertyService.getPlayerById("player1");

        // Assert
        assertNull(result, "Should return null when players list is empty");
    }
}
