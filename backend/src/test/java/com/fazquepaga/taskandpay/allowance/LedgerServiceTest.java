package com.fazquepaga.taskandpay.allowance;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fazquepaga.taskandpay.ai.AiInsightService;
import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class LedgerServiceTest {

    @Mock private TransactionRepository transactionRepository;

    @Mock private UserRepository userRepository;

    @Mock private AiInsightService aiInsightService;

    @Mock private com.google.cloud.firestore.Firestore firestore;

    private LedgerService ledgerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ledgerService =
                new LedgerService(
                        transactionRepository, userRepository, aiInsightService, firestore);
    }

    @Test
    void addTransaction_Credit_ShouldUpdateBalance()
            throws ExecutionException, InterruptedException {
        // Given
        String childId = "child-1";
        BigDecimal amount = BigDecimal.valueOf(10.0);
        String description = "Task Reward";
        Transaction.TransactionType type = Transaction.TransactionType.CREDIT;

        User child = User.builder().id(childId).balance(BigDecimal.ZERO).build();

        com.google.cloud.firestore.DocumentReference userRef = mock(com.google.cloud.firestore.DocumentReference.class);
        com.google.cloud.firestore.DocumentSnapshot userSnap = mock(com.google.cloud.firestore.DocumentSnapshot.class);
        com.google.cloud.firestore.Transaction firestoreTx = mock(com.google.cloud.firestore.Transaction.class);
        com.google.cloud.firestore.CollectionReference usersCol = mock(com.google.cloud.firestore.CollectionReference.class);
        com.google.cloud.firestore.CollectionReference txCol = mock(com.google.cloud.firestore.CollectionReference.class);
        com.google.cloud.firestore.DocumentReference txRef = mock(com.google.cloud.firestore.DocumentReference.class);

        when(firestore.collection("users")).thenReturn(usersCol);
        when(usersCol.document(childId)).thenReturn(userRef);
        when(firestore.collection("transactions")).thenReturn(txCol);
        when(txCol.document(anyString())).thenReturn(txRef);
        
        when(firestoreTx.get(userRef)).thenReturn(ApiFutures.immediateFuture(userSnap));
        when(userSnap.exists()).thenReturn(true);
        when(userSnap.toObject(User.class)).thenReturn(child);

        when(firestore.runTransaction(any())).thenAnswer(invocation -> {
            com.google.cloud.firestore.Transaction.Function function = invocation.getArgument(0);
            return ApiFutures.immediateFuture(function.update(firestoreTx));
        });

        // When
        ledgerService.addTransaction(childId, amount, description, type);

        // Then
        verify(firestoreTx).update(eq(userRef), eq("balance"), argThat(val -> ((BigDecimal)val).compareTo(BigDecimal.valueOf(10.0)) == 0));
        verify(firestoreTx).set(eq(txRef), any(Transaction.class));
    }

    @Test
    void addTransaction_Withdrawal_InsufficientBalance_ShouldThrowException()
            throws ExecutionException, InterruptedException {
        // Given
        String childId = "child-1";
        BigDecimal amount = BigDecimal.valueOf(100.0);
        User child = User.builder().id(childId).balance(BigDecimal.valueOf(10.0)).build();

        com.google.cloud.firestore.DocumentReference userRef = mock(com.google.cloud.firestore.DocumentReference.class);
        com.google.cloud.firestore.DocumentSnapshot userSnap = mock(com.google.cloud.firestore.DocumentSnapshot.class);
        com.google.cloud.firestore.Transaction firestoreTx = mock(com.google.cloud.firestore.Transaction.class);
        com.google.cloud.firestore.CollectionReference usersCol = mock(com.google.cloud.firestore.CollectionReference.class);

        when(firestore.collection("users")).thenReturn(usersCol);
        when(usersCol.document(childId)).thenReturn(userRef);
        when(firestoreTx.get(userRef)).thenReturn(ApiFutures.immediateFuture(userSnap));
        when(userSnap.exists()).thenReturn(true);
        when(userSnap.toObject(User.class)).thenReturn(child);

        when(firestore.runTransaction(any())).thenAnswer(invocation -> {
            com.google.cloud.firestore.Transaction.Function function = invocation.getArgument(0);
            try {
                return ApiFutures.immediateFuture(function.update(firestoreTx));
            } catch (Exception e) {
                return ApiFutures.immediateFailedFuture(e);
            }
        });

        // When & Then
        assertThrows(
                ExecutionException.class,
                () -> ledgerService.addTransaction(childId, amount, "Test", Transaction.TransactionType.WITHDRAWAL));
    }
}
