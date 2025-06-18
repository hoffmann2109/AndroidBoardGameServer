package at.aau.serg.monopoly.websoket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.GiveUpMessage;
import model.Game;
import model.Player;
import org.mockito.ArgumentCaptor;
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
class GameWebSocketHandlerGiveUpTest {

    @InjectMocks
    private GameWebSocketHandler handler;

    @Mock
    private Game game;
    @Mock
    private WebSocketSession session;
    @Mock
    private Player quittingPlayer;
    @Mock
    private Player remainingPlayer;
    @Mock
    private PropertyService propertyService;
    @Mock
    private GameHistoryService gameHistoryService;
    @Mock
    private CardDeckService cardDeckService;
    @Mock
    private PropertyTransactionService propertyTransactionService;
    @Mock
    private RentCollectionService rentCollectionService;
    @Mock
    private RentCalculationService rentCalculationService;
    @Mock
    private CheatService cheatService;
    @Mock
    private DealService dealService;
    @Captor
    private ArgumentCaptor<TextMessage> messageCaptor;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(handler, "game", game);
        ReflectionTestUtils.setField(handler, "objectMapper", new ObjectMapper()); // <- wichtig!

        handler.sessions.clear();
        handler.sessions.add(session);

        when(session.getId()).thenReturn("session1");
        when(session.isOpen()).thenReturn(true);

        handler.sessionToUserId.clear();
        handler.sessionToUserId.put("session1", "session1");
        handler.initializeBotManager();

        when(propertyService.getHouseableProperties()).thenReturn(Collections.emptyList());
        when(propertyService.getTrainStations()).thenReturn(Collections.emptyList());
        when(propertyService.getUtilities()).thenReturn(Collections.emptyList());
        when(quittingPlayer.getMoney()).thenReturn(1000);
        when(remainingPlayer.getMoney()).thenReturn(1000);
        when(quittingPlayer.getId()).thenReturn("session1");
        when(remainingPlayer.getId()).thenReturn("remainingId");
    }

    @Test
    void giveUp_notYourTurn_sendsError() throws Exception {
        // Arrange
        JsonNode json = mapper.readTree("{\"userId\":\"session1\"}");
        when(game.isPlayerTurn("session1")).thenReturn(false);

        // Act
        handler.handleGiveUpFromClient(session, json);

        // Assert
        verify(session).sendMessage(messageCaptor.capture());
        String payload = messageCaptor.getValue().getPayload();
        assertTrue(payload.contains("\"type\":\"ERROR\""));
        assertTrue(payload.contains("You can only give up on your turn"));
        verify(game, never()).giveUp(any());
    }

    @Test
    void giveUp_multiplePlayers_broadcastsGiveUpAndGameState() throws Exception {
        // Arrange
        JsonNode json = mapper.readTree("{\"userId\":\"session1\"}");
        when(game.isPlayerTurn("session1")).thenReturn(true);

        List<Player> playersAfterGiveUp = Arrays.asList(quittingPlayer, remainingPlayer);
        when(game.getPlayers()).thenReturn(playersAfterGiveUp);
        when(game.getPlayerInfo()).thenReturn(Collections.emptyList());
        when(game.getCurrentPlayer()).thenReturn(remainingPlayer);
        when(remainingPlayer.getId()).thenReturn("remainingId");

        // Act
        handler.handleGiveUpFromClient(session, json);

        // Assert
        verify(game).giveUp("session1");
        verify(session, times(3)).sendMessage(messageCaptor.capture());

        List<TextMessage> sent = messageCaptor.getAllValues();
        assertEquals(3, sent.size());

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, atLeastOnce()).sendMessage(captor.capture());

        List<String> payloads = captor.getAllValues().stream().map(TextMessage::getPayload).toList();
        GiveUpMessage gu = mapper.readValue(sent.get(0).getPayload(), GiveUpMessage.class);
        assertEquals("session1", gu.getUserId());
        assertTrue(sent.get(1).getPayload().startsWith("GAME_STATE:"));
        assertTrue(payloads.stream().anyMatch(p -> p.contains("\"type\":\"GIVE_UP\"")));
        assertTrue(payloads.stream().anyMatch(p -> p.startsWith("GAME_STATE:")));
        assertTrue(payloads.stream().anyMatch(p -> p.startsWith("PLAYER_TURN:")));
        assertTrue(sent.get(2).getPayload().startsWith("PLAYER_TURN:remainingId"));
    }
}





