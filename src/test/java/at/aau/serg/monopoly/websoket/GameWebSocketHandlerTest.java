package at.aau.serg.monopoly.websoket;

import model.Game;
import model.Player;
import model.DiceManagerInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class GameWebSocketHandlerTest {

    private GameWebSocketHandler handler;

    @Mock
    private WebSocketSession session;

    @Mock
    private Game game;

    @Mock
    private PropertyTransactionService propertyTransactionService;

    @Mock
    private Player player;

    @Mock
    private DiceManagerInterface diceManager;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        handler = new GameWebSocketHandler();
        // Inject mocks
        var gameField = GameWebSocketHandler.class.getDeclaredField("game");
        gameField.setAccessible(true);
        gameField.set(handler, game);
        var propField = GameWebSocketHandler.class.getDeclaredField("propertyTransactionService");
        propField.setAccessible(true);
        propField.set(handler, propertyTransactionService);
        var diceField = GameWebSocketHandler.class.getDeclaredField("diceManager");
        diceField.setAccessible(true);
        diceField.set(handler, diceManager);
        // Initialize dice manager
        when(diceManager.rollDices()).thenReturn(5); // Default roll value
        // Common session stubbing
        when(session.getId()).thenReturn("player1");
        when(session.isOpen()).thenReturn(true);
        // Simulate INIT
        String initJson = "{\"type\":\"INIT\",\"userId\":\"player1\",\"name\":\"Player1\"}";
        handler.handleTextMessage(session, new TextMessage(initJson));
        // Clear initial broadcasts
        clearInvocations(session);
    }

    @Test
    void givenNotPlayerTurn_whenAttemptingToBuyProperty_thenShouldSendNotYourTurnError() throws Exception {
        // Arrange
        int propertyId = 1;
        when(game.getPlayerById("player1")).thenReturn(Optional.of(player));
        when(game.isPlayerTurn("player1")).thenReturn(false);
        when(propertyTransactionService.canBuyProperty(any(), anyInt())).thenReturn(false);

        // Act
        handler.handleTextMessage(session, new TextMessage("BUY_PROPERTY:" + propertyId));

        // Assert
        verify(session).sendMessage(argThat((TextMessage msg) ->
                msg.getPayload().contains("Cannot buy property - it's not your turn")
        ));
    }

    @Test
    void givenPlayerTurnButCannotBuy_whenAttemptingToBuyProperty_thenShouldSendCannotBuyError() throws Exception {
        // Arrange
        int propertyId = 1;
        when(game.getPlayerById("player1")).thenReturn(Optional.of(player));
        when(game.isPlayerTurn("player1")).thenReturn(true);
        when(propertyTransactionService.canBuyProperty(any(), anyInt())).thenReturn(false);

        // Act
        handler.handleTextMessage(session, new TextMessage("BUY_PROPERTY:" + propertyId));

        // Assert
        verify(session).sendMessage(argThat((TextMessage msg) ->
                msg.getPayload().contains("Cannot buy property (insufficient funds or already owned)")
        ));
    }

    @Test
    void givenPlayerPassesGo_whenRollingDice_thenShouldBroadcastGoPassingMessage() throws Exception {
        // Arrange
        int roll = 5;
        when(diceManager.rollDices()).thenReturn(roll);
        when(game.updatePlayerPosition(roll, "player1")).thenReturn(true);
        when(game.getCurrentPlayer()).thenReturn(player);
        when(player.getId()).thenReturn("player1");
        when(game.getPlayerInfo()).thenReturn(new ArrayList<>());
        
        // Add session to handler's sessions list
        var sessionsField = GameWebSocketHandler.class.getDeclaredField("sessions");
        sessionsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        var sessions = (CopyOnWriteArrayList<WebSocketSession>) sessionsField.get(handler);
        sessions.add(session);

        // Act
        handler.handleTextMessage(session, new TextMessage("Roll"));

        // Assert
        verify(session).sendMessage(argThat((TextMessage msg) ->
                msg.getPayload().contains("Player player1 passed GO and collected €200")
        ));
    }

    @Test
    void givenPlayerDoesNotPassGo_whenRollingDice_thenShouldNotBroadcastGoPassingMessage() throws Exception {
        // Arrange
        int roll = 5;
        when(diceManager.rollDices()).thenReturn(roll);
        when(game.updatePlayerPosition(roll, "player1")).thenReturn(false);
        when(game.getCurrentPlayer()).thenReturn(player);
        when(player.getId()).thenReturn("player1");
        when(game.getPlayerInfo()).thenReturn(new ArrayList<>());
        
        // Add session to handler's sessions list
        var sessionsField = GameWebSocketHandler.class.getDeclaredField("sessions");
        sessionsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        var sessions = (CopyOnWriteArrayList<WebSocketSession>) sessionsField.get(handler);
        sessions.add(session);

        // Act
        handler.handleTextMessage(session, new TextMessage("Roll"));

        // Assert
        verify(session, never()).sendMessage(argThat((TextMessage msg) ->
                msg.getPayload().contains("passed GO and collected €200")
        ));
    }
}
