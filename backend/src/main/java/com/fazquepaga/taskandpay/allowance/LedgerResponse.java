package com.fazquepaga.taskandpay.allowance;

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
public class LedgerResponse {
    private List<Transaction> transactions;
    private BigDecimal balance;
}
