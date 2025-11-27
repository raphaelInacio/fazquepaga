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

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AiInsightService aiInsightService;

    private LedgerService ledgerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ledgerService = new LedgerService(transactionRepository, userRepository, aiInsightService);
    }

    @Test
    void addTransaction_Credit_ShouldUpdateBalance() throws ExecutionException, InterruptedException {
        // Given
        String childId = "child-1";
        BigDecimal amount = BigDecimal.valueOf(10.0);
        String description = "Task Reward";
        Transaction.TransactionType type = Transaction.TransactionType.CREDIT;

        User child = User.builder()
                .id(childId)
                .balance(BigDecimal.ZERO)
                .build();

        when(userRepository.findByIdSync(childId)).thenReturn(child);
        when(userRepository.save(any(User.class))).thenReturn(ApiFutures.immediateFuture(null));

        // When
        ledgerService.addTransaction(childId, amount, description, type);

        // Then
        verify(transactionRepository).save(any(Transaction.class));
        verify(userRepository).save(argThat(user -> user.getBalance().compareTo(BigDecimal.valueOf(10.0)) == 0));
    }

    @Test
    void getTransactions_ShouldReturnLedgerResponse() throws ExecutionException, InterruptedException {
        // Given
        String childId = "child-1";
        QuerySnapshot querySnapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot documentSnapshot = mock(QueryDocumentSnapshot.class);
        Transaction transaction = new Transaction();

        when(transactionRepository.findByChildId(childId)).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(Collections.singletonList(documentSnapshot));
        when(documentSnapshot.toObject(Transaction.class)).thenReturn(transaction);

        User child = User.builder().id(childId).parentId("parent-id").build();
        when(userRepository.findByIdSync(childId)).thenReturn(child);

        // When
        LedgerResponse result = ledgerService.getTransactions(childId, "parent-id");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTransactions().size());
    }
}
