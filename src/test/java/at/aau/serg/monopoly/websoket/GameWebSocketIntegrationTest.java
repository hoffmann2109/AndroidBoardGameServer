package at.aau.serg.monopoly.websoket;
import model.PlayerInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GameWebSocketIntegrationTest {
    @LocalServerPort
    private int port;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testGameStartWithTwoPlayers() throws Exception {
        // Create two WebSocket clients
        StandardWebSocketClient client1 = new StandardWebSocketClient();
        StandardWebSocketClient client2 = new StandardWebSocketClient();

        // Create futures to store received messages
        CompletableFuture<String> client1Future = new CompletableFuture<>();
        CompletableFuture<String> client2Future = new CompletableFuture<>();
        List<String> client1Messages = new ArrayList<>();
        List<String> client2Messages = new ArrayList<>();

        // Connect first client
        WebSocketSession session1 = client1.doHandshake(new TestWebSocketHandler(client1Future, client1Messages),
                "ws://localhost:" + port + "/monopoly").get(5, TimeUnit.SECONDS);

        // Connect second client
        WebSocketSession session2 = client2.doHandshake(new TestWebSocketHandler(client2Future, client2Messages),
                "ws://localhost:" + port + "/monopoly").get(5, TimeUnit.SECONDS);

        // Wait for game state messages
        String gameState1 = client1Future.get(5, TimeUnit.SECONDS);
        String gameState2 = client2Future.get(5, TimeUnit.SECONDS);

        // Verify game state messages
        assertThat(gameState1).startsWith("GAME_STATE:");
        assertThat(gameState2).startsWith("GAME_STATE:");

        // Parse and verify player information
        String json1 = gameState1.substring("GAME_STATE:".length());
        String json2 = gameState2.substring("GAME_STATE:".length());

        List<PlayerInfo> players1 = objectMapper.readValue(json1,
                objectMapper.getTypeFactory().constructCollectionType(List.class, PlayerInfo.class));
        List<PlayerInfo> players2 = objectMapper.readValue(json2,
                objectMapper.getTypeFactory().constructCollectionType(List.class, PlayerInfo.class));

        // Verify that we have at least 2 players and at most 4 players
        assertThat(players1.size()).isBetween(2, 4);
        assertThat(players2.size()).isBetween(2, 4);

        // Verify that all players have the correct starting money
        for (PlayerInfo player : players1) {
            assertThat(player.getMoney()).isEqualTo(1500);
        }

        // Clean up
        session1.close();
        session2.close();
    }

    private static class TestWebSocketHandler extends org.springframework.web.socket.handler.AbstractWebSocketHandler {
        private final CompletableFuture<String> future;
        private final List<String> messages;

        public TestWebSocketHandler(CompletableFuture<String> future, List<String> messages) {
            this.future = future;
            this.messages = messages;
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) {
            String payload = message.getPayload();
            messages.add(payload);
            if (payload.startsWith("GAME_STATE:")) {
                future.complete(payload);
            }
        }
    }
}