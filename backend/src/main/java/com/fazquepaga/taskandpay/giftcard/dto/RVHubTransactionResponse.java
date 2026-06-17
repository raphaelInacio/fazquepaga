package com.fazquepaga.taskandpay.giftcard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RVHubTransactionResponse {
    private String id;

    @JsonProperty("product_id")
    private String productId;

    private BigDecimal amount;
    private String status;
    private List<Link> links;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Link {
        private String rel;
        private String href;
        private String method;
    }
}
