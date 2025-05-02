package at.aau.serg.monopoly.firebase;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LeaderboardServiceTest {

    private LeaderboardService leaderboardService;
    private Firestore firestore;

    @BeforeEach
    void setUp() {
        firestore = mock(Firestore.class);
        leaderboardService = new LeaderboardService();

        try (MockedStatic<FirestoreClient> firestoreClient = Mockito.mockStatic(FirestoreClient.class)) {
            firestoreClient.when(FirestoreClient::getFirestore).thenReturn(firestore);
        }
    }

    @Test
    void testUpdateUserStats() throws Exception {
        // Mock Firestore-Kette
        CollectionReference usersRef = mock(CollectionReference.class);
        DocumentReference userDocRef = mock(DocumentReference.class);
        CollectionReference gameHistoryRef = mock(CollectionReference.class);
        ApiFuture<QuerySnapshot> historyFuture = mock(ApiFuture.class);
        QuerySnapshot historySnapshot = mock(QuerySnapshot.class);
        ApiFuture<DocumentSnapshot> userFuture = mock(ApiFuture.class);
        DocumentSnapshot userSnapshot = mock(DocumentSnapshot.class);

        when(firestore.collection("users")).thenReturn(usersRef);
        when(usersRef.document("123")).thenReturn(userDocRef);
        when(userDocRef.collection("gameHistory")).thenReturn(gameHistoryRef);
        when(gameHistoryRef.get()).thenReturn(historyFuture);
        when(historyFuture.get()).thenReturn(historySnapshot);
        when(userDocRef.get()).thenReturn(userFuture);
        when(userFuture.get()).thenReturn(userSnapshot);
        when(userSnapshot.getString("name")).thenReturn("TestUser");

        // Mock Spieldaten
        List<QueryDocumentSnapshot> games = List.of(mock(QueryDocumentSnapshot.class));
        when(historySnapshot.getDocuments()).thenReturn(games);
        when(games.get(0).getData()).thenReturn(Map.of("won", true, "endMoney", 2000));

        // Testausführung
        leaderboardService.updateUserStats("123", firestore);

        // Verifizierung
        verify(userDocRef).set(
                argThat(map ->
                        map.get("wins").equals(1) &&
                                map.get("highestMoney").equals(2000)
                ),
                any(SetOptions.class)
        );
    }

    @Test
    void testUpdateLeaderboard() throws Exception {
        try (MockedStatic<FirestoreClient> firestoreClient = Mockito.mockStatic(FirestoreClient.class)) {
            firestoreClient.when(FirestoreClient::getFirestore).thenReturn(firestore);
        CollectionReference usersRef = mock(CollectionReference.class);
        Query userQuery = mock(Query.class);
        ApiFuture<QuerySnapshot> queryFuture = mock(ApiFuture.class);
        QuerySnapshot querySnapshot = mock(QuerySnapshot.class);
        CollectionReference leaderboardRef = mock(CollectionReference.class);
        DocumentReference docRef = mock(DocumentReference.class);
        Query deleteQuery = mock(Query.class);
        ApiFuture<QuerySnapshot> deleteFuture = mock(ApiFuture.class);
        QuerySnapshot deleteSnapshot = mock(QuerySnapshot.class);

        when(firestore.collection("users")).thenReturn(usersRef);
        when(usersRef.orderBy(eq("wins"), eq(Query.Direction.DESCENDING))).thenReturn(userQuery);
        when(userQuery.limit(50)).thenReturn(userQuery);
        when(userQuery.get()).thenReturn(queryFuture);
        when(queryFuture.get()).thenReturn(querySnapshot);

        // Mock leaderboard collection and deletion
        when(firestore.collection("leaderboard_wins")).thenReturn(leaderboardRef);
        when(leaderboardRef.limit(100)).thenReturn(deleteQuery);
        when(deleteQuery.get()).thenReturn(deleteFuture);
        when(deleteFuture.get()).thenReturn(deleteSnapshot);
        when(deleteSnapshot.getDocuments()).thenReturn(List.of()); // No documents to delete

        when(leaderboardRef.document(anyString())).thenReturn(docRef);

        // Mock user data
        QueryDocumentSnapshot userDoc = mock(QueryDocumentSnapshot.class);
        when(querySnapshot.getDocuments()).thenReturn(List.of(userDoc));
        when(userDoc.getId()).thenReturn("user123");
        when(userDoc.getData()).thenReturn(Map.of("wins", 5, "name", "TestUser"));

        // Execute test
        leaderboardService.updateLeaderboard("wins", "leaderboard_wins");

        // Verify document is set
            verify(docRef).set(argThat(entry ->
                    entry.get("name").equals("TestUser") &&
                            entry.get("wins").equals(5) &&
                            entry.get("rank").equals(1)
            ));
        }
    }

    @Test
    void testDeleteCollection() throws Exception {
        CollectionReference collectionRef = mock(CollectionReference.class);
        ApiFuture<QuerySnapshot> future = mock(ApiFuture.class);
        QuerySnapshot snapshot = mock(QuerySnapshot.class);

        when(firestore.collection("test_collection")).thenReturn(collectionRef);
        when(collectionRef.limit(100)).thenReturn(collectionRef);
        when(collectionRef.get()).thenReturn(future);
        when(future.get()).thenReturn(snapshot);
        when(snapshot.getDocuments()).thenReturn(List.of());

        leaderboardService.deleteCollection(firestore, "test_collection");

        verify(collectionRef).get();
    }
}