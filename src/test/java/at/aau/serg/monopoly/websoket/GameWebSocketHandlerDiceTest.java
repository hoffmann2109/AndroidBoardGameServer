package at.aau.serg.monopoly.websoket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.DiceManagerInterface;
import model.Game;
import model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

class GameWebSocketHandlerDiceTest {

    @Mock WebSocketSession session;
    @Mock PropertyTransactionService propertyTransactionService;

    @Captor ArgumentCaptor<TextMessage> msgCaptor;

    GameWebSocketHandler handler;
    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new GameWebSocketHandler();

        ReflectionTestUtils.setField(handler, "propertyTransactionService", propertyTransactionService);

        when(session.getId()).thenReturn("session-1");
        when(session.isOpen()).thenReturn(true);
    }

    @Test
    void testInitRegistersPlayerAndBroadcasts() throws Exception {
        handler.afterConnectionEstablished(session);

        String initJson = mapper.createObjectNode()
                .put("type", "INIT")
                .put("userId", "u1")
                .put("name", "Alice")
                .toString();

        handler.handleTextMessage(session, new TextMessage(initJson));

        verify(session, atLeast(3)).sendMessage(msgCaptor.capture());
        List<TextMessage> sent = msgCaptor.getAllValues();

        // Join message:
        assertTrue(sent.stream()
                .anyMatch(m -> m.getPayload().contains("SYSTEM: Alice (u1) joined the game"))
        );

        // Game State and Player Turn message:
        assertTrue(sent.stream().anyMatch(m -> m.getPayload().startsWith("GAME_STATE:")));
        assertTrue(sent.stream().anyMatch(m -> m.getPayload().startsWith("PLAYER_TURN:")));
    }

    @Test
    void testRollBeforeInitReturnsError() throws Exception {
        handler.afterConnectionEstablished(session);

        // send Roll without INIT:
        handler.handleTextMessage(session, new TextMessage("Roll"));

        verify(session).sendMessage(msgCaptor.capture());
        String payload = msgCaptor.getValue().getPayload();
        assertEquals("{\"type\":\"ERROR\", \"message\":\"Send INIT message first\"}", payload);
    }


    @Test
    void testRollSuccessBroadcastsDiceRollAndGameState() throws Exception {
        // INIT
        handler.afterConnectionEstablished(session);
        String initJson = mapper.createObjectNode()
                .put("type", "INIT")
                .put("userId", "u1")
                .put("name", "Bob")
                .toString();
        handler.handleTextMessage(session, new TextMessage(initJson));

        // Mock diceManager
        DiceManagerInterface mockDice = mock(DiceManagerInterface.class);
        when(mockDice.rollDices()).thenReturn(8);
        ReflectionTestUtils.setField(handler, "diceManager", mockDice);

        clearInvocations(session);

        // Roll:
        handler.handleTextMessage(session, new TextMessage("Roll"));

        // Verify all three messages:
        verify(session, atLeast(3)).sendMessage(msgCaptor.capture());
        List<TextMessage> all = msgCaptor.getAllValues();

        // Verify parsing and broadcasting:
        JsonNode diceMsg = mapper.readTree(all.get(0).getPayload());
        assertEquals("DICE_ROLL", diceMsg.get("type").asText());
        assertEquals("u1", diceMsg.get("userId").asText());
        assertEquals(8, diceMsg.get("roll").asInt());
        assertTrue(all.stream().anyMatch(m -> m.getPayload().startsWith("GAME_STATE:")));
        assertTrue(all.stream().anyMatch(m -> m.getPayload().startsWith("PLAYER_TURN:")));
    }


    @Test
    void testRollExceptionReturnsServerError() throws Exception {
        // INIT
        handler.afterConnectionEstablished(session);
        String initJson = mapper.createObjectNode()
                .put("type", "INIT")
                .put("userId", "u2")
                .put("name", "Carol")
                .toString();
        handler.handleTextMessage(session, new TextMessage(initJson));

        // Mock diceManager
        DiceManagerInterface mockDice = mock(DiceManagerInterface.class);
        when(mockDice.rollDices()).thenThrow(new RuntimeException("boom"));
        ReflectionTestUtils.setField(handler, "diceManager", mockDice);

        clearInvocations(session);

        // Roll:
        handler.handleTextMessage(session, new TextMessage("Roll"));

        // Verify error message:
        verify(session).sendMessage(msgCaptor.capture());
        String payload = msgCaptor.getValue().getPayload();
        assertEquals("{\"type\":\"ERROR\", \"message\":\"Server error processing your request.\"}", payload);
    }

    @Test
    void testRollTwelveDoesNotAdvanceTurn() throws Exception {
        // Two sessions are needed now:
        WebSocketSession s1 = mock(WebSocketSession.class);
        WebSocketSession s2 = mock(WebSocketSession.class);
        when(s1.getId()).thenReturn("s1");
        when(s2.getId()).thenReturn("s2");
        when(s1.isOpen()).thenReturn(true);
        when(s2.isOpen()).thenReturn(true);

        handler = new GameWebSocketHandler();
        ReflectionTestUtils.setField(handler, "propertyTransactionService", propertyTransactionService);

        handler.afterConnectionEstablished(s1);
        handler.afterConnectionEstablished(s2);

        // INIT
        String init1 = mapper.createObjectNode()
                .put("type", "INIT").put("userId", "u1").put("name", "Alice")
                .toString();
        String init2 = mapper.createObjectNode()
                .put("type", "INIT").put("userId", "u2").put("name", "Bob")
                .toString();
        handler.handleTextMessage(s1, new TextMessage(init1));
        handler.handleTextMessage(s2, new TextMessage(init2));

        // Mock diceManager
        DiceManagerInterface mockDice = mock(DiceManagerInterface.class);
        when(mockDice.rollDices()).thenReturn(12);
        ReflectionTestUtils.setField(handler, "diceManager", mockDice);

        clearInvocations(s1, s2);

        // Player 1 rolls:
        handler.handleTextMessage(s1, new TextMessage("Roll"));

        // Pick out PLAYER_TURN message:
        ArgumentCaptor<TextMessage> cap = ArgumentCaptor.forClass(TextMessage.class);
        verify(s1, atLeast(1)).sendMessage(cap.capture());
        List<TextMessage> msgs = cap.getAllValues();

        TextMessage lastTurnMsg = msgs.stream()
                .filter(m -> m.getPayload().startsWith("PLAYER_TURN:"))
                .reduce((first, second) -> second)
                .orElseThrow();

        // Roll is 12, so the turn should stay
        assertEquals("PLAYER_TURN:u1", lastTurnMsg.getPayload());
    }

    @Test
    void testManualRollSuccessBroadcastsDiceRollAndGameState() throws Exception {
        // INIT
        handler.afterConnectionEstablished(session);
        String initJson = mapper.createObjectNode()
                .put("type", "INIT")
                .put("userId", "u1")
                .put("name", "Bob")
                .toString();
        handler.handleTextMessage(session, new TextMessage(initJson));

        clearInvocations(session);

        // Manual Roll:
        handler.handleTextMessage(session, new TextMessage("MANUAL_ROLL:8"));

        // Verify all three messages:
        verify(session, atLeast(3)).sendMessage(msgCaptor.capture());
        List<TextMessage> all = msgCaptor.getAllValues();

        // Verify parsing and broadcasting:
        JsonNode diceMsg = mapper.readTree(all.get(0).getPayload());
        assertEquals("DICE_ROLL", diceMsg.get("type").asText());
        assertEquals("u1", diceMsg.get("userId").asText());
        assertEquals(8, diceMsg.get("value").asInt());
        assertTrue(diceMsg.get("isManual").asBoolean());
        assertTrue(all.stream().anyMatch(m -> m.getPayload().startsWith("GAME_STATE:")));
        assertTrue(all.stream().anyMatch(m -> m.getPayload().startsWith("PLAYER_TURN:")));
    }

    @Test
    void testManualRollInvalidValueReturnsError() throws Exception {
        // INIT
        handler.afterConnectionEstablished(session);
        String initJson = mapper.createObjectNode()
                .put("type", "INIT")
                .put("userId", "u1")
                .put("name", "Bob")
                .toString();
        handler.handleTextMessage(session, new TextMessage(initJson));

        clearInvocations(session);

        // Manual Roll with invalid value:
        handler.handleTextMessage(session, new TextMessage("MANUAL_ROLL:40"));

        // Verify error message:
        verify(session).sendMessage(msgCaptor.capture());
        TextMessage errorMsg = msgCaptor.getValue();
        assertTrue(errorMsg.getPayload().contains("Invalid roll value"));
    }

    @Test
    void testManualRollInvalidFormatReturnsError() throws Exception {
        // INIT
        handler.afterConnectionEstablished(session);
        String initJson = mapper.createObjectNode()
                .put("type", "INIT")
                .put("userId", "u1")
                .put("name", "Bob")
                .toString();
        handler.handleTextMessage(session, new TextMessage(initJson));

        clearInvocations(session);

        // Manual Roll with invalid format:
        handler.handleTextMessage(session, new TextMessage("MANUAL_ROLL:abc"));

        // Verify error message:
        verify(session).sendMessage(msgCaptor.capture());
        TextMessage errorMsg = msgCaptor.getValue();
        assertTrue(errorMsg.getPayload().contains("Invalid manual roll format"));
    }
    @Test
    void testNextTurnReturnsErrorIfNotYourTurn() throws Exception {
        handler.afterConnectionEstablished(session);

        String initJson = mapper.createObjectNode()
                .put("type", "INIT")
                .put("userId", "u1")
                .put("name", "Alice")
                .toString();
        handler.handleTextMessage(session, new TextMessage(initJson));

        // Manipuliert Spiellogik, damit Spieler nicht dran ist
        Game spyGame = spy(new Game());
        spyGame.addPlayer("u1", "Alice");
        ReflectionTestUtils.setField(handler, "game", spyGame);

        doReturn(false).when(spyGame).isPlayerTurn("u1");

        clearInvocations(session);

        handler.handleTextMessage(session, new TextMessage("NEXT_TURN"));

        verify(session).sendMessage(msgCaptor.capture());
        String msg = msgCaptor.getValue().getPayload();
        assertTrue(msg.contains("Not your turn"));
    }


    @Test
    void testNextTurnAdvancesPlayerIfValid() throws Exception {
        handler.afterConnectionEstablished(session);

        String initJson = mapper.createObjectNode()
                .put("type", "INIT")
                .put("userId", "u1")
                .put("name", "Alice")
                .toString();
        handler.handleTextMessage(session, new TextMessage(initJson));

        Game spyGame = spy(new Game());
        spyGame.addPlayer("u1", "Alice");
        ReflectionTestUtils.setField(handler, "game", spyGame);

        doReturn(true).when(spyGame).isPlayerTurn("u1");

        clearInvocations(session);

        handler.handleTextMessage(session, new TextMessage("NEXT_TURN"));

        verify(spyGame).nextPlayer();
    }


    @Test
    void testRollTwelveAllowsSecondRoll() throws Exception {
        handler.afterConnectionEstablished(session);

        // INIT
        String initJson = mapper.createObjectNode()
                .put("type", "INIT")
                .put("userId", "u1")
                .put("name", "Alice")
                .toString();
        handler.handleTextMessage(session, new TextMessage(initJson));

        // Würfeln: zuerst 12, dann z.B. 4
        DiceManagerInterface mockDice = mock(DiceManagerInterface.class);
        when(mockDice.rollDices()).thenReturn(12).thenReturn(4); // zweimal würfeln erlaubt
        ReflectionTestUtils.setField(handler, "diceManager", mockDice);

        clearInvocations(session);

        // Roll #1 = 12
        handler.handleTextMessage(session, new TextMessage("Roll"));

        // Roll #2 = 4 (immer noch erlaubt)
        handler.handleTextMessage(session, new TextMessage("Roll"));

        // Check ob beide Rolls gesendet wurden
        verify(session, atLeast(2)).sendMessage(msgCaptor.capture());
        List<TextMessage> sent = msgCaptor.getAllValues();

        long diceRolls = sent.stream().filter(m -> m.getPayload().contains("DICE_ROLL")).count();
        assertEquals(2, diceRolls); // zwei gültige Würfe erlaubt
    }

    @Test
    void testNextTurnFromWrongPlayer_returnsError() throws Exception {
        WebSocketSession s1 = mock(WebSocketSession.class);
        WebSocketSession s2 = mock(WebSocketSession.class);
        when(s1.getId()).thenReturn("s1");
        when(s2.getId()).thenReturn("s2");
        when(s1.isOpen()).thenReturn(true);
        when(s2.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(s1);
        handler.afterConnectionEstablished(s2);

        handler.handleTextMessage(s1, new TextMessage(mapper.createObjectNode()
                .put("type", "INIT").put("userId", "u1").put("name", "Alice").toString()));
        handler.handleTextMessage(s2, new TextMessage(mapper.createObjectNode()
                .put("type", "INIT").put("userId", "u2").put("name", "Bob").toString()));

        // u2 versucht Zug zu enden obwohl u1 dran ist
        handler.handleTextMessage(s2, new TextMessage("NEXT_TURN"));

        verify(s2).sendMessage(msgCaptor.capture());
        String payload = msgCaptor.getValue().getPayload();
        assertTrue(payload.contains("Not your turn!"));
    }

    @Test
    void testRollTwiceWithoutTwelveReturnsError() throws Exception {
        handler.afterConnectionEstablished(session);

        // INIT
        String initJson = mapper.createObjectNode()
                .put("type", "INIT")
                .put("userId", "u1")
                .put("name", "Alice")
                .toString();
        handler.handleTextMessage(session, new TextMessage(initJson));

        // roll ≠ 12
        DiceManagerInterface mockDice = mock(DiceManagerInterface.class);
        when(mockDice.rollDices()).thenReturn(5);
        ReflectionTestUtils.setField(handler, "diceManager", mockDice);

        clearInvocations(session);

        // roll 1
        handler.handleTextMessage(session, new TextMessage("Roll"));

        // roll 2 (ungültig)
        clearInvocations(session);
        handler.handleTextMessage(session, new TextMessage("Roll"));

        // error
        verify(session).sendMessage(msgCaptor.capture());
        TextMessage errorMsg = msgCaptor.getValue();
        assertTrue(errorMsg.getPayload().contains("already rolled"));
    }
}
