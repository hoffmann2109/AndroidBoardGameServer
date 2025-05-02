package at.aau.serg.monopoly.websoket;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import model.GameHistory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameHistoryServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Firestore firestore;

    @InjectMocks
    private GameHistoryService gameHistoryService;

    @Test
    void testSaveGameHistory() throws Exception {
        // Step 1: Mock Firestore chain
        Firestore firestoreMock = mock(Firestore.class);
        CollectionReference users = mock(CollectionReference.class);
        DocumentReference userDoc = mock(DocumentReference.class);
        CollectionReference gameHistory = mock(CollectionReference.class);
        DocumentReference gameDoc = mock(DocumentReference.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);

        // Step 2: Build chain
        when(firestoreMock.collection("users")).thenReturn(users);
        when(users.document("123")).thenReturn(userDoc);
        when(userDoc.collection("gameHistory")).thenReturn(gameHistory);
        when(gameHistory.document(anyString())).thenReturn(gameDoc);
        when(gameDoc.set(any(GameHistory.class))).thenReturn(writeFuture);

        // Step 3: Replace static FirestoreClient.getFirestore()
        try (MockedStatic<FirestoreClient> firestoreClient = Mockito.mockStatic(FirestoreClient.class)) {
            firestoreClient.when(FirestoreClient::getFirestore).thenReturn(firestoreMock);

            // Step 4: Execute test
            GameHistoryService service = new GameHistoryService();
            boolean result = service.saveGameHistory("123", 30, 2500, 2, 4, true);

            // Step 5: Verify call
            assertTrue(result);
            verify(gameDoc).set(any(GameHistory.class));
        }
    }
}