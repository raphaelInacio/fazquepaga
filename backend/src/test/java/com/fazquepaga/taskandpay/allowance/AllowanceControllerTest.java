package com.fazquepaga.taskandpay.allowance;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AllowanceController.class)
class AllowanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AllowanceService allowanceService;

    @MockBean
    private LedgerService ledgerService;

    @Test
    void shouldGetPredictedAllowance() throws Exception {
        // Given
        String childId = "child-id";
        BigDecimal predictedAllowance = BigDecimal.valueOf(100.0);

        when(allowanceService.calculatePredictedAllowance(childId)).thenReturn(predictedAllowance);

        // When & Then
        mockMvc.perform(get("/api/v1/allowance/predicted").param("child_id", childId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.predicted_allowance").value(100.0));
    }

    @Test
    void shouldGetLedger() throws Exception {
        // Given
        String childId = "child-id";
        Transaction transaction = Transaction.builder()
                .id("tx-1")
                .amount(BigDecimal.valueOf(10.0))
                .description("Task Reward")
                .type(Transaction.TransactionType.CREDIT)
                .build();

        when(ledgerService.getTransactions(childId, "parent-id")).thenReturn(List.of(transaction));

        // When & Then
        mockMvc.perform(get("/api/v1/allowance/children/{childId}/ledger", childId)
                .param("parent_id", "parent-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("tx-1"))
                .andExpect(jsonPath("$[0].amount").value(10.0))
                .andExpect(jsonPath("$[0].description").value("Task Reward"))
                .andExpect(jsonPath("$[0].type").value("CREDIT"));
    }

    @Test
    void shouldReturnEmptyLedgerWhenNoTransactions() throws Exception {
        // Given
        String childId = "child-id";
        when(ledgerService.getTransactions(childId, "parent-id")).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/allowance/children/{childId}/ledger", childId)
                .param("parent_id", "parent-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
