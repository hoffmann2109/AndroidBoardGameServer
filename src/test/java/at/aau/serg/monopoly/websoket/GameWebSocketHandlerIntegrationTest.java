package at.aau.serg.monopoly.websoket;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GameWebSocketHandlerIntegrationTest {
    @LocalServerPort
    private int port;

    @Test
    void testWebSocketConnection() throws Exception {
        String url = "ws://localhost:" + port + "/monopoly";
        CompletableFuture<String> future = new CompletableFuture<>();


        StandardWebSocketClient client = new StandardWebSocketClient();


        client.doHandshake(new AbstractWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                System.out.println("message received " + message.getPayload()); //bewusst geloggt aktuell
                future.complete(message.getPayload());
            }

            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                System.out.println("Connection created!");
                future.complete("Connection created!");
            }
        }, url).get();


        String receivedMessage = future.get(5, TimeUnit.SECONDS);
        assertThat(receivedMessage).isEqualTo("Connection created!");
    }
}
