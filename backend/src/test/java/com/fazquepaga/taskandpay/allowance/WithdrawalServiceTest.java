package com.fazquepaga.taskandpay.allowance;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.fazquepaga.taskandpay.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WithdrawalServiceTest {

    @Mock
    private LedgerService ledgerService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private WithdrawalService withdrawalService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void requestWithdrawal_ShouldCreateTransactionAndNotify() throws ExecutionException, InterruptedException {
        // Arrange
        String childId = "child-1";
        BigDecimal amount = BigDecimal.valueOf(50);
        User child = User.builder().id(childId).role(User.Role.CHILD).parentId("parent-1")
                .balance(BigDecimal.valueOf(100)).build();
        User parent = User.builder().id("parent-1").role(User.Role.PARENT).build();
        Transaction transaction = Transaction.builder().id("txn-1").amount(amount)
                .status(Transaction.TransactionStatus.PENDING).build();

        when(userRepository.findByIdSync(childId)).thenReturn(child);
        when(userRepository.findByIdSync("parent-1")).thenReturn(parent);
        when(ledgerService.addTransaction(eq(childId), eq(amount), anyString(),
                eq(Transaction.TransactionType.WITHDRAWAL), eq(Transaction.TransactionStatus.PENDING)))
                .thenReturn(transaction);

        // Act
        Transaction result = withdrawalService.requestWithdrawal(childId, amount);

        // Assert
        assertNotNull(result);
        assertEquals(Transaction.TransactionStatus.PENDING, result.getStatus());
        verify(notificationService).sendWithdrawalRequested(any(User.class), any(User.class), eq(amount));
    }

    @Test
    void requestWithdrawal_ShouldFailEvaluateInsufficientBalance() throws ExecutionException, InterruptedException {
        // Arrange
        String childId = "child-1";
        BigDecimal amount = BigDecimal.valueOf(150); // More than 100
        User child = User.builder().id(childId).role(User.Role.CHILD).balance(BigDecimal.valueOf(100)).build();

        when(userRepository.findByIdSync(childId)).thenReturn(child);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> withdrawalService.requestWithdrawal(childId, amount));
    }

    @Test
    void approveWithdrawal_ShouldUpdateStatusAndNotify() throws ExecutionException, InterruptedException {
        // Arrange
        String parentId = "parent-1";
        String txnId = "txn-1";
        String proof = "proof-url";
        User parent = User.builder().id(parentId).role(User.Role.PARENT).build();
        User child = User.builder().id("child-1").role(User.Role.CHILD).parentId(parentId).build();
        Transaction transaction = Transaction.builder().id(txnId).childId("child-1")
                .status(Transaction.TransactionStatus.PENDING).amount(BigDecimal.TEN).build();

        when(userRepository.findByIdSync(parentId)).thenReturn(parent);
        when(userRepository.findByIdSync("child-1")).thenReturn(child);
        when(transactionRepository.findById(txnId)).thenReturn(transaction);

        // Act
        withdrawalService.approveWithdrawal(parentId, txnId, proof);

        // Assert
        assertEquals(Transaction.TransactionStatus.PAID, transaction.getStatus());
        assertEquals(proof, transaction.getPaymentProof());
        verify(notificationService).sendWithdrawalPaid(any(User.class), eq(BigDecimal.TEN));
        verify(transactionRepository).save(transaction);
    }
}
