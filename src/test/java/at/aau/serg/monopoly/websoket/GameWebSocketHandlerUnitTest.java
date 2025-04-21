package at.aau.serg.monopoly.websoket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.DiceRollMessage;
import model.DiceManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


class GameWebSocketHandlerUnitTest {

    private WebSocketSession session;
    private GameWebSocketHandler gameWebSocketHandler;

    @BeforeEach
    void setUp() {
        gameWebSocketHandler= new GameWebSocketHandler();
        session= mock(WebSocketSession.class);
        when(session.getId()).thenReturn("1");
        when(session.isOpen()).thenReturn(true);
    }

    @Test
    void testAfterConnectionEstablished() throws IOException {
        gameWebSocketHandler.afterConnectionEstablished(session);
        verify(session,  times(1)).sendMessage(new TextMessage("Player joined: 1 (Total: 1)"));
    }

    @Test
    void testGameStartTriggeredOnFourConnections() throws Exception {
        // Erstelle vier Mock-Sitzungen
        WebSocketSession session1 = mock(WebSocketSession.class);
        WebSocketSession session2 = mock(WebSocketSession.class);
        WebSocketSession session3 = mock(WebSocketSession.class);
        WebSocketSession session4 = mock(WebSocketSession.class);

        // Setze IDs und isOpen()-Status
        when(session1.getId()).thenReturn("1");
        when(session2.getId()).thenReturn("2");
        when(session3.getId()).thenReturn("3");
        when(session4.getId()).thenReturn("4");

        when(session1.isOpen()).thenReturn(true);
        when(session2.isOpen()).thenReturn(true);
        when(session3.isOpen()).thenReturn(true);
        when(session4.isOpen()).thenReturn(true);


        gameWebSocketHandler.afterConnectionEstablished(session1);
        gameWebSocketHandler.afterConnectionEstablished(session2);
        gameWebSocketHandler.afterConnectionEstablished(session3);
        gameWebSocketHandler.afterConnectionEstablished(session4);

        String expectedPayload = "Game started! 4 players are connected.";


        verify(session1, atLeastOnce()).sendMessage(argThat(msg -> msg.getPayload().equals(expectedPayload)));
        verify(session2, atLeastOnce()).sendMessage(argThat(msg -> msg.getPayload().equals(expectedPayload)));
        verify(session3, atLeastOnce()).sendMessage(argThat(msg -> msg.getPayload().equals(expectedPayload)));
        verify(session4, atLeastOnce()).sendMessage(argThat(msg -> msg.getPayload().equals(expectedPayload)));
    }


    @Test
    void testHandleTextMessage() throws Exception {
        TextMessage textMessage = new TextMessage("Test");
        gameWebSocketHandler.afterConnectionEstablished(session);
        gameWebSocketHandler.handleTextMessage(session,textMessage);
        verify(session, times(1)).sendMessage(new TextMessage("Player 1: Test"));

    }

    @Test
    void testAfterConnectionClosed() throws Exception{
        gameWebSocketHandler.afterConnectionClosed(session, CloseStatus.NORMAL);
        verify(session, never()).sendMessage(any());

    }

    @Test
    public void testRollDiceNormalCase() throws Exception {
        // Gemockte Instanz von DiceManager und Object-Mapper (JSON-Serialisierung)
        try (MockedConstruction<DiceManager> diceConstr = Mockito.mockConstruction(DiceManager.class,
                (mock, context) -> when(mock.rollDices()).thenReturn(12));
             MockedConstruction<ObjectMapper> omConstr = Mockito.mockConstruction(ObjectMapper.class,
                     (mock, context) -> when(mock.writeValueAsString(any()))
                             .thenReturn("{\"rolled\":12}"))) {

            GameWebSocketHandler handler = new GameWebSocketHandler();

            WebSocketSession session = mock(WebSocketSession.class);
            when(session.getId()).thenReturn("1");
            when(session.isOpen()).thenReturn(true);
            handler.afterConnectionEstablished(session);

            handler.handleTextMessage(session, new TextMessage("Roll"));

            DiceManager diceMock = diceConstr.constructed().get(0);
            ObjectMapper  omMock   = omConstr.constructed().get(0);

            verify(diceMock).rollDices();
            verify(omMock).writeValueAsString(any(DiceRollMessage.class));
        }
    }

