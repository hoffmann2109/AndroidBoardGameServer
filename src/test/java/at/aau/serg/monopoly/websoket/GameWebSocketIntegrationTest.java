package at.aau.serg.monopoly.websoket;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.PlayerInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GameWebSocketIntegrationTest {
    @LocalServerPort
    private int port;
    private static StandardWebSocketClient client1;
    private static StandardWebSocketClient client2;
    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    static void initClients() {
        client1 = new StandardWebSocketClient();
        client2 = new StandardWebSocketClient();
    }

    @AfterAll
    static void cleanupClients() {
        // no-op
    }

    @Test
    void testGameStartWithTwoPlayers() throws Exception {

        CompletableFuture<String> stateFuture1 = new CompletableFuture<>();
        CompletableFuture<String> stateFuture2 = new CompletableFuture<>();

        // Connect client1
        WebSocketSession session1 = client1.doHandshake(
                new CountingWebSocketHandler(stateFuture1) {
                    @Override
                    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                        session.sendMessage(new TextMessage("{\"type\":\"INIT\",\"userId\":\"1\",\"name\":\"Player1\"}"));
                    }
                }, String.valueOf(new URI("ws://localhost:" + port + "/monopoly"))).get(10, TimeUnit.SECONDS);

        // Pause to ensure the first broadcast completes before connecting the second client
        Thread.sleep(1000);

        // Connect client2
        WebSocketSession session2 = client2.doHandshake(
                new CountingWebSocketHandler(stateFuture2) {
                    @Override
                    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                        session.sendMessage(new TextMessage("{\"type\":\"INIT\",\"userId\":\"2\",\"name\":\"Player2\"}"));
                    }
                }, String.valueOf(new URI("ws://localhost:" + port + "/monopoly"))).get(10, TimeUnit.SECONDS);

        // Await the GAME_STATE for each client
        String state1 = stateFuture1.get(10, TimeUnit.SECONDS);
        String state2 = stateFuture2.get(10, TimeUnit.SECONDS);
        assertThat(state1).startsWith("GAME_STATE:");
        assertThat(state2).startsWith("GAME_STATE:");


        // Parse and verify 2 players
        String json1 = state1.substring("GAME_STATE:".length());
        List<PlayerInfo> players1 = mapper.readValue(
                json1, mapper.getTypeFactory().constructCollectionType(List.class, PlayerInfo.class)
        );
        assertThat(players1).hasSize(2);
        players1.forEach(p -> assertThat(p.getMoney()).isEqualTo(1500));

        // Cleanup
        session1.close(CloseStatus.NORMAL);
        session2.close(CloseStatus.NORMAL);
    }

    /**
     * A WebSocketHandler that only completes its future on the Nth GAME_STATE message.
     */
    private abstract static class CountingWebSocketHandler extends AbstractWebSocketHandler {
        private final CompletableFuture<String> future;

        public CountingWebSocketHandler(CompletableFuture<String> future) {
            this.future = future;
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            String payload = message.getPayload();
            if (!payload.startsWith("GAME_STATE:")) return;

            String json = payload.substring("GAME_STATE:".length());
            List<PlayerInfo> players =
                    mapper.readValue(json, mapper.getTypeFactory()
                            .constructCollectionType(List.class, PlayerInfo.class));

            if (players.size() == 2) {
                future.complete(payload);
            }
        }
    }
}
