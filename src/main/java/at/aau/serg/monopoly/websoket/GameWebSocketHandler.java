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
import model.Player;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {
    private final Logger logger = Logger.getLogger(GameWebSocketHandler.class.getName());
    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final Game game = new Game();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private DiceManagerInterface diceManager;

    @Autowired
    private PropertyTransactionService propertyTransactionService;

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
        String playerId = session.getId();

        try {
            if (payload.trim().equalsIgnoreCase("Roll")){
                int roll = diceManager.rollDices();
                logger.info("Player " + playerId + " rolled " + roll);

                DiceRollMessage drm = new DiceRollMessage(playerId, roll);
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
                    game.updatePlayerMoney(playerId, amount);
                    broadcastGameState();
                } catch (NumberFormatException e) {
                    System.err.println("Invalid money update format: " + payload);
                }
            } else if (payload.startsWith("BUY_PROPERTY:")) {
                handleBuyProperty(session, payload);
            } else {
                logger.info("Received unknown message format: " + payload + " from player " + playerId);
                broadcastMessage("Player " + playerId + ": " + payload);
            }
        } catch (Exception e) {
            logger.severe("Error handling message from player " + playerId + ": " + e.getMessage());
            sendMessageToSession(session, createJsonError("Server error processing your request."));
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

    private void handleBuyProperty(WebSocketSession session, String payload) {
        String playerId = session.getId();
        try {
            int propertyId = Integer.parseInt(payload.substring("BUY_PROPERTY:".length()));
            logger.info("Player " + playerId + " attempts to buy property " + propertyId);

            Optional<Player> playerOpt = game.getPlayerById(playerId);
            if (playerOpt.isEmpty()) {
                logger.warning("Player " + playerId + " not found in game state during buy attempt.");
                sendMessageToSession(session, createJsonError("Player not found."));
                return;
            }
            Player player = playerOpt.get();

            if (propertyTransactionService.canBuyProperty(player, propertyId)) {
                boolean success = propertyTransactionService.buyProperty(player, propertyId);
                if (success) {
                    logger.info("Property " + propertyId + " bought successfully by player " + playerId);
                    broadcastMessage(createJsonMessage("PROPERTY_BOUGHT", "Player " + playerId + " bought property " + propertyId));
                    broadcastGameState();
                } else {
                    logger.warning("Property purchase failed for player " + playerId + ", property " + propertyId + " after canBuy check.");
                    sendMessageToSession(session, createJsonError("Failed to buy property due to server error."));
                }
            } else {
                logger.info("Player " + playerId + " cannot buy property " + propertyId + " (insufficient funds or already owned).");
                sendMessageToSession(session, createJsonError("Cannot buy property (insufficient funds or already owned)."));
            }

        } catch (NumberFormatException e) {
            logger.warning("Invalid property ID format received from player " + playerId + ": " + payload);
            sendMessageToSession(session, createJsonError("Invalid property ID format."));
        } catch (Exception e) {
            logger.severe("Error handling BUY_PROPERTY for player " + playerId + ": " + e.getMessage());
            sendMessageToSession(session, createJsonError("Server error handling buy property request."));
        }
    }

    private void sendMessageToSession(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        } catch (Exception e) {
            logger.severe("Error sending message to session " + session.getId() + ": " + e.getMessage());
        }
    }

    private String createJsonError(String errorMessage) {
        return "{\"type\":\"ERROR\", \"message\":\"" + escapeJson(errorMessage) + "\"}";
    }

    private String createJsonMessage(String type, String message) {
        return "{\"type\":\""+ escapeJson(type) +"\", \"message\":\"" + escapeJson(message) + "\"}";
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

