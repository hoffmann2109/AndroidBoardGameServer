package at.aau.serg.monopoly.websoket;

import model.Game;
import model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new GameWebSocketHandler();
        // Use reflection to set the mocked game and propertyTransactionService
        try {
            var gameField = GameWebSocketHandler.class.getDeclaredField("game");
            gameField.setAccessible(true);
            gameField.set(handler, game);
            
            var propertyServiceField = GameWebSocketHandler.class.getDeclaredField("propertyTransactionService");
            propertyServiceField.setAccessible(true);
            propertyServiceField.set(handler, propertyTransactionService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up test", e);
        }
    }

    @Test
    void givenNotPlayerTurn_whenAttemptingToBuyProperty_thenShouldSendNotYourTurnError() throws Exception {
        // Arrange
        String playerId = "player1";
        int propertyId = 1;
        when(session.getId()).thenReturn(playerId);
        when(session.isOpen()).thenReturn(true);
        when(game.getPlayerById(playerId)).thenReturn(Optional.of(player));
        when(game.isPlayerTurn(playerId)).thenReturn(false);
        when(propertyTransactionService.canBuyProperty(any(), anyInt())).thenReturn(false);

        // Act
        handler.handleTextMessage(session, new TextMessage("BUY_PROPERTY:" + propertyId));

        // Assert
        verify(session).sendMessage(argThat((TextMessage message) -> 
            message.getPayload().contains("Cannot buy property - it's not your turn")));
    }

    @Test
    void givenPlayerTurnButCannotBuy_whenAttemptingToBuyProperty_thenShouldSendCannotBuyError() throws Exception {
        // Arrange
        String playerId = "player1";
        int propertyId = 1;
        when(session.getId()).thenReturn(playerId);
        when(session.isOpen()).thenReturn(true);
        when(game.getPlayerById(playerId)).thenReturn(Optional.of(player));
        when(game.isPlayerTurn(playerId)).thenReturn(true);
        when(propertyTransactionService.canBuyProperty(any(), anyInt())).thenReturn(false);

        // Act
        handler.handleTextMessage(session, new TextMessage("BUY_PROPERTY:" + propertyId));

        // Assert
        verify(session).sendMessage(argThat((TextMessage message) -> 
            message.getPayload().contains("Cannot buy property (insufficient funds or already owned)")));
    }
} 