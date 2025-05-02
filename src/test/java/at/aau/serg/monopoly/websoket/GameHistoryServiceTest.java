package at.aau.serg.monopoly.websoket;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import model.GameHistory;
import model.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameHistoryServiceTest {

    @Test
    void testSaveGameHistory_successful() throws Exception {
        // Arrange
        Firestore firestore = mock(Firestore.class);
        CollectionReference users = mock(CollectionReference.class);
        DocumentReference userDoc = mock(DocumentReference.class);
        CollectionReference history = mock(CollectionReference.class);
        DocumentReference gameDoc = mock(DocumentReference.class);
        ApiFuture<WriteResult> future = mock(ApiFuture.class);

        when(firestore.collection("users")).thenReturn(users);
        when(users.document("123")).thenReturn(userDoc);
        when(userDoc.collection("gameHistory")).thenReturn(history);
        when(history.document(anyString())).thenReturn(gameDoc);
        when(gameDoc.set(any(GameHistory.class))).thenReturn(future);
        when(future.get()).thenReturn(mock(WriteResult.class));

        try (MockedStatic<FirestoreClient> client = Mockito.mockStatic(FirestoreClient.class)) {
            client.when(FirestoreClient::getFirestore).thenReturn(firestore);

            GameHistoryService service = new GameHistoryService();
            boolean result = service.saveGameHistory("123", 45, 1500, 1, 4, false);

            assertTrue(result);
            verify(gameDoc).set(any(GameHistory.class));
        }
    }

    @Test
    void testSaveGameHistory_executionException() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference users = mock(CollectionReference.class);
        DocumentReference userDoc = mock(DocumentReference.class);
        CollectionReference history = mock(CollectionReference.class);
        DocumentReference gameDoc = mock(DocumentReference.class);
        ApiFuture<WriteResult> future = mock(ApiFuture.class);

        when(firestore.collection("users")).thenReturn(users);
        when(users.document("123")).thenReturn(userDoc);
        when(userDoc.collection("gameHistory")).thenReturn(history);
        when(history.document(anyString())).thenReturn(gameDoc);
        when(gameDoc.set(any(GameHistory.class))).thenReturn(future);
        when(future.get()).thenThrow(new ExecutionException("failed", new Throwable()));

        try (MockedStatic<FirestoreClient> client = Mockito.mockStatic(FirestoreClient.class)) {
            client.when(FirestoreClient::getFirestore).thenReturn(firestore);

            GameHistoryService service = new GameHistoryService();
            boolean result = service.saveGameHistory("123", 30, 1000, 1, 2, true);

            assertFalse(result);
        }
    }

    @Test
    void testSaveGameHistory_interruptedException() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference users = mock(CollectionReference.class);
        DocumentReference userDoc = mock(DocumentReference.class);
        CollectionReference history = mock(CollectionReference.class);
        DocumentReference gameDoc = mock(DocumentReference.class);
        ApiFuture<WriteResult> future = mock(ApiFuture.class);

        when(firestore.collection("users")).thenReturn(users);
        when(users.document("123")).thenReturn(userDoc);
        when(userDoc.collection("gameHistory")).thenReturn(history);
        when(history.document(anyString())).thenReturn(gameDoc);
        when(gameDoc.set(any(GameHistory.class))).thenReturn(future);
        when(future.get()).thenThrow(new InterruptedException("interrupted"));

        try (MockedStatic<FirestoreClient> client = Mockito.mockStatic(FirestoreClient.class)) {
            client.when(FirestoreClient::getFirestore).thenReturn(firestore);

            GameHistoryService service = new GameHistoryService();
            boolean result = service.saveGameHistory("123", 30, 1000, 1, 2, false);

            assertFalse(result);
        }
    }

    @Test
    void testSaveGameHistoryForAllPlayers_emptyList() {
        GameHistoryService service = new GameHistoryService();

        // No exception should be thrown
        service.saveGameHistoryForAllPlayers(null, 30, "winnerId", 1);
        service.saveGameHistoryForAllPlayers(List.of(), 30, "winnerId", 1);
    }

    @Test
    void testSaveGameHistoryForAllPlayers_multiplePlayers() throws Exception {
        Player p1 = new Player("p1", "Alice");
        Player p2 = new Player("p2", "Bob");
        p1.addMoney(100);
        p2.addMoney(200);

        Firestore firestore = mock(Firestore.class);
        CollectionReference users = mock(CollectionReference.class);
        DocumentReference userDoc = mock(DocumentReference.class);
        CollectionReference history = mock(CollectionReference.class);
        DocumentReference gameDoc = mock(DocumentReference.class);
        ApiFuture<WriteResult> future = mock(ApiFuture.class);

        when(firestore.collection(anyString())).thenReturn(users);
        when(users.document(anyString())).thenReturn(userDoc);
        when(userDoc.collection(anyString())).thenReturn(history);
        when(history.document(anyString())).thenReturn(gameDoc);
        when(gameDoc.set(any(GameHistory.class))).thenReturn(future);
        when(future.get()).thenReturn(mock(WriteResult.class));

        try (MockedStatic<FirestoreClient> client = Mockito.mockStatic(FirestoreClient.class)) {
            client.when(FirestoreClient::getFirestore).thenReturn(firestore);

            GameHistoryService service = new GameHistoryService();
            service.saveGameHistoryForAllPlayers(List.of(p1, p2), 40, "p2", 2);

            verify(gameDoc, atLeastOnce()).set(any(GameHistory.class));
        }
    }
}
