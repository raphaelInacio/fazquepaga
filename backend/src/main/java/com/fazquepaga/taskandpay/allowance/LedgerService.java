package com.fazquepaga.taskandpay.allowance;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class LedgerService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public LedgerService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public void addTransaction(String childId, BigDecimal amount, String description, Transaction.TransactionType type)
            throws ExecutionException, InterruptedException {
        User child = userRepository.findByIdSync(childId);
        if (child == null) {
            throw new IllegalArgumentException("Child not found");
        }

        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID().toString())
                .childId(childId)
                .amount(amount)
                .description(description)
                .date(Instant.now())
                .type(type)
                .build();

        transactionRepository.save(transaction);

        // Update balance
        BigDecimal currentBalance = child.getBalance() != null ? child.getBalance() : BigDecimal.ZERO;
        BigDecimal newBalance;
        if (type == Transaction.TransactionType.CREDIT) {
            newBalance = currentBalance.add(amount);
        } else {
            newBalance = currentBalance.subtract(amount);
        }
        child.setBalance(newBalance);
        userRepository.save(child).get();
    }

    public List<Transaction> getTransactions(String childId, String parentId)
            throws ExecutionException, InterruptedException {
        User child = userRepository.findByIdSync(childId);
        if (child == null) {
            throw new IllegalArgumentException("Child not found");
        }
        if (!child.getParentId().equals(parentId)) {
            throw new IllegalArgumentException("Child does not belong to this parent");
        }

        List<QueryDocumentSnapshot> documents = transactionRepository.findByChildId(childId).getDocuments();
        return documents.stream().map(doc -> doc.toObject(Transaction.class)).collect(Collectors.toList());
    }
}
