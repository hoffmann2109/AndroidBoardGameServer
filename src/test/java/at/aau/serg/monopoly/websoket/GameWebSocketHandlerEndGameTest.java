package at.aau.serg.monopoly.websoket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.properties.HouseableProperty;
import model.properties.TrainStation;
import model.properties.Utility;
import model.Player;
import model.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GameWebSocketHandlerEndGameTest {

    @InjectMocks
    private GameWebSocketHandler handler;
    @Mock
    private PropertyService propertyService;
    @Mock
    private WebSocketSession session;
    @Mock
    private Game mockGame;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(handler, "game", mockGame);
        ReflectionTestUtils.setField(handler, "propertyService", propertyService);

        when(session.getId()).thenReturn("sess-1");
        when(session.isOpen()).thenReturn(true);
        handler.sessions.clear();
        handler.sessions.add(session);
    }

    @Test
    void testSumLiquidationValueOfOwnedProperties() {
        // Arrange
        HouseableProperty hp = mock(HouseableProperty.class);
        when(hp.getOwnerId()).thenReturn("p1");
        when(hp.getPurchasePrice()).thenReturn(100);

        TrainStation ts = mock(TrainStation.class);
        when(ts.getOwnerId()).thenReturn("p1");
        when(ts.getPurchasePrice()).thenReturn(200);

        Utility u = mock(Utility.class);
        when(u.getOwnerId()).thenReturn("otherOwner");
        when(u.getPurchasePrice()).thenReturn(300);

        when(propertyService.getHouseableProperties())
                .thenReturn(Collections.singletonList(hp));
        when(propertyService.getTrainStations())
                .thenReturn(Collections.singletonList(ts));
        when(propertyService.getUtilities())
                .thenReturn(Collections.singletonList(u));

        Integer total = ReflectionTestUtils.invokeMethod(
                handler,
                "sumLiquidationValueOfOwnedProperties",
                "p1"
        );
        assertNotNull(total);
        // hp contributes 100/2 = 50, ts contributes 200/2 = 100, utility is ignored => 150
        assertEquals(150, total.intValue());
    }

    @Test
    void testCheckAllPlayersForBankruptcy_broadcastsIsBankruptOnce() throws Exception {
        // Arrange
        Player bankruptPlayer = mock(Player.class);
        when(bankruptPlayer.getId()).thenReturn("bankruptId");
        when(bankruptPlayer.getMoney()).thenReturn(-20); // negative cash

        Player solventPlayer = mock(Player.class);
        when(solventPlayer.getId()).thenReturn("solventId");
        when(solventPlayer.getMoney()).thenReturn(100); // positive cash

        when(mockGame.getPlayers())
                .thenReturn(Arrays.asList(bankruptPlayer, solventPlayer));

        HouseableProperty p = mock(HouseableProperty.class);
        when(p.getOwnerId()).thenReturn("bankruptId");
        when(p.getPurchasePrice()).thenReturn(20);

        when(propertyService.getHouseableProperties())
                .thenReturn(Collections.singletonList(p));
        when(propertyService.getTrainStations())
                .thenReturn(Collections.emptyList());
        when(propertyService.getUtilities())
                .thenReturn(Collections.emptyList());


        GameWebSocketHandler spyHandler = spy(handler);
        doNothing().when(spyHandler).processPlayerGiveUp(anyString(), anyInt(), anyInt());

        spyHandler.sessions.clear();
        spyHandler.sessions.add(session);

        // Act
        ReflectionTestUtils.invokeMethod(spyHandler, "checkAllPlayersForBankruptcy");

        // Assert
        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, times(1)).sendMessage(captor.capture());

        String payload = captor.getValue().getPayload();
        ObjectNode node = (ObjectNode) mapper.readTree(payload);
        assertEquals("IS_BANKRUPT", node.get("type").asText());
        assertEquals("bankruptId", node.get("userId").asText());
    }

    @Test
    void testCheckAllPlayersForBankruptcy_serializationExceptionIsCaught() throws Exception {
        // Arrange
        Player bankruptPlayer = mock(Player.class);
        when(bankruptPlayer.getId()).thenReturn("bankruptX");
        when(bankruptPlayer.getMoney()).thenReturn(-50);

        when(mockGame.getPlayers()).thenReturn(Collections.singletonList(bankruptPlayer));

        when(propertyService.getHouseableProperties()).thenReturn(Collections.emptyList());
        when(propertyService.getTrainStations()).thenReturn(Collections.emptyList());
        when(propertyService.getUtilities()).thenReturn(Collections.emptyList());

        ObjectMapper realMapper = new ObjectMapper();
        ObjectMapper spyMapper = spy(realMapper);
        when(spyMapper.writeValueAsString(any())).thenThrow(
                new com.fasterxml.jackson.core.JsonProcessingException("boom") {});

        ReflectionTestUtils.setField(handler, "objectMapper", spyMapper);

        GameWebSocketHandler spyHandler = spy(handler);
        doNothing().when(spyHandler).processPlayerGiveUp(anyString(),anyInt(), anyInt());

        spyHandler.sessions.clear();
        spyHandler.sessions.add(session);

        // Act
        ReflectionTestUtils.invokeMethod(spyHandler, "checkAllPlayersForBankruptcy");

        // Assert
        verify(session, never()).sendMessage(any(TextMessage.class));
    }
}
