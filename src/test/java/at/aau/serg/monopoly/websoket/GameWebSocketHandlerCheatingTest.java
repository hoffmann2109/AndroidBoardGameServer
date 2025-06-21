package at.aau.serg.monopoly.websoket;

import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GameWebSocketHandlerCheatingTest {

    @InjectMocks
    private GameWebSocketHandler handler;

    @Mock private Game game;
    @Mock private CheatService cheatService;
    @Mock private WebSocketSession session;
    @Mock private Player player;
    @Captor private ArgumentCaptor<TextMessage> messageCaptor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(handler, "game",         game);
        ReflectionTestUtils.setField(handler, "cheatService", cheatService);

        lenient().when(session.getId()).thenReturn("session1");
        lenient().when(session.isOpen()).thenReturn(true);

        lenient().when(game.getPlayerInfo()).thenReturn(Collections.emptyList());
        lenient().when(game.getCurrentPlayer()).thenReturn(player);
        lenient().when(player.getId()).thenReturn("session1");

        lenient().when(game.getPlayerById("session1"))
                .thenReturn(Optional.of(player));
        lenient().when(player.getMoney()).thenReturn(1000);

        handler.sessions.add(session);
        handler.sessionToUserId.put("session1", "session1");
    }

    @Test
    void handleCheatMessage_successfulParsingAndUpdate() throws JsonProcessingException {
        // Arrange
        String payload = "{\"type\":\"CHEAT_MESSAGE\",\"message\":\"100\"}";
        when(cheatService.getAmount("100", 1000)).thenReturn(100);

        // Act
        handler.handleCheatMessage(payload, "session1");

        // Assert
        verify(cheatService).getAmount("100", 1000);
        verify(game).updatePlayerMoney("session1", 100);
    }

    @Test
    void handleCheatMessage_invalidNumber_doesNotUpdate() throws JsonProcessingException {
        // Arrange
        String payload = "{\"type\":\"CHEAT_MESSAGE\",\"message\":\"NaN\"}";
        when(cheatService.getAmount("NaN", 1000))
                .thenThrow(new NumberFormatException("bad"));

        // Act
        handler.handleCheatMessage(payload, "session1");

        // Assert
        verify(game, never()).updatePlayerMoney(any(), anyInt());
    }

    @Test
    void handleTextMessage_onCheatMessage_callsCheatAndBroadcastsState() throws Exception {
        // Arrange
        String payload = "{\"type\":\"CHEAT_MESSAGE\",\"message\":\"42\"}";
        when(cheatService.getAmount("42", 1000)).thenReturn(42);

        // Act
        handler.handleTextMessage(session, new TextMessage(payload));

        // Assert: money updated
        verify(game).updatePlayerMoney("session1", 42);
        // Assert: at least one GAME_STATE broadcast
        verify(session, atLeastOnce()).sendMessage(messageCaptor.capture());
        boolean sawState = messageCaptor.getAllValues().stream()
                .anyMatch(tm -> tm.getPayload().startsWith("GAME_STATE:"));
        assertTrue(sawState, "Expected a GAME_STATE broadcast after cheating");
    }
}
