package com.fazquepaga.taskandpay.ai.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdventureTask {
    private String id;
    private String originalDescription;
    private String adventureDescription;
    private BigDecimal value;
    private String status;
}
