package at.aau.serg.monopoly.websoket;
import model.Game;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;

import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final Game game = new Game();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        sessions.add(session);
        // Add player to game with a default name (can be updated later)
        game.addPlayer(session.getId(), "Player " + sessions.size());
        broadcastMessage("Player joined: " + session.getId() + " (Total: " + sessions.size() + ")");

        // Check if 2-4 players are connected
        if (sessions.size() >= 2 && sessions.size() <= 4) {
            startGame();
        }

        System.out.println("Player connected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        System.out.println("Received: " + payload);
        broadcastMessage("Player " + session.getId() + ": " + payload);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session,@NonNull CloseStatus status) {
        sessions.remove(session);
        broadcastMessage("Player left: " + session.getId() + " (Total: " + sessions.size() + ")");
        System.out.println("Player disconnected: " + session.getId());
    }

    private void broadcastMessage(String message) {
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                } else {
                    sessions.remove(session);  // Remove inactive sessions
                }
            } catch (Exception e) {
                System.err.println("Error sending message: " + e.getMessage());
            }
        }
    }

    private void startGame() {
        try {
            // Send initial game state to all players
            String gameState = objectMapper.writeValueAsString(game.getPlayerInfo());
            broadcastMessage("GAME_STATE:" + gameState);
            broadcastMessage("Game started! " + sessions.size() + " players are connected.");
            System.out.println("Game started with " + sessions.size() + " players!");
        } catch (Exception e) {
            System.err.println("Error sending game state: " + e.getMessage());
        }
    }
}

