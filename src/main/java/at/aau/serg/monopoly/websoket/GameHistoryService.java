package at.aau.serg.monopoly.websoket;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import model.GameHistory;
import model.Player;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class GameHistoryService {

    private static final Logger logger = Logger.getLogger(GameHistoryService.class.getName());
    private static final String COLLECTION_NAME = "users";
    private static final String SUBCOLLECTION_NAME = "gameHistory";


    private void ensureGameHistorySubcollection(String userId) {
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            DocumentReference userDocRef = firestore.collection(COLLECTION_NAME).document(userId);

            ApiFuture<DocumentSnapshot> future = userDocRef.get();
            DocumentSnapshot document = future.get();

            if (!document.exists()) {
                userDocRef.set(Collections.emptyMap()).get();
                logger.log(Level.INFO, "Benutzerdokument-gameHistory für {0} angelegt", userId);
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.WARNING, "Fehler beim Prüfen der Subcollection für Benutzer: " + userId, e);
            Thread.currentThread().interrupt();
        }
    }



    /**
     * Speichert die Spielhistorie für einen bestimmten Spieler
     *
     * @param userId          Die ID des Spielers
     * @param durationMinutes Die Dauer des Spiels in Minuten
     * @param endMoney        Das Geld des Spielers am Ende
     * @param levelGained     Die gewonnenen Level
     * @param won             Ob der Spieler gewonnen hat
     * @return true, wenn das Speichern erfolgreich war, sonst false
     */
    public boolean saveGameHistory(String userId, int durationMinutes, int endMoney,
                                   int levelGained, boolean won) {



        try {
            ensureGameHistorySubcollection(userId);
            Firestore firestore = FirestoreClient.getFirestore();

            GameHistory gameHistory = new GameHistory();
            gameHistory.setId(UUID.randomUUID().toString());
            gameHistory.setUserId(userId);
            gameHistory.setDurationMinutes(durationMinutes);
            gameHistory.setEndMoney(endMoney);
            gameHistory.setLevelGained(levelGained);
            gameHistory.setTimestamp(new Date());
            gameHistory.setWon(won);

            // Pfad: users/UID/gameHistory/ID
            ApiFuture<WriteResult> result = firestore.collection(COLLECTION_NAME)
                    .document(userId)
                    .collection(SUBCOLLECTION_NAME)
                    .document(gameHistory.getId())
                    .set(gameHistory);

            result.get(); // Warten auf das Ergebnis
            logger.log(Level.INFO, "Spielhistorie für Benutzer {0} erfolgreich gespeichert", userId);
            return true;
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Fehler beim Speichern der Spielhistorie für Benutzer {0}", userId);
            Thread.currentThread().interrupt(); // Guter Umgang mit InterruptedException
            return false;
        }
    }


    /**
     * Speichert die Spielhistorie für alle Spieler die am Ende eines Spiels übrig sind
     * aktuell nur ein Gewinner möglich - aber für andere Versionen des Spiels bereits so angelegt, dass es auch mehrere Spieler geben kann am Ende
     *
     * @param players         Die Liste der Spieler
     * @param durationMinutes Die Dauer des Spiels in Minuten
     * @param winnerId        Die ID des Gewinners (oder null, wenn kein Gewinner)
     * @param levelGained     Die gewonnenen Level
     */
    public void saveGameHistoryForAllPlayers(java.util.List<Player> players, int durationMinutes,
                                             String winnerId, int levelGained) {
        if (players == null || players.isEmpty()) {
            logger.warning("Keine Spieler zum Speichern der Spielhistorie vorhanden");
            return;
        }


        for (Player player : players) {
            boolean won = player.getId().equals(winnerId);
            saveGameHistory(
                    player.getId(),
                    durationMinutes,
                    player.getMoney(),
                    levelGained,
                    won
            );
        }

        logger.info("Spielhistorie für alle Spieler gespeichert");
    }

    /**
     * Speichert einen Spielabbruch (Give Up) als verlorenes Spiel für einen Spieler
     */
    public void markPlayerAsLoser(String userId) {
        try {
            ensureGameHistorySubcollection(userId);

            Firestore firestore = FirestoreClient.getFirestore();

            GameHistory gameHistory = new GameHistory();
            gameHistory.setId(UUID.randomUUID().toString());
            gameHistory.setUserId(userId);
            gameHistory.setDurationMinutes(0); // Spiel wurde aufgegeben - verhindert verfälschte Statistiken
            gameHistory.setEndMoney(0); // damit nicht die Statistik verfälscht wird durch gezieltes Nutzten des mechanismus
            gameHistory.setLevelGained(0); // kein Levelgewinn
            gameHistory.setTimestamp(new Date());
            gameHistory.setWon(false); // Spieler hat verloren

            ApiFuture<WriteResult> result = firestore.collection(COLLECTION_NAME)
                    .document(userId)
                    .collection(SUBCOLLECTION_NAME)
                    .document(gameHistory.getId())
                    .set(gameHistory);

            result.get(); // warten auf Abschluss
            logger.info("Spielabbruch als Niederlage für " + userId + " gespeichert.");
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Fehler beim Speichern des Spielabbruchs für " + userId, e);
            Thread.currentThread().interrupt();
        }
    }
}