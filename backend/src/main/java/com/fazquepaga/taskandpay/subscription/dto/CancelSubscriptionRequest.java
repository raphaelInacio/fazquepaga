package com.fazquepaga.taskandpay.subscription.dto;

import com.fazquepaga.taskandpay.subscription.CancellationReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelSubscriptionRequest {

    @NotNull(message = "Cancellation reason is required")
    private CancellationReason reason;

    @Size(max = 500, message = "Reason details cannot exceed 500 characters")
    private String reasonDetails;
}
