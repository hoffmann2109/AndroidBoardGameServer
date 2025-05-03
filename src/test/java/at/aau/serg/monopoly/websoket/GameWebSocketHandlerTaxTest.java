package at.aau.serg.monopoly.websoket;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.TaxPaymentMessage;
import model.Game;
import model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import static org.mockito.Mockito.*;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import static org.junit.jupiter.api.Assertions.assertEquals;


class GameWebSocketHandlerTaxTest {
    private GameWebSocketHandler handler;
    private Game game;
    private WebSocketSession session;
    private ObjectMapper objectMapper;
    private static final String TEST_USER_ID = "testUserId";
    private static final String TEST_PLAYER_NAME = "Test Player";

    @BeforeEach
    void setUp() throws Exception {
        handler = new GameWebSocketHandler();
        game = new Game();
        session = mock(WebSocketSession.class);
        objectMapper = new ObjectMapper();

        // Set up test game state
        game.addPlayer(TEST_USER_ID, TEST_PLAYER_NAME);
        
        // Use reflection to set the private fields
        Field sessionToUserIdField = GameWebSocketHandler.class.getDeclaredField("sessionToUserId");
        sessionToUserIdField.setAccessible(true);
        ConcurrentHashMap<String, String> sessionMap = new ConcurrentHashMap<>();
        sessionToUserIdField.set(handler, sessionMap);
        
        Field gameField = GameWebSocketHandler.class.getDeclaredField("game");
        gameField.setAccessible(true);
        gameField.set(handler, game);

        // Set up session ID
        when(session.getId()).thenReturn("testSessionId");
        sessionMap.put("testSessionId", TEST_USER_ID);
    }

    @Test
    void testHandleValidTaxPaymentMessage() throws Exception {
        // Arrange
        String taxMessage = objectMapper.writeValueAsString(
            new TaxPaymentMessage(TEST_USER_ID, 200, "EINKOMMENSTEUER")
        );
        TextMessage message = new TextMessage(taxMessage);

        // Act
        handler.handleTextMessage(session, message);

        // Assert
        Player player = game.getPlayerById(TEST_USER_ID).orElseThrow();
        assertEquals(1300, player.getMoney()); // 1500 (starting) - 200 (tax)
    }

    @Test
    void testHandleTaxPaymentMessageFromDifferentPlayer() throws Exception {
        // Arrange
        String differentUserId = "differentUserId";
        String taxMessage = objectMapper.writeValueAsString(
            new TaxPaymentMessage(differentUserId, 200, "EINKOMMENSTEUER")
        );
        TextMessage message = new TextMessage(taxMessage);

        // Act
        handler.handleTextMessage(session, message);

        // Assert
        Player player = game.getPlayerById(TEST_USER_ID).orElseThrow();
        assertEquals(1500, player.getMoney()); // Money should not change
    }

    @Test
    void testHandleInvalidTaxPaymentMessage() throws Exception {
        // Arrange
        String invalidMessage = "{\"type\":\"TAX_PAYMENT\",\"invalid\":\"json\"}";
        TextMessage message = new TextMessage(invalidMessage);

        // Act
        handler.handleTextMessage(session, message);

        // Assert
        Player player = game.getPlayerById(TEST_USER_ID).orElseThrow();
        assertEquals(1500, player.getMoney()); // Money should not change
    }

    @Test
    void testHandleZusatzsteuerTaxPayment() throws Exception {
        // Arrange
        String taxMessage = objectMapper.writeValueAsString(
            new TaxPaymentMessage(TEST_USER_ID, 100, "ZUSATZSTEUER")
        );
        TextMessage message = new TextMessage(taxMessage);

        // Act
        handler.handleTextMessage(session, message);

        // Assert
        Player player = game.getPlayerById(TEST_USER_ID).orElseThrow();
        assertEquals(1400, player.getMoney()); // 1500 (starting) - 100 (tax)
    }

    @Test
    void testHandleMultipleTaxPayments() throws Exception {
        // Arrange
        String einkommensteuerMessage = objectMapper.writeValueAsString(
            new TaxPaymentMessage(TEST_USER_ID, 200, "EINKOMMENSTEUER")
        );
        String zusatzsteuerMessage = objectMapper.writeValueAsString(
            new TaxPaymentMessage(TEST_USER_ID, 100, "ZUSATZSTEUER")
        );

        // Act
        handler.handleTextMessage(session, new TextMessage(einkommensteuerMessage));
        handler.handleTextMessage(session, new TextMessage(zusatzsteuerMessage));

        // Assert
        Player player = game.getPlayerById(TEST_USER_ID).orElseThrow();
        assertEquals(1200, player.getMoney()); // 1500 - 200 - 100
    }
} 