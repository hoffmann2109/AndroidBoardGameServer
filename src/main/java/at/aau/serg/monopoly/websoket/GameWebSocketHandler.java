package at.aau.serg.monopoly.websoket;
import lombok.NonNull;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;

import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        sessions.add(session);
        broadcastMessage("Player joined: " + session.getId() + " (Total: " + sessions.size() + ")");

        // Check if four players are connected
        if (sessions.size() == 4) {
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
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                System.err.println("Error sending message: " + e.getMessage());
            }
        }
    }

    private void startGame() {
        broadcastMessage("Game started! All 4 players are connected.");
        System.out.println("Game started!");
    }
}

