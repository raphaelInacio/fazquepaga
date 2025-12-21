package com.fazquepaga.taskandpay.allowance;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.fazquepaga.taskandpay.notification.NotificationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

@Service
public class WithdrawalService {

    private final LedgerService ledgerService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final TransactionRepository transactionRepository;

    public WithdrawalService(
            LedgerService ledgerService,
            UserRepository userRepository,
            NotificationService notificationService,
            TransactionRepository transactionRepository) {
        this.ledgerService = ledgerService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.transactionRepository = transactionRepository;
    }

    public Transaction requestWithdrawal(String childId, BigDecimal amount)
            throws ExecutionException, InterruptedException {
        User child = userRepository.findByIdSync(childId);
        if (child == null || child.getRole() != User.Role.CHILD) {
            throw new IllegalArgumentException("Child not found");
        }

        BigDecimal currentBalance = child.getBalance() != null ? child.getBalance() : BigDecimal.ZERO;
        if (currentBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        // Create transaction - Deduct immediately (Status: PENDING)
        Transaction transaction = ledgerService.addTransaction(
                childId,
                amount,
                "Withdrawal Request",
                Transaction.TransactionType.WITHDRAWAL,
                Transaction.TransactionStatus.PENDING);

        // Notify Parent
        try {
            User parent = userRepository.findByIdSync(child.getParentId());
            if (parent != null) {
                notificationService.sendWithdrawalRequested(parent, child, amount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return transaction;
    }

    public Transaction approveWithdrawal(String parentId, String transactionId, String paymentProof)
            throws ExecutionException, InterruptedException {
        User parent = userRepository.findByIdSync(parentId);
        if (parent == null || parent.getRole() != User.Role.PARENT) {
            throw new IllegalArgumentException("User is not a parent");
        }

        Transaction transaction = transactionRepository.findById(transactionId);

        if (transaction == null) {
            throw new IllegalArgumentException("Transaction not found");
        }

        if (transaction.getStatus() != Transaction.TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction is not pending");
        }

        // Verify parent owns the child of this transaction
        User child = userRepository.findByIdSync(transaction.getChildId());
        if (child == null || !child.getParentId().equals(parentId)) {
            throw new IllegalArgumentException("Unauthorized");
        }

        transaction.setStatus(Transaction.TransactionStatus.PAID);
        transaction.setPaymentProof(paymentProof);
        transactionRepository.save(transaction);

        // Notify Child
        try {
            notificationService.sendWithdrawalPaid(child, transaction.getAmount());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return transaction;
    }

    public Transaction rejectWithdrawal(String parentId, String transactionId, String reason)
            throws ExecutionException, InterruptedException {
        User parent = userRepository.findByIdSync(parentId);
        if (parent == null || parent.getRole() != User.Role.PARENT) {
            throw new IllegalArgumentException("User is not a parent");
        }

        Transaction transaction = transactionRepository.findById(transactionId);
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction not found");
        }

        if (transaction.getStatus() != Transaction.TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction is not pending");
        }

        // Verify parent owns the child of this transaction
        User child = userRepository.findByIdSync(transaction.getChildId());
        if (child == null || !child.getParentId().equals(parentId)) {
            throw new IllegalArgumentException("Unauthorized");
        }

        // Refund
        ledgerService.addTransaction(
                transaction.getChildId(),
                transaction.getAmount(),
                "Refund: Withdrawal Rejected (" + reason + ")",
                Transaction.TransactionType.CREDIT,
                Transaction.TransactionStatus.COMPLETED);

        transaction.setStatus(Transaction.TransactionStatus.REJECTED);
        transaction.setDescription(transaction.getDescription() + " [REJECTED: " + reason + "]");
        transactionRepository.save(transaction);

        return transaction;
    }
}
