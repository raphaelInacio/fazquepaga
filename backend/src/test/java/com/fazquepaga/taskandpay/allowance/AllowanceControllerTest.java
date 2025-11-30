package com.fazquepaga.taskandpay.allowance;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AllowanceControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private AllowanceService allowanceService;

    @MockBean private LedgerService ledgerService;

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
        String parentId = "parent-id";
        Transaction transaction =
                Transaction.builder()
                        .id("tx-1")
                        .amount(BigDecimal.valueOf(10.0))
                        .description("Task Reward")
                        .type(Transaction.TransactionType.CREDIT)
                        .build();

        LedgerResponse response =
                LedgerResponse.builder()
                        .transactions(List.of(transaction))
                        .balance(BigDecimal.valueOf(50.0))
                        .build();

        when(ledgerService.getTransactions(childId, parentId)).thenReturn(response);

        // When & Then
        mockMvc.perform(
                        get("/api/v1/allowance/children/{childId}/ledger", childId)
                                .param("parent_id", parentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions[0].id").value("tx-1"))
                .andExpect(jsonPath("$.transactions[0].amount").value(10.0))
                .andExpect(jsonPath("$.transactions[0].description").value("Task Reward"))
                .andExpect(jsonPath("$.transactions[0].type").value("CREDIT"))
                .andExpect(jsonPath("$.balance").value(50.0));
    }

    @Test
    void shouldGetLedgerInsights() throws Exception {
        // Given
        String childId = "child-id";
        String parentId = "parent-id";
        String aiInsight = "Parabéns! Você está economizando muito bem! 🎉";

        LedgerResponse response =
                LedgerResponse.builder()
                        .transactions(List.of())
                        .balance(BigDecimal.valueOf(50.0))
                        .build();

        when(ledgerService.getTransactions(childId, parentId)).thenReturn(response);
        when(ledgerService.getInsights(childId)).thenReturn(aiInsight);

        // When & Then
        mockMvc.perform(
                        get("/api/v1/allowance/children/{childId}/ledger/insights", childId)
                                .param("parent_id", parentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.insight").value(aiInsight));
    }
}
