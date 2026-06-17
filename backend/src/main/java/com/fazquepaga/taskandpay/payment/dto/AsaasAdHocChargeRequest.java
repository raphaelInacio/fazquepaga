package com.fazquepaga.taskandpay.payment.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsaasAdHocChargeRequest {
    private String customer;
    private String billingType;
    private BigDecimal value;
    private String dueDate;
    private String description;
    private String externalReference;
}
