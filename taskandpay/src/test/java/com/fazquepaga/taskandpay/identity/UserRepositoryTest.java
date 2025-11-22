package com.fazquepaga.taskandpay.identity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.*;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class UserRepositoryTest {

    @Mock
    private Firestore firestore;

    @Mock
    private CollectionReference collectionReference;

    @Mock
    private DocumentReference documentReference;

    @Mock
    private DocumentSnapshot documentSnapshot;

    @Mock
    private Query query;

    @Mock
    private QuerySnapshot querySnapshot;

    @Mock
    private QueryDocumentSnapshot queryDocumentSnapshot;

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(firestore.collection("users")).thenReturn(collectionReference);
        userRepository = new UserRepository(firestore);
    }

    @Test
    void shouldSaveNewUserWithGeneratedId() {
        // Given
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .role(User.Role.PARENT)
                .build();

        when(collectionReference.document()).thenReturn(documentReference);
        when(documentReference.getId()).thenReturn("generated-id");
        when(documentReference.set(any(User.class)))
                .thenReturn(ApiFutures.immediateFuture(mock(WriteResult.class)));

        // When
        userRepository.save(user);

        // Then
        assertEquals("generated-id", user.getId());
        verify(documentReference).set(user);
    }

    @Test
    void shouldUpdateExistingUser() {
        // Given
        User user = User.builder()
                .id("existing-id")
                .name("Test User")
                .email("test@example.com")
                .role(User.Role.PARENT)
                .build();

        when(collectionReference.document("existing-id")).thenReturn(documentReference);
        when(documentReference.set(any(User.class)))
                .thenReturn(ApiFutures.immediateFuture(mock(WriteResult.class)));

        // When
        userRepository.save(user);

        // Then
        verify(collectionReference).document("existing-id");
        verify(documentReference).set(user);
    }

    @Test
    void shouldFindByIdWhenUserExists() throws ExecutionException, InterruptedException {
        // Given
        String userId = "user-123";
        User expectedUser = User.builder()
                .id(userId)
                .name("Test User")
                .role(User.Role.PARENT)
                .build();

        when(collectionReference.document(userId)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(ApiFutures.immediateFuture(documentSnapshot));
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.toObject(User.class)).thenReturn(expectedUser);

        // When
        User result = userRepository.findByIdSync(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Test User", result.getName());
    }

    @Test
    void shouldReturnNullWhenUserNotFound() throws ExecutionException, InterruptedException {
        // Given
        String userId = "non-existent-id";

        when(collectionReference.document(userId)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(ApiFutures.immediateFuture(documentSnapshot));
        when(documentSnapshot.exists()).thenReturn(false);

        // When
        User result = userRepository.findByIdSync(userId);

        // Then
        assertNull(result);
    }

    @Test
    void shouldFindByPhoneNumberWhenUserExists() throws ExecutionException, InterruptedException {
        // Given
        String phoneNumber = "+1234567890";
        User expectedUser = User.builder()
                .id("user-123")
                .phoneNumber(phoneNumber)
                .role(User.Role.CHILD)
                .build();

        when(collectionReference.whereEqualTo("phoneNumber", phoneNumber)).thenReturn(query);
        when(query.limit(1)).thenReturn(query);
        when(query.get()).thenReturn(ApiFutures.immediateFuture(querySnapshot));
        when(querySnapshot.getDocuments()).thenReturn(List.of(queryDocumentSnapshot));
        when(queryDocumentSnapshot.toObject(User.class)).thenReturn(expectedUser);

        // When
        User result = userRepository.findByPhoneNumber(phoneNumber);

        // Then
        assertNotNull(result);
        assertEquals(phoneNumber, result.getPhoneNumber());
    }

    @Test
    void shouldReturnNullWhenPhoneNumberNotFound()
            throws ExecutionException, InterruptedException {
        // Given
        String phoneNumber = "+9999999999";

        when(collectionReference.whereEqualTo("phoneNumber", phoneNumber)).thenReturn(query);
        when(query.limit(1)).thenReturn(query);
        when(query.get()).thenReturn(ApiFutures.immediateFuture(querySnapshot));
        when(querySnapshot.getDocuments()).thenReturn(Collections.emptyList());

        // When
        User result = userRepository.findByPhoneNumber(phoneNumber);

        // Then
        assertNull(result);
    }

    @Test
    void shouldHandleEmptyUserId() {
        // Given
        User user = User.builder()
                .id("")
                .name("Test User")
                .role(User.Role.PARENT)
                .build();

        when(collectionReference.document()).thenReturn(documentReference);
        when(documentReference.getId()).thenReturn("generated-id");
        when(documentReference.set(any(User.class)))
                .thenReturn(ApiFutures.immediateFuture(mock(WriteResult.class)));

        // When
        userRepository.save(user);

        // Then
        assertEquals("generated-id", user.getId());
        verify(documentReference).set(user);
    }
}
