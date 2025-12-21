package com.fazquepaga.taskandpay.allowance;

import com.google.cloud.firestore.annotation.DocumentId;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @DocumentId
    private String id;

    private String childId;
    private BigDecimal amount;
    private String description;
    private Instant date;
    private TransactionType type;
    private TransactionStatus status;
    private String paymentProof;

    public enum TransactionType {
        CREDIT,
        DEBIT,
        WITHDRAWAL,
        TASK_EARNING
    }

    public enum TransactionStatus {
        PENDING,
        PAID,
        REJECTED,
        COMPLETED // For immediate transactions like tasks
    }
}
