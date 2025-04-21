package at.aau.serg.monopoly.websoket;
import com.fasterxml.jackson.core.JsonProcessingException;
import data.DiceRollMessage;
import model.DiceManager;
import model.DiceManagerInterface;
import model.Game;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;
import java.util.logging.Logger;

import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {
    private final Logger logger = Logger.getLogger(GameWebSocketHandler.class.getName());
    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final Game game = new Game();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private DiceManagerInterface diceManager;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        sessions.add(session);
        // Add player to game with a default name (can be updated later)
        game.addPlayer(session.getId(), "Player " + sessions.size());
        broadcastMessage("Player joined: " + session.getId() + " (Total: " + sessions.size() + ")");

        diceManager = new DiceManager();
        diceManager.initializeStandardDices();
        // Check if 2-4 players are connected
        if (sessions.size() >= 2 && sessions.size() <= 4) {
            startGame();
        }

        System.out.println("Player connected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        if (payload.trim().equalsIgnoreCase("Roll")){
            int roll = diceManager.rollDices();
            logger.info("Player " + session.getId() + " rolled " + roll);

            DiceRollMessage drm = new DiceRollMessage(session.getId(), roll);
            String json = null;
            try {
                json = objectMapper.writeValueAsString(drm);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to serialize DiceRollMessage", e);
            }
            broadcastMessage(json);
        } else if (payload.startsWith("UPDATE_MONEY:")) {
            try {
                int amount = Integer.parseInt(payload.substring("UPDATE_MONEY:".length()));
                game.updatePlayerMoney(session.getId(), amount);
                broadcastGameState();
            } catch (NumberFormatException e) {
                System.err.println("Invalid money update format: " + payload);
            }
        } else {
            System.out.println("Received: " + payload);
            broadcastMessage("Player " + session.getId() + ": " + payload);
        }
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

    private void broadcastGameState() {
        try {
            String gameState = objectMapper.writeValueAsString(game.getPlayerInfo());
            broadcastMessage("GAME_STATE:" + gameState);
        } catch (Exception e) {
            System.err.println("Error broadcasting game state: " + e.getMessage());
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

