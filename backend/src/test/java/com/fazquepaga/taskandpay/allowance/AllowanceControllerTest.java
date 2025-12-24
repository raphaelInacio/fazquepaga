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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AllowanceController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.test.context.ActiveProfiles("test")
@org.springframework.test.context.TestPropertySource(properties = "asaas.api-key=dummy-test-key")
class AllowanceControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private AllowanceService allowanceService;

    @MockBean private LedgerService ledgerService;

    @MockBean private WithdrawalService withdrawalService;

    @MockBean private com.fazquepaga.taskandpay.identity.UserRepository userRepository;
    @MockBean private com.fazquepaga.taskandpay.security.JwtService jwtService;

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

    @Test
    void shouldRequestWithdrawal() throws Exception {
        // Given
        String childId = "child-id";
        BigDecimal amount = BigDecimal.valueOf(20.0);
        Transaction transaction =
                Transaction.builder()
                        .id("tx-2")
                        .amount(amount)
                        .description("Withdrawal Request")
                        .type(Transaction.TransactionType.WITHDRAWAL)
                        .status(Transaction.TransactionStatus.PENDING)
                        .build();

        when(withdrawalService.requestWithdrawal(childId, amount)).thenReturn(transaction);

        // When & Then
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                                        "/api/v1/allowance/children/{childId}/withdraw", childId)
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                .content("{\"amount\": 20.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("tx-2"))
                .andExpect(jsonPath("$.amount").value(20.0))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
