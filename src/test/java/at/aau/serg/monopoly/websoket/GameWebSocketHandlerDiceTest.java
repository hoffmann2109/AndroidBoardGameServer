package at.aau.serg.monopoly.websoket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.DiceManagerInterface;
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
}
