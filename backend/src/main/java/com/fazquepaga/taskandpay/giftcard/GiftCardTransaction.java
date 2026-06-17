package com.fazquepaga.taskandpay.giftcard;

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
public class GiftCardTransaction {

    @DocumentId private String id;

    private String childId;
    private String parentId;
    private String productId;
    private BigDecimal amount;
    private Status status;
    private String asaasPaymentId;
    private String rvhubTransactionId;
    private String pinCode;
    private Instant createdAt;
    private String idempotencyKey;

    public enum Status {
        PENDING,
        APPROVED,
        FAILED,
        COMPLETED
    }
}
