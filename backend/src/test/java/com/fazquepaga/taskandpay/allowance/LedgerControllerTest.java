package com.fazquepaga.taskandpay.allowance;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fazquepaga.taskandpay.config.SecurityConfig;
import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = LedgerController.class,
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
class LedgerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LedgerService ledgerService;

    @MockBean
    private UserRepository userRepository;

    @Test
    @WithMockUser(username = "parent@example.com", roles = "PARENT")
    void shouldReturnChildLedgerSuccessfully() throws Exception {
        // Given
        String childId = "child-id-1";
        String parentId = "parent@example.com";
        User child = User.builder().id(childId).parentId(parentId).build();
        when(userRepository.findByIdSync(childId)).thenReturn(child);

        Transaction transaction = Transaction.builder()
                .id("tx-1")
                .childId(childId)
                .amount(new BigDecimal("10.00"))
                .description("Task completed")
                .type(Transaction.TransactionType.CREDIT)
                .date(Instant.now()) // Corrected from .timestamp(Instant.now())
                .build();
        List<Transaction> transactions = Collections.singletonList(transaction);
        // Corrected from .getTransactionsByChildId(childId)
        when(ledgerService.getTransactions(eq(childId), eq(parentId))).thenReturn(transactions);

        // When & Then
        mockMvc.perform(get("/api/v1/children/{childId}/ledger", childId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("tx-1"))
                .andExpect(jsonPath("$[0].childId").value(childId))
                .andExpect(jsonPath("$[0].amount").value(10.00));
    }

    @Test
    @WithMockUser(username = "unauthorized@example.com", roles = "PARENT")
    void shouldReturnForbiddenWhenParentIsNotAuthorized() throws Exception {
        // Given
        String childId = "child-id-1";
        String authorizedParentId = "authorized@example.com";
        User child = User.builder().id(childId).parentId(authorizedParentId).build();
        when(userRepository.findByIdSync(childId)).thenReturn(child);

        // When & Then
        mockMvc.perform(get("/api/v1/children/{childId}/ledger", childId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "parent@example.com", roles = "PARENT")
    void shouldReturnEmptyLedgerWhenNoTransactions() throws Exception {
        // Given
        String childId = "child-id-1";
        String parentId = "parent@example.com";
        User child = User.builder().id(childId).parentId(parentId).build();
        when(userRepository.findByIdSync(childId)).thenReturn(child);
        // Corrected from .getTransactionsByChildId(childId)
        when(ledgerService.getTransactions(eq(childId), eq(parentId))).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/children/{childId}/ledger", childId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        // Given
        String childId = "child-id-1";

        // When & Then
        mockMvc.perform(get("/api/v1/children/{childId}/ledger", childId))
                .andExpect(status().isUnauthorized());
    }
}
