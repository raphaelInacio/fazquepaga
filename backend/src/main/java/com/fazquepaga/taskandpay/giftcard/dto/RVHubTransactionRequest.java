package com.fazquepaga.taskandpay.giftcard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RVHubTransactionRequest {
    @JsonProperty("product_id")
    private String productId;

    private BigDecimal amount;
}
