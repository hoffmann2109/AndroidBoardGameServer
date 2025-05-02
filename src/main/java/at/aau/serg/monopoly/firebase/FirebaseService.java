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
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class FirebaseService {

    private static final Logger logger = Logger.getLogger(FirebaseService.class.getName());

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                // Versuchen Sie mehrere Pfade
                InputStream serviceAccount = getClass().getClassLoader()
                        .getResourceAsStream("serviceAccountKey.json");

                if (serviceAccount == null) {
                    serviceAccount = getClass().getClassLoader()
                            .getResourceAsStream("/serviceAccountKey.json");
                }

                if (serviceAccount == null) {
                    serviceAccount = new FileInputStream(new File("serviceAccountKey.json"));
                }

                if (serviceAccount != null) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .setDatabaseUrl("https://monopoloyapp.firebaseio.com")
                            .build();

                    FirebaseApp.initializeApp(options);
                    logger.info("Firebase wurde erfolgreich initialisiert");

                    // Test-Verbindung zum Firestore
                    try {
                        Firestore firestore = FirestoreClient.getFirestore();
                        firestore.collection("test").document("test").set(
                                Collections.singletonMap("timestamp", new Date().toString())
                        ).get(); // Blockierend ausführen, um Fehler sofort zu erkennen
                        logger.info("Firestore-Verbindung erfolgreich getestet");
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Firestore-Verbindungstest fehlgeschlagen", e);
                    }
                } else {
                    logger.log(Level.SEVERE, "serviceAccountKey.json konnte nicht geladen werden. Überprüfen Sie den Pfad und die Verfügbarkeit der Datei.");
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Fehler bei der Firebase-Initialisierung: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unerwarteter Fehler bei der Firebase-Initialisierung: " + e.getMessage(), e);
        }
    }
}