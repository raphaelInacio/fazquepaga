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
public class RVHubProductResponse {
    @JsonProperty("product_id")
    private String productId;
    private String name;
    private String provider;
    private BigDecimal amount;
    @JsonProperty("fixed_amount")
    private boolean fixedAmount;
    @JsonProperty("minimum_amount")
    private BigDecimal minimumAmount;
    @JsonProperty("maximum_amount")
    private BigDecimal maximumAmount;
    private String description;
}