    @Test
    void testRollDiceErrorCase() throws Exception {
        // Gemockte Instanz von DiceManager und Object-Mapper (JSON-Serialisierung)
        try (MockedConstruction<ObjectMapper> omConstr = mockConstruction(ObjectMapper.class,
                (mock, context) -> when(mock.writeValueAsString(any()))
                        .thenThrow(new JsonProcessingException("Serialization Failure!") {
                        }));
             MockedConstruction<model.DiceManager> diceConstr = mockConstruction(model.DiceManager.class,
                     (mock, context) -> when(mock.rollDices()).thenReturn(12))) {

            GameWebSocketHandler handler = new GameWebSocketHandler();

            WebSocketSession session = mock(WebSocketSession.class);
            when(session.getId()).thenReturn("sessionX");
            when(session.isOpen()).thenReturn(true);
            handler.afterConnectionEstablished(session);

            assertThrows(RuntimeException.class, () ->
                    handler.handleTextMessage(session, new TextMessage("Roll"))
            );
        }
    }

    @Test
    void testAfterConnectionClosedWithRemainingSessions() throws IOException {

        WebSocketSession session2 = mock(WebSocketSession.class);


        when(session.getId()).thenReturn("1");
        when(session2.getId()).thenReturn("2");


        when(session2.isOpen()).thenReturn(true);


        gameWebSocketHandler.afterConnectionEstablished(session);
        gameWebSocketHandler.afterConnectionEstablished(session2);


        gameWebSocketHandler.afterConnectionClosed(session, CloseStatus.NORMAL);


        verify(session, times(1)).sendMessage(new TextMessage("Player joined: 1 (Total: 1)"));
        verify(session2, times(1)).sendMessage(new TextMessage("Player joined: 2 (Total: 2)"));
        verify(session, times(1)).sendMessage(new TextMessage("Player joined: 2 (Total: 2)"));
        verify(session2, times(1)).sendMessage(new TextMessage("Player left: 1 (Total: 1)"));
    }

    @Test
    void testHandleUpdateMoneyMessage() throws Exception {
        // Setup
        gameWebSocketHandler.afterConnectionEstablished(session);
        
        // Test successful money update
        TextMessage validMoneyMessage = new TextMessage("UPDATE_MONEY:500");
        gameWebSocketHandler.handleTextMessage(session, validMoneyMessage);
        
        // Verify that the game state was broadcast
        verify(session, atLeastOnce()).sendMessage(argThat(msg -> 
            ((TextMessage)msg).getPayload().startsWith("GAME_STATE:")));
    }

    @Test
    void testHandleInvalidUpdateMoneyMessage() throws Exception {
        // Setup
        gameWebSocketHandler.afterConnectionEstablished(session);
        
        // Test invalid money format
        TextMessage invalidMoneyMessage = new TextMessage("UPDATE_MONEY:notanumber");
        gameWebSocketHandler.handleTextMessage(session, invalidMoneyMessage);
        
        // Verify that no game state was broadcast for invalid format
        verify(session, never()).sendMessage(argThat(msg -> 
            ((TextMessage)msg).getPayload().startsWith("GAME_STATE:")));
    }

    @Test
    void testHandleUpdateMoneyMessageWithNegativeAmount() throws Exception {
        // Setup
        gameWebSocketHandler.afterConnectionEstablished(session);
        
        // Test negative money update
        TextMessage negativeMoneyMessage = new TextMessage("UPDATE_MONEY:-300");
        gameWebSocketHandler.handleTextMessage(session, negativeMoneyMessage);
        
        // Verify that the game state was broadcast
        verify(session, atLeastOnce()).sendMessage(argThat(msg -> 
            ((TextMessage)msg).getPayload().startsWith("GAME_STATE:")));
    }

    @Test
    void testHandleUpdateMoneyMessageWithZeroAmount() throws Exception {
        // Setup
        gameWebSocketHandler.afterConnectionEstablished(session);
        
        // Test zero money update
        TextMessage zeroMoneyMessage = new TextMessage("UPDATE_MONEY:0");
        gameWebSocketHandler.handleTextMessage(session, zeroMoneyMessage);
        
        // Verify that the game state was broadcast
        verify(session, atLeastOnce()).sendMessage(argThat(msg -> 
            ((TextMessage)msg).getPayload().startsWith("GAME_STATE:")));
    }

