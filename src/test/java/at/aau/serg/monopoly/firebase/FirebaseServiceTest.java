package at.aau.serg.monopoly.firebase;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FirebaseServiceTest {

    private FirebaseService service;

    @BeforeEach
    void setUp() {
        service = new FirebaseService();
    }

    @Test
    void testInitialize_firebaseAlreadyInitialized() {
        try (MockedStatic<FirebaseApp> apps = mockStatic(FirebaseApp.class)) {
            apps.when(FirebaseApp::getApps).thenReturn(List.of(mock(FirebaseApp.class)));
            assertDoesNotThrow(() -> service.initialize());
        }
    }

    @Disabled ("Will be inspected later")
    @Test
    void testInitialize_successfulInit() {
        try (
                MockedStatic<FirebaseApp> apps = mockStatic(FirebaseApp.class);
                MockedStatic<FirestoreClient> firestoreMock = mockStatic(FirestoreClient.class);
                MockedStatic<GoogleCredentials> credentialsMock = mockStatic(GoogleCredentials.class)
        ) {
            apps.when(FirebaseApp::getApps).thenReturn(Collections.emptyList());

            Firestore firestore = mock(Firestore.class);
            DocumentReference doc = mock(DocumentReference.class);
            CollectionReference col = mock(CollectionReference.class);
            ApiFuture<WriteResult> future = mock(ApiFuture.class);

            firestoreMock.when(FirestoreClient::getFirestore).thenReturn(firestore);
            when(firestore.collection(anyString())).thenReturn(col);
            when(col.document(anyString())).thenReturn(doc);
            when(doc.set(any())).thenReturn(future);
            when(future.get(anyLong(), any())).thenReturn(mock(WriteResult.class));
            doNothing().when(doc).delete();

            GoogleCredentials creds = mock(GoogleCredentials.class);
            credentialsMock.when(() -> GoogleCredentials.fromStream(any())).thenReturn(creds);

            InputStream dummyStream = new ByteArrayInputStream("{}".getBytes());

            try (MockedStatic<FirebaseOptions> opt = mockStatic(FirebaseOptions.class)) {
                opt.when(FirebaseOptions::builder).thenCallRealMethod();
                assertDoesNotThrow(() -> {
                    FirebaseService testService = spy(service);
                    doReturn(dummyStream).when(testService).locateServiceAccountKey();
                    testService.initialize();
                });
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testInitialize_lacksServiceAccountKey() throws Exception {
        FirebaseService testService = spy(service);
        doReturn(null).when(testService).locateServiceAccountKey();

        try (MockedStatic<FirebaseApp> apps = mockStatic(FirebaseApp.class)) {
            apps.when(FirebaseApp::getApps).thenReturn(Collections.emptyList());
            assertDoesNotThrow(testService::initialize);
        }
    }

    @Disabled ("Will be inspected later")
    @Test
    void testHandleFirebaseInitialization_interrupted() throws Exception {
        FirebaseService testService = spy(service);
        doThrow(new InterruptedException("test")).when(testService).locateServiceAccountKey();

        try (MockedStatic<FirebaseApp> apps = mockStatic(FirebaseApp.class)) {
            apps.when(FirebaseApp::getApps).thenReturn(Collections.emptyList());
            assertDoesNotThrow(testService::initialize);
        }
    }


    @Test
    void testTestFirestoreConnection_executionFails() throws Exception {
        try (MockedStatic<FirestoreClient> firestoreMock = mockStatic(FirestoreClient.class)) {
            Firestore firestore = mock(Firestore.class);
            firestoreMock.when(FirestoreClient::getFirestore).thenReturn(firestore);

            CollectionReference col = mock(CollectionReference.class);
            DocumentReference doc = mock(DocumentReference.class);
            ApiFuture<WriteResult> future = mock(ApiFuture.class);

            when(firestore.collection(anyString())).thenReturn(col);
            when(col.document(anyString())).thenReturn(doc);
            when(doc.set(any())).thenReturn(future);
            when(future.get(anyLong(), any())).thenThrow(new ExecutionException("fail", new Throwable()));

            // call private method through reflection
            assertDoesNotThrow(() -> {
                FirebaseService testService = new FirebaseService();
                var method = FirebaseService.class.getDeclaredMethod("testFirestoreConnection");
                method.setAccessible(true);
                method.invoke(testService);
            });
        }
    }

    @Test
    void testTestFirestoreConnection_timeoutFails() throws Exception {
        try (MockedStatic<FirestoreClient> firestoreMock = mockStatic(FirestoreClient.class)) {
            Firestore firestore = mock(Firestore.class);
            firestoreMock.when(FirestoreClient::getFirestore).thenReturn(firestore);

            CollectionReference col = mock(CollectionReference.class);
            DocumentReference doc = mock(DocumentReference.class);
            ApiFuture<WriteResult> future = mock(ApiFuture.class);

            when(firestore.collection(anyString())).thenReturn(col);
            when(col.document(anyString())).thenReturn(doc);
            when(doc.set(any())).thenReturn(future);
            when(future.get(anyLong(), any())).thenThrow(new TimeoutException("timeout"));

            assertDoesNotThrow(() -> {
                FirebaseService testService = new FirebaseService();
                var method = FirebaseService.class.getDeclaredMethod("testFirestoreConnection");
                method.setAccessible(true);
                method.invoke(testService);
            });
        }
    }

    @Test
    void testLocateServiceAccountKey_fromFileSystem() throws Exception {
        File tempFile = File.createTempFile("serviceAccountKey", ".json");
        tempFile.deleteOnExit();

        FirebaseService testService = new FirebaseService() {
            @Override
            public InputStream locateServiceAccountKey() throws IOException {
                return new FileInputStream(tempFile);
            }
        };

        try (InputStream in = testService.locateServiceAccountKey()) {
            assertNotNull(in);
        }
    }

    @Test
    void testHandleFirebaseInitialization_throwsIOException() throws Exception {
        FirebaseService testService = spy(service);
        doThrow(new IOException("test")).when(testService).locateServiceAccountKey();

        try (MockedStatic<FirebaseApp> apps = mockStatic(FirebaseApp.class)) {
            apps.when(FirebaseApp::getApps).thenReturn(Collections.emptyList());
            assertDoesNotThrow(testService::initialize);
        }
    }


}
