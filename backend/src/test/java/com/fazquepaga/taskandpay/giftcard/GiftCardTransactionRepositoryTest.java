package com.fazquepaga.taskandpay.giftcard;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class GiftCardTransactionRepositoryTest {

    @Mock private Firestore firestore;
    @Mock private CollectionReference collectionReference;
    @Mock private DocumentReference documentReference;
    @Mock private DocumentSnapshot documentSnapshot;
    @Mock private Query query;
    @Mock private QuerySnapshot querySnapshot;

    private GiftCardTransactionRepository repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(firestore.collection("giftcard_transactions")).thenReturn(collectionReference);
        repository = new GiftCardTransactionRepository(firestore);
    }

    @Test
    void shouldSaveNewTransactionWithGeneratedId() {
        // Given
        GiftCardTransaction transaction =
                GiftCardTransaction.builder()
                        .childId("child-123")
                        .parentId("parent-456")
                        .productId("prod-789")
                        .amount(BigDecimal.valueOf(50.0))
                        .status(GiftCardTransaction.Status.PENDING)
                        .createdAt(Instant.now())
                        .idempotencyKey("key-abc")
                        .build();

        when(collectionReference.document(any())).thenReturn(documentReference);
        when(documentReference.set(any(GiftCardTransaction.class)))
                .thenReturn(ApiFutures.immediateFuture(mock(WriteResult.class)));

        // When
        repository.save(transaction);

        // Then
        assertNotNull(transaction.getId());
        assertFalse(transaction.getId().isEmpty());
        verify(collectionReference).document(transaction.getId());
        verify(documentReference).set(transaction);
    }

    @Test
    void shouldUpdateExistingTransaction() {
        // Given
        GiftCardTransaction transaction =
                GiftCardTransaction.builder()
                        .id("existing-id")
                        .childId("child-123")
                        .parentId("parent-456")
                        .productId("prod-789")
                        .amount(BigDecimal.valueOf(50.0))
                        .status(GiftCardTransaction.Status.APPROVED)
                        .createdAt(Instant.now())
                        .build();

        when(collectionReference.document("existing-id")).thenReturn(documentReference);
        when(documentReference.set(any(GiftCardTransaction.class)))
                .thenReturn(ApiFutures.immediateFuture(mock(WriteResult.class)));

        // When
        repository.save(transaction);

        // Then
        assertEquals("existing-id", transaction.getId());
        verify(collectionReference).document("existing-id");
        verify(documentReference).set(transaction);
    }

    @Test
    void shouldFindByIdWhenTransactionExists() throws ExecutionException, InterruptedException {
        // Given
        String txId = "tx-123";
        GiftCardTransaction expected =
                GiftCardTransaction.builder()
                        .id(txId)
                        .childId("child-1")
                        .amount(BigDecimal.TEN)
                        .status(GiftCardTransaction.Status.COMPLETED)
                        .build();

        when(collectionReference.document(txId)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(ApiFutures.immediateFuture(documentSnapshot));
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.toObject(GiftCardTransaction.class)).thenReturn(expected);

        // When
        GiftCardTransaction result = repository.findById(txId);

        // Then
        assertNotNull(result);
        assertEquals(txId, result.getId());
        assertEquals(BigDecimal.TEN, result.getAmount());
        assertEquals(GiftCardTransaction.Status.COMPLETED, result.getStatus());
    }

    @Test
    void shouldReturnNullWhenTransactionNotFound() throws ExecutionException, InterruptedException {
        // Given
        String txId = "tx-nonexistent";
        when(collectionReference.document(txId)).thenReturn(documentReference);
        when(documentReference.get()).thenReturn(ApiFutures.immediateFuture(documentSnapshot));
        when(documentSnapshot.exists()).thenReturn(false);

        // When
        GiftCardTransaction result = repository.findById(txId);

        // Then
        nullResultCheck(result);
    }

    private void nullResultCheck(GiftCardTransaction result) {
        assertNull(result);
    }

    @Test
    void shouldFindByChildId() throws ExecutionException, InterruptedException {
        // Given
        String childId = "child-123";
        GiftCardTransaction expectedTx =
                GiftCardTransaction.builder()
                        .id("tx-1")
                        .childId(childId)
                        .amount(BigDecimal.TEN)
                        .build();

        when(collectionReference.whereEqualTo("childId", childId)).thenReturn(query);
        when(query.orderBy("createdAt", Query.Direction.DESCENDING)).thenReturn(query);
        when(query.get()).thenReturn(ApiFutures.immediateFuture(querySnapshot));
        when(querySnapshot.toObjects(GiftCardTransaction.class)).thenReturn(List.of(expectedTx));

        // When
        List<GiftCardTransaction> results = repository.findByChildId(childId);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(childId, results.get(0).getChildId());
    }

    @Test
    void shouldFindByParentId() throws ExecutionException, InterruptedException {
        // Given
        String parentId = "parent-456";
        GiftCardTransaction expectedTx =
                GiftCardTransaction.builder()
                        .id("tx-1")
                        .parentId(parentId)
                        .amount(BigDecimal.TEN)
                        .build();

        when(collectionReference.whereEqualTo("parentId", parentId)).thenReturn(query);
        when(query.orderBy("createdAt", Query.Direction.DESCENDING)).thenReturn(query);
        when(query.get()).thenReturn(ApiFutures.immediateFuture(querySnapshot));
        when(querySnapshot.toObjects(GiftCardTransaction.class)).thenReturn(List.of(expectedTx));

        // When
        List<GiftCardTransaction> results = repository.findByParentId(parentId);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(parentId, results.get(0).getParentId());
    }

    @Test
    void shouldFindByIdempotencyKey() throws ExecutionException, InterruptedException {
        // Given
        String key = "idempotency-key-abc";
        GiftCardTransaction expectedTx =
                GiftCardTransaction.builder()
                        .id("tx-1")
                        .idempotencyKey(key)
                        .amount(BigDecimal.TEN)
                        .build();

        when(collectionReference.whereEqualTo("idempotencyKey", key)).thenReturn(query);
        when(query.limit(1)).thenReturn(query);
        when(query.get()).thenReturn(ApiFutures.immediateFuture(querySnapshot));
        when(querySnapshot.toObjects(GiftCardTransaction.class)).thenReturn(List.of(expectedTx));

        // When
        Optional<GiftCardTransaction> result = repository.findByIdempotencyKey(key);

        // Then
        assertTrue(result.isPresent());
        assertEquals(key, result.get().getIdempotencyKey());
    }

    @Test
    void shouldReturnEmptyOptionalWhenIdempotencyKeyNotFound()
            throws ExecutionException, InterruptedException {
        // Given
        String key = "idempotency-key-nonexistent";
        when(collectionReference.whereEqualTo("idempotencyKey", key)).thenReturn(query);
        when(query.limit(1)).thenReturn(query);
        when(query.get()).thenReturn(ApiFutures.immediateFuture(querySnapshot));
        when(querySnapshot.toObjects(GiftCardTransaction.class))
                .thenReturn(Collections.emptyList());

        // When
        Optional<GiftCardTransaction> result = repository.findByIdempotencyKey(key);

        // Then
        assertFalse(result.isPresent());
    }
}
