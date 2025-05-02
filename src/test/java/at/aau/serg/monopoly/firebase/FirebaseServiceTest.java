package at.aau.serg.monopoly.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.ByteArrayInputStream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class FirebaseServiceTest {

    @InjectMocks
    private FirebaseService firebaseService;

    @Test
    void testInitializeWithoutCredentials() {
        FirebaseApp app = mock(FirebaseApp.class);
        assertDoesNotThrow(() -> firebaseService.initialize());
    }
}