package at.aau.serg.monopoly.websoket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.GiveUpMessage;
import data.HasWonMessage;
import model.Game;
import model.Player;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GameWebSocketHandlerGiveUpTest {

    @InjectMocks
    private GameWebSocketHandler handler;

    @Mock private Game game;
    @Mock private WebSocketSession session;
    @Mock private Player quittingPlayer;
    @Mock private Player remainingPlayer;
    @Captor private ArgumentCaptor<TextMessage> messageCaptor;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        // inject our mocks
        ReflectionTestUtils.setField(handler, "game", game);
        handler.sessions.clear();
        handler.sessions.add(session);

        when(session.getId()).thenReturn("session1");
        when(session.isOpen()).thenReturn(true);

        handler.sessionToUserId.clear();
        handler.sessionToUserId.put("session1", "session1");
    }

    @Test
    void giveUp_notYourTurn_sendsError() throws Exception {
        // Arrange
        JsonNode json = mapper.readTree("{\"userId\":\"session1\"}");
        when(game.isPlayerTurn("session1")).thenReturn(false);

        // Act
        handler.handleGiveUp(session, json);

        // Assert: error sent back to same session
        verify(session).sendMessage(messageCaptor.capture());
        String payload = messageCaptor.getValue().getPayload();
        assertTrue(payload.contains("\"type\":\"ERROR\""));
        assertTrue(payload.contains("You can only give up on your turn"));
        // no game.giveUp
        verify(game, never()).giveUp(any());
    }

    @Test
    void giveUp_multiplePlayers_broadcastsGiveUpAndGameState() throws Exception {
        // Arrange: it's your turn
        JsonNode json = mapper.readTree("{\"userId\":\"session1\"}");
        when(game.isPlayerTurn("session1")).thenReturn(true);

        List<Player> players = Arrays.asList(quittingPlayer, remainingPlayer);
        when(game.getPlayers()).thenReturn(players);
        when(game.getPlayerInfo()).thenReturn(Collections.emptyList());
        when(game.getCurrentPlayer()).thenReturn(remainingPlayer);
        when(remainingPlayer.getId()).thenReturn("remainingId");

        // Act
        handler.handleGiveUp(session, json);

        // Assert: game.giveUp called
        verify(game).giveUp("session1");

        // We expect exactly 3 sendMessage calls:
        // 1) GIVE_UP
        // 2) GAME_STATE
        // 3) PLAYER_TURN
        verify(session, times(3)).sendMessage(messageCaptor.capture());

        List<TextMessage> sent = messageCaptor.getAllValues();
        assertEquals(3, sent.size());

        // 1st message = GIVE_UP
        String giveUpPayload = sent.get(0).getPayload();
        assertTrue(giveUpPayload.contains("\"type\":\"GIVE_UP\""));
        assertTrue(giveUpPayload.contains("\"userId\":\"session1\""));

        // 2nd message = GAME_STATE:
        String gameStatePayload = sent.get(1).getPayload();
        assertTrue(gameStatePayload.startsWith("GAME_STATE:"));

        // 3rd message = PLAYER_TURN:
        String turnPayload = sent.get(2).getPayload();
        assertTrue(turnPayload.startsWith("PLAYER_TURN:"));
    }


    @Test
    void giveUp_lastPlayer_sendsHasWonAndEndGame() throws Exception {
        // Arrange: it's your turn
        JsonNode json = mapper.readTree("{\"userId\":\"session1\"}");
        when(game.isPlayerTurn("session1")).thenReturn(true);
        // after removal only one player left
        when(game.getPlayers()).thenReturn(Collections.singletonList(remainingPlayer));
        when(remainingPlayer.getId()).thenReturn("winner1");

        // Act
        handler.handleGiveUp(session, json);

        // Assert: game.giveUp called
        verify(game).giveUp("session1");

        // We expect exactly 2 sendMessage calls: HAS_WON and CLEAR_CHAT
        verify(session, times(2)).sendMessage(messageCaptor.capture());

        List<TextMessage> sent = messageCaptor.getAllValues();
        assertEquals(2, sent.size());

        // 1) The first must be the HasWonMessage
        String hasWonPayload = sent.get(0).getPayload();
        HasWonMessage parsed = mapper.readValue(hasWonPayload, HasWonMessage.class);
        assertEquals("winner1", parsed.getUserId());

        // 2) The second can be the clear-chat JSON (we just assert it has type CLEAR_CHAT)
        String clearChat = sent.get(1).getPayload();
        assertTrue(clearChat.contains("\"type\":\"CLEAR_CHAT\""));
    }

    @Test
    void giveUp_serializationError_sendsError() throws Exception {
        // Arrange
        JsonNode json = mapper.readTree("{\"userId\":\"session1\"}");
        when(game.isPlayerTurn("session1")).thenReturn(true);
        when(game.getPlayers())
                .thenReturn(Arrays.asList(quittingPlayer, remainingPlayer));
        when(game.getPlayerInfo()).thenReturn(Collections.emptyList());
        when(game.getCurrentPlayer()).thenReturn(remainingPlayer);
        when(remainingPlayer.getId()).thenReturn("remainingId");
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        ReflectionTestUtils.setField(handler, "objectMapper", mockMapper);
        when(mockMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("serialize-fail") {});

        // Act
        handler.handleGiveUp(session, json);

        // Assert
        // 1) game.giveUp must still have been called
        verify(game).giveUp("session1");

        // 2) Since serialization failed, we should send exactly one ERROR to the session
        verify(session, times(1)).sendMessage(messageCaptor.capture());
        String errorPayload = messageCaptor.getValue().getPayload();
        assertTrue(errorPayload.contains("\"type\":\"ERROR\""),
                "Expected an ERROR message");
        assertTrue(errorPayload.contains("Server error processing give up"),
                "Expected our give-up error text");
    }


}
