package at.aau.serg.monopoly.websoket;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class GameWebSocketHandlerUnitTest {

    private WebSocketSession session;
    private GameWebSocketHandler gameWebSocketHandler;

    @BeforeEach
    void setUp() {
        gameWebSocketHandler= new GameWebSocketHandler();
        session= mock(WebSocketSession.class);
        when(session.getId()).thenReturn("1");
    }



    @Test
    void testAfterConnectionEstablished() throws IOException {
        gameWebSocketHandler.afterConnectionEstablished(session);
        verify(session,  times(1)).sendMessage(new TextMessage("Player joined: 1 (Total: 1)"));
    }

    @Test
    void testHandleTextMessage() throws Exception {
        TextMessage textMessage = new TextMessage("Test");
        gameWebSocketHandler.afterConnectionEstablished(session);
        gameWebSocketHandler.handleTextMessage(session,textMessage);
        verify(session, times(1)).sendMessage(new TextMessage("Player 1: Test"));

    }

    @Test
    void afterConnectionClosed() {
    }

    @AfterEach
    void tearDown() {
        gameWebSocketHandler=null;
        session= null;
    }
}