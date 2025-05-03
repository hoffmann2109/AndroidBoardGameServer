package at.aau.serg.monopoly.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class FirebaseService {

    private static final Logger logger = Logger.getLogger(FirebaseService.class.getName());
    private static final int FIRESTORE_TIMEOUT = 10; // in Sekunden

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                handleFirebaseInitialization();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.SEVERE, "Firebase-Initialisierung wurde unterbrochen", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Kritischer Fehler bei der Firebase-Initialisierung", e);
        }
    }

    private void handleFirebaseInitialization() throws IOException, InterruptedException {
        InputStream serviceAccount = null;
        try {
            serviceAccount = locateServiceAccountKey();

            if (serviceAccount != null) {
                initializeFirebaseApp(serviceAccount);
                testFirestoreConnection();
            } else {
                logger.log(Level.SEVERE, "serviceAccountKey.json konnte nicht gefunden werden");
            }
        } finally {
            if (serviceAccount != null) {
                serviceAccount.close();
            }
        }
    }

    InputStream locateServiceAccountKey() throws IOException {
        // Versuche verschiedene Pfade
        InputStream stream = getClass().getClassLoader().getResourceAsStream("serviceAccountKey.json");
        if (stream == null) stream = new FileInputStream(new File("serviceAccountKey.json"));
        return stream;
    }

    private void initializeFirebaseApp(InputStream serviceAccount) throws IOException {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://monopolyapp-11fb3.firebaseio.com").build();

        FirebaseApp.initializeApp(options);
        logger.info("Firebase wurde erfolgreich initialisiert");
    }

    private void testFirestoreConnection() {
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            String testDocId = "connection_test_" + UUID.randomUUID();

            // Test schreiben
            firestore.collection("connection_tests")
                    .document(testDocId)
                    .set(Collections.singletonMap("timestamp", new Date()))
                    .get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

            logger.info("Firestore-Verbindung erfolgreich getestet");

            // Aufräumen
            firestore.collection("connection_tests").document(testDocId).delete();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.SEVERE, "Firestore-Verbindungstest unterbrochen", e);
        } catch (TimeoutException e) {
            logger.log(Level.SEVERE, "Firestore-Verbindungstest timeout", e);
        } catch (ExecutionException e) {
            logger.log(Level.SEVERE, "Firestore-Verbindungstest fehlgeschlagen", e);
        }
    }
}