package at.aau.serg.monopoly.firebase;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    private LeaderboardService leaderboardService;
    private Firestore firestore;

    private static MockedStatic<FirestoreClient> firestoreClientMock;

    @BeforeAll
    static void initStaticMock() {
        firestoreClientMock = Mockito.mockStatic(FirestoreClient.class);
    }

    @AfterAll
    static void closeStaticMock() {
        firestoreClientMock.close();
    }

    @BeforeEach
    void setup() {
        firestore = mock(Firestore.class);
        leaderboardService = new LeaderboardService();
        firestoreClientMock.when(FirestoreClient::getFirestore).thenReturn(firestore);
    }

    @Test
    void testUpdateLeaderboard_success() throws Exception {
        CollectionReference users = mock(CollectionReference.class);
        Query query = mock(Query.class);
        Query limitedQuery = mock(Query.class);
        ApiFuture<QuerySnapshot> future = mock(ApiFuture.class);
        QuerySnapshot snapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot userDoc = mock(QueryDocumentSnapshot.class);

        Map<String, Object> userData = new HashMap<>();
        userData.put("wins", 3);
        userData.put("name", "Tester");

        when(firestore.collection("users")).thenReturn(users);
        when(users.orderBy(eq("wins"), any())).thenReturn(query);
        when(query.limit(50)).thenReturn(limitedQuery);
        when(limitedQuery.get()).thenReturn(future);
        when(future.get()).thenReturn(snapshot);
        when(snapshot.getDocuments()).thenReturn(List.of(userDoc));
        when(userDoc.getId()).thenReturn("123");
        when(userDoc.getData()).thenReturn(userData);

        CollectionReference lb = mock(CollectionReference.class);
        DocumentReference docRef = mock(DocumentReference.class);
        Query lbLimit = mock(Query.class);
        ApiFuture<QuerySnapshot> lbFuture = mock(ApiFuture.class);
        QuerySnapshot lbSnapshot = mock(QuerySnapshot.class);

        when(firestore.collection("leaderboard_wins")).thenReturn(lb);
        when(lb.limit(100)).thenReturn(lbLimit);
        when(lbLimit.get()).thenReturn(lbFuture);
        when(lbFuture.get()).thenReturn(lbSnapshot);
        when(lbSnapshot.getDocuments()).thenReturn(Collections.emptyList());

        when(lb.document(anyString())).thenReturn(docRef);

        leaderboardService.updateLeaderboard(firestore, "wins", "leaderboard_wins");

        verify(docRef).set(argThat((Map<String, Object> m) ->
                m.get("name").equals("Tester") &&
                        m.get("userId").equals("123") &&
                        m.get("wins").equals(3) &&
                        m.get("rank").equals(1)
        ));
    }

    @Test
    void testDeleteCollection_recursive() throws Exception {
        CollectionReference collection = mock(CollectionReference.class);
        Query limitedQuery = mock(Query.class);
        ApiFuture<QuerySnapshot> future1 = mock(ApiFuture.class);
        QuerySnapshot snap1 = mock(QuerySnapshot.class);
        QueryDocumentSnapshot doc = mock(QueryDocumentSnapshot.class);
        DocumentReference docRef = mock(DocumentReference.class);

        when(firestore.collection("some")).thenReturn(collection);
        when(collection.limit(100)).thenReturn(limitedQuery);
        when(limitedQuery.get()).thenReturn(future1);
        when(future1.get()).thenReturn(snap1);
        when(doc.getReference()).thenReturn(docRef);
        when(snap1.getDocuments()).thenReturn(Collections.nCopies(100, doc));

        leaderboardService.deleteCollection(firestore, "some");

        verify(docRef, times(100)).delete();
    }

    @Test
    void testUpdateAllLeaderboards_success() {
        leaderboardService = spy(new LeaderboardService());
        doNothing().when(leaderboardService).updateWinsLeaderboard(any());
        doNothing().when(leaderboardService).updateLevelLeaderboard(any());
        doNothing().when(leaderboardService).updateMoneyLeaderboard(any());
        doNothing().when(leaderboardService).updateHighMoneyLeaderboard(any());
        doNothing().when(leaderboardService).updateGamesPlayedLeaderboard(any());

        leaderboardService.updateAllLeaderboards();

        verify(leaderboardService).updateWinsLeaderboard(any());
        verify(leaderboardService).updateLevelLeaderboard(any());
        verify(leaderboardService).updateMoneyLeaderboard(any());
        verify(leaderboardService).updateHighMoneyLeaderboard(any());
        verify(leaderboardService).updateGamesPlayedLeaderboard(any());
    }

    @Test
    void testUpdateLeaderboard_userDataNull() throws Exception {
        CollectionReference users = mock(CollectionReference.class);
        Query query = mock(Query.class);
        Query limitedQuery = mock(Query.class);
        ApiFuture<QuerySnapshot> future = mock(ApiFuture.class);
        QuerySnapshot snapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot userDoc = mock(QueryDocumentSnapshot.class);

        when(firestore.collection("users")).thenReturn(users);
        when(users.orderBy(eq("wins"), any())).thenReturn(query);
        when(query.limit(50)).thenReturn(limitedQuery);
        when(limitedQuery.get()).thenReturn(future);
        when(future.get()).thenReturn(snapshot);
        when(snapshot.getDocuments()).thenReturn(List.of(userDoc));
        when(userDoc.getData()).thenReturn(null); // wichtig!

        CollectionReference lb = mock(CollectionReference.class);
        Query lbLimit = mock(Query.class);
        ApiFuture<QuerySnapshot> lbFuture = mock(ApiFuture.class);
        QuerySnapshot lbSnapshot = mock(QuerySnapshot.class);

        when(firestore.collection("leaderboard_wins")).thenReturn(lb);
        when(lb.limit(100)).thenReturn(lbLimit);
        when(lbLimit.get()).thenReturn(lbFuture);
        when(lbFuture.get()).thenReturn(lbSnapshot);
        when(lbSnapshot.getDocuments()).thenReturn(Collections.emptyList());

        leaderboardService.updateLeaderboard(firestore, "wins", "leaderboard_wins");
        // Erwartung: kein Set-Aufruf, da getData() == null
    }

    @Test
    void testDeleteCollection_empty() throws Exception {
        CollectionReference collection = mock(CollectionReference.class);
        Query limitedQuery = mock(Query.class);
        ApiFuture<QuerySnapshot> future = mock(ApiFuture.class);
        QuerySnapshot snapshot = mock(QuerySnapshot.class);

        when(firestore.collection("empty")).thenReturn(collection);
        when(collection.limit(100)).thenReturn(limitedQuery);
        when(limitedQuery.get()).thenReturn(future);
        when(future.get()).thenReturn(snapshot);
        when(snapshot.getDocuments()).thenReturn(Collections.emptyList());

        leaderboardService.deleteCollection(firestore, "empty");
        // Erwartung: kein Aufruf von delete()
    }

    @Test
    void testUpdateWinsLeaderboard_callsUpdateLeaderboard() {
        LeaderboardService spyService = Mockito.spy(new LeaderboardService());
        doNothing().when(spyService).updateLeaderboard(any(), eq("wins"), eq("leaderboard_wins"));

        spyService.updateWinsLeaderboard(firestore);

        verify(spyService).updateLeaderboard(firestore, "wins", "leaderboard_wins");
    }

}
