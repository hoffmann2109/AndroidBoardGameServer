package at.aau.serg.monopoly.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class FirebaseService {
    
    private static final Logger logger = Logger.getLogger(FirebaseService.class.getName());
    
    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = getClass().getClassLoader()
                    .getResourceAsStream("serviceAccountKey.json");
                
                if (serviceAccount != null) {
                    FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setDatabaseUrl("https://monopoloyapp.firebaseio.com")
                        .build();
                    
                    FirebaseApp.initializeApp(options);
                    logger.info("Firebase wurde erfolgreich initialisiert");
                } else {
                    logger.log(Level.SEVERE, "serviceAccountKey.json konnte nicht geladen werden");
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Fehler bei der Firebase-Initialisierung", e);
        }
    }
}