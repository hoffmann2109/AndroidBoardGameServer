package at.aau.serg.monopoly.websoket;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import data.GameHistoryRequest;
import model.GameHistory;
import model.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameHistoryServiceTest {

    @Test
    void testSaveGameHistory_successful() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference users = mock(CollectionReference.class);
        DocumentReference userDoc = mock(DocumentReference.class);
        CollectionReference history = mock(CollectionReference.class);
        DocumentReference gameDoc = mock(DocumentReference.class);
        ApiFuture<WriteResult> future = mock(ApiFuture.class);
        ApiFuture<DocumentSnapshot> getFuture = mock(ApiFuture.class);
        DocumentSnapshot documentSnapshot = mock(DocumentSnapshot.class);

        when(firestore.collection("users")).thenReturn(users);
        when(users.document("123")).thenReturn(userDoc);

        when(userDoc.get()).thenReturn(getFuture);
        when(getFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);

        when(userDoc.collection("gameHistory")).thenReturn(history);
        when(history.document(anyString())).thenReturn(gameDoc);
        when(gameDoc.set(any(GameHistory.class))).thenReturn(future);
        when(future.get()).thenReturn(mock(WriteResult.class));

        try (MockedStatic<FirestoreClient> client = Mockito.mockStatic(FirestoreClient.class)) {
            client.when(FirestoreClient::getFirestore).thenReturn(firestore);

            GameHistoryService service = new GameHistoryService();
            boolean result = service.saveGameHistory(new GameHistoryRequest("123", 45, 1500, false));

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
        ApiFuture<DocumentSnapshot> getFuture = mock(ApiFuture.class);
        DocumentSnapshot documentSnapshot = mock(DocumentSnapshot.class);

        when(firestore.collection("users")).thenReturn(users);
        when(users.document("123")).thenReturn(userDoc);
        when(userDoc.get()).thenReturn(getFuture);
        when(getFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);

        when(userDoc.collection("gameHistory")).thenReturn(history);
        when(history.document(anyString())).thenReturn(gameDoc);
        when(gameDoc.set(any(GameHistory.class))).thenReturn(future);
        when(future.get()).thenThrow(new ExecutionException("failed", new Throwable()));

        try (MockedStatic<FirestoreClient> client = Mockito.mockStatic(FirestoreClient.class)) {
            client.when(FirestoreClient::getFirestore).thenReturn(firestore);

            GameHistoryService service = new GameHistoryService();
            boolean result = service.saveGameHistory(new GameHistoryRequest("123", 30, 1000, true));
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
        ApiFuture<DocumentSnapshot> getFuture = mock(ApiFuture.class);
        DocumentSnapshot documentSnapshot = mock(DocumentSnapshot.class);

        when(firestore.collection("users")).thenReturn(users);
        when(users.document("123")).thenReturn(userDoc);
        when(userDoc.get()).thenReturn(getFuture);
        when(getFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(userDoc.collection("gameHistory")).thenReturn(history);
        when(history.document(anyString())).thenReturn(gameDoc);
        when(gameDoc.set(any(GameHistory.class))).thenReturn(future);
        when(future.get()).thenThrow(new InterruptedException("interrupted"));

        try (MockedStatic<FirestoreClient> client = Mockito.mockStatic(FirestoreClient.class)) {
            client.when(FirestoreClient::getFirestore).thenReturn(firestore);

            GameHistoryService service = new GameHistoryService();
            boolean result = service.saveGameHistory(new GameHistoryRequest("123", 30, 1000, false));

            assertFalse(result);
        }
    }

    @Test
    void testSaveGameHistoryForAllPlayers_emptyList() {
        GameHistoryService service = new GameHistoryService();
        assertDoesNotThrow(() -> service.saveGameHistoryForAllPlayers(null, 30, "winnerId"));
        assertDoesNotThrow(() -> service.saveGameHistoryForAllPlayers(List.of(), 30, "winnerId"));
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
        ApiFuture<DocumentSnapshot> getFuture = mock(ApiFuture.class);
        DocumentSnapshot documentSnapshot = mock(DocumentSnapshot.class);

        when(firestore.collection(anyString())).thenReturn(users);
        when(users.document(anyString())).thenReturn(userDoc);
        when(userDoc.get()).thenReturn(getFuture);
        when(getFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);
        when(userDoc.collection(anyString())).thenReturn(history);
        when(history.document(anyString())).thenReturn(gameDoc);
        when(gameDoc.set(any(GameHistory.class))).thenReturn(future);
        when(future.get()).thenReturn(mock(WriteResult.class));

        try (MockedStatic<FirestoreClient> client = Mockito.mockStatic(FirestoreClient.class)) {
            client.when(FirestoreClient::getFirestore).thenReturn(firestore);

            GameHistoryService service = new GameHistoryService();
            service.saveGameHistoryForAllPlayers(List.of(p1, p2), 40, "p2");

            verify(gameDoc, atLeastOnce()).set(any(GameHistory.class));
        }
    }

    @Test
    void testSaveGameHistoryWhenUserDocumentNotExists() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference users = mock(CollectionReference.class);
        DocumentReference userDoc = mock(DocumentReference.class);
        CollectionReference history = mock(CollectionReference.class);
        DocumentReference gameDoc = mock(DocumentReference.class);
        ApiFuture<WriteResult> future = mock(ApiFuture.class);
        ApiFuture<DocumentSnapshot> getFuture = mock(ApiFuture.class);
        DocumentSnapshot documentSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<WriteResult> setFuture = mock(ApiFuture.class);

        when(firestore.collection("users")).thenReturn(users);
        when(users.document("123")).thenReturn(userDoc);

        when(userDoc.get()).thenReturn(getFuture);
        when(getFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(false); // Dokument existiert nicht

        when(userDoc.set(Collections.emptyMap())).thenReturn(setFuture);
        when(setFuture.get()).thenReturn(mock(WriteResult.class));

        when(userDoc.collection("gameHistory")).thenReturn(history);
        when(history.document(anyString())).thenReturn(gameDoc);
        when(gameDoc.set(any(GameHistory.class))).thenReturn(future);
        when(future.get()).thenReturn(mock(WriteResult.class));

        try (MockedStatic<FirestoreClient> client = Mockito.mockStatic(FirestoreClient.class)) {
            client.when(FirestoreClient::getFirestore).thenReturn(firestore);

            GameHistoryService service = new GameHistoryService();
            boolean result = service.saveGameHistory(new GameHistoryRequest("123", 45, 1500, false));

            assertTrue(result);
            verify(userDoc).set(Collections.emptyMap());
            verify(gameDoc).set(any(GameHistory.class));
        }
    }

    @Test
    void testEnsureGameHistorySubcollectionThrowsException() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference users = mock(CollectionReference.class);
        DocumentReference userDoc = mock(DocumentReference.class);
        ApiFuture<DocumentSnapshot> getFuture = mock(ApiFuture.class);

        when(firestore.collection("users")).thenReturn(users);
        when(users.document("123")).thenReturn(userDoc);
        when(userDoc.get()).thenReturn(getFuture);
        when(getFuture.get()).thenThrow(new ExecutionException("test", new Exception()));

        // Mock für die spätere Speicherung der Historie hinzufügen
        CollectionReference history = mock(CollectionReference.class);
        DocumentReference gameDoc = mock(DocumentReference.class);
        ApiFuture<WriteResult> future = mock(ApiFuture.class);

        when(userDoc.collection("gameHistory")).thenReturn(history);
        when(history.document(anyString())).thenReturn(gameDoc);
        when(gameDoc.set(any(GameHistory.class))).thenReturn(future);
        when(future.get()).thenThrow(new ExecutionException("failed", new Throwable()));

        try (MockedStatic<FirestoreClient> client = Mockito.mockStatic(FirestoreClient.class)) {
            client.when(FirestoreClient::getFirestore).thenReturn(firestore);

            GameHistoryService service = new GameHistoryService();
            boolean result = service.saveGameHistory(new GameHistoryRequest("123", 30, 1000, true));

            assertFalse(result);
        }
    }

    @Test
    void testMarkPlayerAsLoser_successful() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference users = mock(CollectionReference.class);
        DocumentReference userDoc = mock(DocumentReference.class);
        CollectionReference history = mock(CollectionReference.class);
        DocumentReference gameDoc = mock(DocumentReference.class);
        ApiFuture<WriteResult> future = mock(ApiFuture.class);
        ApiFuture<DocumentSnapshot> getFuture = mock(ApiFuture.class);
        DocumentSnapshot documentSnapshot = mock(DocumentSnapshot.class);

        when(firestore.collection("users")).thenReturn(users);
        when(users.document("123")).thenReturn(userDoc);
        when(userDoc.get()).thenReturn(getFuture);
        when(getFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);

        when(userDoc.collection("gameHistory")).thenReturn(history);
        when(history.document(anyString())).thenReturn(gameDoc);
        when(gameDoc.set(any(GameHistory.class))).thenReturn(future);
        when(future.get()).thenReturn(mock(WriteResult.class));

        try (MockedStatic<FirestoreClient> client = Mockito.mockStatic(FirestoreClient.class)) {
            client.when(FirestoreClient::getFirestore).thenReturn(firestore);

            GameHistoryService service = new GameHistoryService();
            service.markPlayerAsLoser("123",0,0);

            ArgumentCaptor<GameHistory> captor = ArgumentCaptor.forClass(GameHistory.class);
            verify(gameDoc).set(captor.capture());
            GameHistory savedHistory = captor.getValue();
            assertEquals("123", savedHistory.getUserId());
            assertEquals(0, savedHistory.getDurationMinutes());
            assertEquals(0, savedHistory.getEndMoney());
            assertEquals(0, savedHistory.getLevelGained());
            assertFalse(savedHistory.isWon());
            assertNotNull(savedHistory.getTimestamp());
        }
    }

    @Test
    void testMarkPlayerAsLoser_interruptedException() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference users = mock(CollectionReference.class);
        DocumentReference userDoc = mock(DocumentReference.class);
        CollectionReference history = mock(CollectionReference.class);
        DocumentReference gameDoc = mock(DocumentReference.class);
        ApiFuture<WriteResult> future = mock(ApiFuture.class);
        ApiFuture<DocumentSnapshot> getFuture = mock(ApiFuture.class);
        DocumentSnapshot documentSnapshot = mock(DocumentSnapshot.class);

        when(firestore.collection("users")).thenReturn(users);
        when(users.document("123")).thenReturn(userDoc);
        when(userDoc.get()).thenReturn(getFuture);
        when(getFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);

        when(userDoc.collection("gameHistory")).thenReturn(history);
        when(history.document(anyString())).thenReturn(gameDoc);
        when(gameDoc.set(any(GameHistory.class))).thenReturn(future);
        when(future.get()).thenThrow(new InterruptedException());

        try (MockedStatic<FirestoreClient> client = Mockito.mockStatic(FirestoreClient.class)) {
            client.when(FirestoreClient::getFirestore).thenReturn(firestore);

            GameHistoryService service = new GameHistoryService();
            assertDoesNotThrow(() -> service.markPlayerAsLoser("123",0,0));
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void testMarkPlayerAsLoser_executionException() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference users = mock(CollectionReference.class);
        DocumentReference userDoc = mock(DocumentReference.class);
        CollectionReference history = mock(CollectionReference.class);
        DocumentReference gameDoc = mock(DocumentReference.class);
        ApiFuture<WriteResult> future = mock(ApiFuture.class);
        ApiFuture<DocumentSnapshot> getFuture = mock(ApiFuture.class);
        DocumentSnapshot documentSnapshot = mock(DocumentSnapshot.class);

        when(firestore.collection("users")).thenReturn(users);
        when(users.document("123")).thenReturn(userDoc);
        when(userDoc.get()).thenReturn(getFuture);
        when(getFuture.get()).thenReturn(documentSnapshot);
        when(documentSnapshot.exists()).thenReturn(true);

        when(userDoc.collection("gameHistory")).thenReturn(history);
        when(history.document(anyString())).thenReturn(gameDoc);
        when(gameDoc.set(any(GameHistory.class))).thenReturn(future);
        when(future.get()).thenThrow(new ExecutionException("test", new Exception()));

        try (MockedStatic<FirestoreClient> client = Mockito.mockStatic(FirestoreClient.class)) {
            client.when(FirestoreClient::getFirestore).thenReturn(firestore);

            GameHistoryService service = new GameHistoryService();
            assertDoesNotThrow(() -> service.markPlayerAsLoser("123", 0, 0));
        } finally {
            Thread.interrupted();
        }
    }
}
