package at.aau.serg.monopoly.firebase;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.java.Log;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
@EnableScheduling
@Log
public class LeaderboardService {

    private static final String USERS_COLLECTION = "users";
    private static final String LEADERBOARD_WINS = "leaderboard_wins";
    private static final String LEADERBOARD_LEVEL = "leaderboard_level";
    private static final String LEADERBOARD_MONEY = "leaderboard_averageMoney";
    private static final String LEADERBOARD_HIGH_MONEY = "leaderboard_highestMoney";
    private static final String LEADERBOARD_GAMES_PLAYED = "leaderboard_gamesPlayed";
    private static final int LEADERBOARD_SIZE = 50;

    @Scheduled(fixedRate = 86400000)
    public void updateAllLeaderboards() {
        log.info("Starte Leaderboard-Aktualisierung: " + new Date());
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            if (firestore == null) {
                log.severe("Firestore ist nicht initialisiert");
                return;
            }

            updateWinsLeaderboard(firestore);
            updateLevelLeaderboard(firestore);
            updateMoneyLeaderboard(firestore);
            updateHighMoneyLeaderboard(firestore);
            updateGamesPlayedLeaderboard(firestore);

            log.info("Leaderboard-Aktualisierung abgeschlossen");
        } catch (Exception e) {
            log.severe("Fehler bei Leaderboard-Aktualisierung: " + e.getMessage());
        }
    }

    void updateWinsLeaderboard(Firestore firestore) {
        updateLeaderboard(firestore, "wins", LEADERBOARD_WINS);
    }

    void updateLevelLeaderboard(Firestore firestore) {
        updateLeaderboard(firestore, "level", LEADERBOARD_LEVEL);
    }

    void updateMoneyLeaderboard(Firestore firestore) {
        updateLeaderboard(firestore, "averageMoney", LEADERBOARD_MONEY);
    }

    void updateHighMoneyLeaderboard(Firestore firestore) {
        updateLeaderboard(firestore, "highestMoney", LEADERBOARD_HIGH_MONEY);
    }

    void updateGamesPlayedLeaderboard(Firestore firestore) {
        updateLeaderboard(firestore, "gamesPlayed", LEADERBOARD_GAMES_PLAYED);
    }

    void updateLeaderboard(Firestore firestore, String fieldName, String leaderboardCollection) {
        try {
            ApiFuture<QuerySnapshot> query = firestore.collection(USERS_COLLECTION)
                    .orderBy(fieldName, Query.Direction.DESCENDING)
                    .limit(LEADERBOARD_SIZE)
                    .get();

            List<QueryDocumentSnapshot> users = query.get().getDocuments();
            deleteCollection(firestore, leaderboardCollection);

            int rank = 1;
            for (DocumentSnapshot user : users) {
                Map<String, Object> userData = user.getData();
                if (userData == null) continue;

                Map<String, Object> entry = new HashMap<>();
                entry.put("userId", user.getId());
                entry.put("name", userData.getOrDefault("name", "Unbekannt"));
                entry.put("rank", rank);
                entry.put(fieldName, userData.getOrDefault(fieldName, 0));

                firestore.collection(leaderboardCollection)
                        .document(String.valueOf(rank))
                        .set(entry);
                rank++;
            }

            log.info(leaderboardCollection + " aktualisiert");
        } catch (InterruptedException | ExecutionException e) {
            log.severe("Fehler bei " + leaderboardCollection + ": " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    void deleteCollection(Firestore firestore, String collectionPath) throws ExecutionException, InterruptedException {
        CollectionReference collection = firestore.collection(collectionPath);
        ApiFuture<QuerySnapshot> future = collection.limit(100).get();
        for (DocumentSnapshot document : future.get().getDocuments()) {
            document.getReference().delete();
        }
    }
}