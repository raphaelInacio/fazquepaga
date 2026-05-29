package com.fazquepaga.taskandpay.subscription.dto;

import com.fazquepaga.taskandpay.identity.User.SubscriptionStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelSubscriptionResponse {

    private SubscriptionStatus status;
    private Instant cancellationDate;
    private String message;
}