    @Test
    void testGameStateBroadcastOnPlayerJoin() throws Exception {
        // Setup first player
        gameWebSocketHandler.afterConnectionEstablished(session);
        
        // Verify initial join message
        verify(session, times(1)).sendMessage(new TextMessage("Player joined: 1 (Total: 1)"));
        
        // Setup second player
        WebSocketSession session2 = mock(WebSocketSession.class);
        when(session2.getId()).thenReturn("2");
        when(session2.isOpen()).thenReturn(true);
        gameWebSocketHandler.afterConnectionEstablished(session2);
        
        // Verify messages for both players
        verify(session, times(1)).sendMessage(new TextMessage("Player joined: 2 (Total: 2)"));
        verify(session2, times(1)).sendMessage(new TextMessage("Player joined: 2 (Total: 2)"));
        
        // Verify game state broadcasts
        verify(session, atLeastOnce()).sendMessage(argThat(msg -> {
            String payload = ((TextMessage)msg).getPayload();
            return payload.startsWith("GAME_STATE:") && 
                   payload.contains("\"id\":\"1\"") && 
                   payload.contains("\"id\":\"2\"");
        }));
        
        verify(session2, atLeastOnce()).sendMessage(argThat(msg -> {
            String payload = ((TextMessage)msg).getPayload();
            return payload.startsWith("GAME_STATE:") && 
                   payload.contains("\"id\":\"1\"") && 
                   payload.contains("\"id\":\"2\"");
        }));
    }

    @Test
    void testGameStateBroadcastOnMoneyUpdate() throws Exception {
        // Setup two players
        gameWebSocketHandler.afterConnectionEstablished(session);
        WebSocketSession session2 = mock(WebSocketSession.class);
        when(session2.getId()).thenReturn("2");
        when(session2.isOpen()).thenReturn(true);
        gameWebSocketHandler.afterConnectionEstablished(session2);
        
        // Update money for first player
        TextMessage moneyMessage = new TextMessage("UPDATE_MONEY:500");
        gameWebSocketHandler.handleTextMessage(session, moneyMessage);
        
        // Verify game state broadcast to both players
        verify(session, atLeastOnce()).sendMessage(argThat(msg -> 
            ((TextMessage)msg).getPayload().startsWith("GAME_STATE:")));
        verify(session2, atLeastOnce()).sendMessage(argThat(msg -> 
            ((TextMessage)msg).getPayload().startsWith("GAME_STATE:")));
    }

    @Test
    void testGameStateBroadcastOnGameStart() throws Exception {
        // Setup four players to trigger game start
        WebSocketSession session1 = mock(WebSocketSession.class);
        WebSocketSession session2 = mock(WebSocketSession.class);
        WebSocketSession session3 = mock(WebSocketSession.class);
        WebSocketSession session4 = mock(WebSocketSession.class);
        
        when(session1.getId()).thenReturn("1");
        when(session2.getId()).thenReturn("2");
        when(session3.getId()).thenReturn("3");
        when(session4.getId()).thenReturn("4");
        
        when(session1.isOpen()).thenReturn(true);
        when(session2.isOpen()).thenReturn(true);
        when(session3.isOpen()).thenReturn(true);
        when(session4.isOpen()).thenReturn(true);
        
        // Connect all players
        gameWebSocketHandler.afterConnectionEstablished(session1);
        gameWebSocketHandler.afterConnectionEstablished(session2);
        gameWebSocketHandler.afterConnectionEstablished(session3);
        gameWebSocketHandler.afterConnectionEstablished(session4);
        
        // Verify game state broadcast to all players
        verify(session1, atLeastOnce()).sendMessage(argThat(msg -> 
            ((TextMessage)msg).getPayload().startsWith("GAME_STATE:")));
        verify(session2, atLeastOnce()).sendMessage(argThat(msg -> 
            ((TextMessage)msg).getPayload().startsWith("GAME_STATE:")));
        verify(session3, atLeastOnce()).sendMessage(argThat(msg -> 
            ((TextMessage)msg).getPayload().startsWith("GAME_STATE:")));
        verify(session4, atLeastOnce()).sendMessage(argThat(msg -> 
            ((TextMessage)msg).getPayload().startsWith("GAME_STATE:")));
    }

    @AfterEach
    void tearDown() {
        gameWebSocketHandler=null;
        session= null;
    }
}
