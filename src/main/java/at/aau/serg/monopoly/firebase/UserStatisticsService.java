package at.aau.serg.monopoly.firebase;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
@Log
public class UserStatisticsService {

    private static final String USERS_COLLECTION = "users";
    private static final String GAME_HISTORY_COLLECTION = "gameHistory";

    public void updateStatsForUsers(List<String> userIds) {
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            if (firestore == null) {
                log.severe("Firestore ist nicht initialisiert");
                return;
            }

            for (String userId : userIds) {
                updateUserStats(userId, firestore);
            }
        } catch (Exception e) {
            log.severe("Fehler beim Aktualisieren der Benutzerstatistiken: " + e.getMessage());
        }
    }

    void updateUserStats(String userId, Firestore firestore) {
        try {
            CollectionReference historyRef = firestore.collection(USERS_COLLECTION)
                    .document(userId).collection(GAME_HISTORY_COLLECTION);
            ApiFuture<QuerySnapshot> historySnapshot = historyRef.get();
            List<QueryDocumentSnapshot> games = historySnapshot.get().getDocuments();

            if (games.isEmpty()) return;

            int wins = 0;
            int totalGames = games.size();
            int totalMoney = 0;
            int highestMoney = 0;

            for (DocumentSnapshot game : games) {
                Map<String, Object> gameData = game.getData();
                if (gameData == null) continue;

                if (Boolean.TRUE.equals(gameData.get("won"))) wins++;
                if (gameData.get("endMoney") != null) {
                    int endMoney = ((Number) gameData.get("endMoney")).intValue();
                    totalMoney += endMoney;
                    highestMoney = Math.max(highestMoney, endMoney);
                }
            }

            int averageMoney = totalGames > 0 ? totalMoney / totalGames : 0;
            int level = totalGames / 2;

            DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(userId);
            Map<String, Object> updates = new HashMap<>();
            updates.put("gamesPlayed", totalGames);
            updates.put("wins", wins);
            updates.put("level", level);
            updates.put("averageMoney", averageMoney);
            updates.put("highestMoney", highestMoney);

            DocumentSnapshot userDoc = userRef.get().get();
            if (userDoc.exists() && userDoc.contains("name")) {
                updates.put("name", userDoc.getString("name"));
            }

            userRef.set(updates, SetOptions.merge());

        } catch (InterruptedException | ExecutionException e) {
            log.severe("Fehler bei Statistiken für " + userId + ": " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}