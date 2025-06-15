package at.aau.serg.monopoly.websoket;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.TaxPaymentMessage;
import model.Game;
import model.Player;
import model.DiceManagerInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

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

        game.addPlayer(TEST_USER_ID, TEST_PLAYER_NAME);

        Field sessionToUserIdField = GameWebSocketHandler.class.getDeclaredField("sessionToUserId");
        sessionToUserIdField.setAccessible(true);
        ConcurrentHashMap<String, String> sessionMap = new ConcurrentHashMap<>();
        sessionToUserIdField.set(handler, sessionMap);

        Field gameField = GameWebSocketHandler.class.getDeclaredField("game");
        gameField.setAccessible(true);
        gameField.set(handler, game);

        when(session.getId()).thenReturn("testSessionId");
        sessionMap.put("testSessionId", TEST_USER_ID);

        // INIT-Nachricht senden
        String initJson = objectMapper.createObjectNode()
                .put("type", "INIT")
                .put("userId", TEST_USER_ID)
                .put("name", TEST_PLAYER_NAME)
                .toString();
        handler.handleTextMessage(session, new TextMessage(initJson));
    }

    @Test
    void testHandleValidTaxPaymentMessage() throws Exception {
        String taxJson = "{\"type\":\"TAX_PAYMENT\",\"playerId\":\"testUserId\",\"amount\":200,\"taxType\":\"EINKOMMENSTEUER\"}";
        handler.handleTextMessage(session, new TextMessage(taxJson));
        Player player = game.getPlayerById(TEST_USER_ID).orElseThrow();
        assertEquals(1300, player.getMoney());
    }

    @Test
    void testHandleTaxPaymentMessageFromDifferentPlayer() throws Exception {
        String taxJson = "{\"type\":\"TAX_PAYMENT\",\"playerId\":\"someoneElse\",\"amount\":200,\"taxType\":\"EINKOMMENSTEUER\"}";
        handler.handleTextMessage(session, new TextMessage(taxJson));
        Player player = game.getPlayerById(TEST_USER_ID).orElseThrow();
        assertEquals(1500, player.getMoney());
    }

    @Test
    void testHandleInvalidTaxPaymentMessage() throws Exception {
        String invalidJson = "{\"type\":\"TAX_PAYMENT\",\"invalid\":\"data\"}";
        handler.handleTextMessage(session, new TextMessage(invalidJson));
        Player player = game.getPlayerById(TEST_USER_ID).orElseThrow();
        assertEquals(1500, player.getMoney());
    }

    @Test
    void testHandleZusatzsteuerTaxPayment() throws Exception {
        String taxJson = "{\"type\":\"TAX_PAYMENT\",\"playerId\":\"testUserId\",\"amount\":100,\"taxType\":\"ZUSATZSTEUER\"}";
        handler.handleTextMessage(session, new TextMessage(taxJson));
        Player player = game.getPlayerById(TEST_USER_ID).orElseThrow();
        assertEquals(1400, player.getMoney());
    }

    @Test
    void testHandleMultipleTaxPayments() throws Exception {
        String einkommensteuerJson = "{\"type\":\"TAX_PAYMENT\",\"playerId\":\"testUserId\",\"amount\":200,\"taxType\":\"EINKOMMENSTEUER\"}";
        String zusatzsteuerJson = "{\"type\":\"TAX_PAYMENT\",\"playerId\":\"testUserId\",\"amount\":100,\"taxType\":\"ZUSATZSTEUER\"}";
        handler.handleTextMessage(session, new TextMessage(einkommensteuerJson));
        handler.handleTextMessage(session, new TextMessage(zusatzsteuerJson));
        Player player = game.getPlayerById(TEST_USER_ID).orElseThrow();
        assertEquals(1200, player.getMoney());
    }

    @Disabled
    @Test
    void testTaxPaymentAfterPassingGo() throws Exception {
        Player player = game.getPlayerById(TEST_USER_ID).orElseThrow();
        player.setPosition(39);
        int initial = player.getMoney();

        Field diceManagerField = GameWebSocketHandler.class.getDeclaredField("diceManager");
        diceManagerField.setAccessible(true);
        diceManagerField.set(handler, mockDiceManager);

        when(diceManager.rollDices()).thenReturn(5);
        when(diceManager.isPasch()).thenReturn(false);
        when(diceManager.getLastRollValues()).thenReturn(List.of(5));

        handler.handleTextMessage(session, new TextMessage("Roll"));
        assertEquals(initial + 200 - 200, player.getMoney());
    }
    @Disabled
    @Test
    void testNoTaxPaymentOnOtherPositions() throws Exception {
        Player player = game.getPlayerById(TEST_USER_ID).orElseThrow();
        player.setPosition(0);
        int initial = player.getMoney();

        Field diceManagerField = GameWebSocketHandler.class.getDeclaredField("diceManager");
        diceManagerField.setAccessible(true);
        DiceManagerInterface diceManager = mock(DiceManagerInterface.class);
        diceManagerField.set(handler, diceManager);

        when(diceManager.rollDices()).thenReturn(3);
        when(diceManager.isPasch()).thenReturn(false);
        when(diceManager.getLastRollValues()).thenReturn(List.of(3));

        handler.handleTextMessage(session, new TextMessage("Roll"));
        assertEquals(initial, player.getMoney());
    }

    @Test
    void testHandleTaxPaymentWithMultiplePlayers() throws Exception {
        // Arrange
        game.addPlayer("otherPlayerId", "Other Player");

        Player currentPlayer = game.getPlayerById(TEST_USER_ID).orElseThrow();
        Player otherPlayer = game.getPlayerById("otherPlayerId").orElseThrow();

        int currentMoney = currentPlayer.getMoney();
        int otherMoney = otherPlayer.getMoney();

        String taxJson = "{\"type\":\"TAX_PAYMENT\",\"playerId\":\"" + TEST_USER_ID + "\",\"amount\":200,\"taxType\":\"EINKOMMENSTEUER\"}";
        handler.handleTextMessage(session, new TextMessage(taxJson));

        // Assert
        assertEquals(currentMoney - 200, currentPlayer.getMoney());
        assertEquals(otherMoney, otherPlayer.getMoney());
    }

    @Test
    void testTaxPaymentOnEinkommensteuerPosition() throws Exception {
        Player player = game.getPlayerById(TEST_USER_ID).orElseThrow();
        player.setPosition(4); // Feld für Einkommensteuer annehmen

        int initialMoney = player.getMoney();

        // Steuer manuell simulieren
        String taxJson = "{\"type\":\"TAX_PAYMENT\",\"playerId\":\"" + TEST_USER_ID + "\",\"amount\":200,\"taxType\":\"EINKOMMENSTEUER\"}";
        handler.handleTextMessage(session, new TextMessage(taxJson));

        assertEquals(initialMoney - 200, player.getMoney());
    }

    @Test
    void testTaxPaymentOnZusatzsteuerPosition() throws Exception {
        Player player = game.getPlayerById(TEST_USER_ID).orElseThrow();
        player.setPosition(38); // Zusatzsteuerfeld annehmen

        int initialMoney = player.getMoney();

        // Steuer manuell simulieren
        String taxJson = "{\"type\":\"TAX_PAYMENT\",\"playerId\":\"" + TEST_USER_ID + "\",\"amount\":100,\"taxType\":\"ZUSATZSTEUER\"}";
        handler.handleTextMessage(session, new TextMessage(taxJson));

        assertEquals(initialMoney - 100, player.getMoney());
    }

    @Test
    void testTaxPaymentWithMultiplePlayers() throws Exception {
        // Arrange
        Game spyGame = spy(new Game());
        spyGame.addPlayer(TEST_USER_ID, "TestPlayer");
        String otherPlayerId = "otherPlayerId";
        spyGame.addPlayer(otherPlayerId, "Other Player");

        Field gameField = GameWebSocketHandler.class.getDeclaredField("game");
        gameField.setAccessible(true);
        gameField.set(handler, spyGame);

        Player currentPlayer = spyGame.getPlayerById(TEST_USER_ID).orElseThrow();
        Player otherPlayer = spyGame.getPlayerById(otherPlayerId).orElseThrow();

        currentPlayer.setPosition(0); // Start at 0
        int currentPlayerInitialMoney = currentPlayer.getMoney();
        int otherPlayerInitialMoney = otherPlayer.getMoney();

}

        Field diceManagerField = GameWebSocketHandler.class.getDeclaredField("diceManager");
        diceManagerField.setAccessible(true);
        diceManagerField.set(handler, mockDiceManager);


        doReturn(true).when(spyGame).updatePlayerPosition(4, TEST_USER_ID);
        currentPlayer.setPosition(4);

        // Act
        handler.handleTextMessage(session, new TextMessage("Roll"));

        // Assert
        assertEquals(currentPlayerInitialMoney - 200, currentPlayer.getMoney()); // Steuern gezahlt
        assertEquals(otherPlayerInitialMoney, otherPlayer.getMoney());           // Andere bleibt gleich
    }
} 