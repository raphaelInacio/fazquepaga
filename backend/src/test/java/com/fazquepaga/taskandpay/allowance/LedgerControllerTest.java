package com.fazquepaga.taskandpay.allowance;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(controllers = LedgerController.class)
@AutoConfigureMockMvc(addFilters = false)
class LedgerControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private LedgerService ledgerService;
        @MockBean
        private com.fazquepaga.taskandpay.identity.UserRepository userRepository;
        @MockBean
        private com.fazquepaga.taskandpay.security.JwtService jwtService;

        @Test
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
                                .date(Instant.now())
                                .build();
                List<Transaction> transactions = Collections.singletonList(transaction);

                LedgerResponse ledgerResponse = LedgerResponse.builder()
                                .transactions(transactions)
                                .balance(new BigDecimal("10.00"))
                                .build();

                when(ledgerService.getTransactions(eq(childId), eq(parentId))).thenReturn(ledgerResponse);

                // When & Then
                mockMvc.perform(
                                get("/api/v1/children/{childId}/ledger", childId)
                                                .param("parent_id", parentId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.transactions[0].id").value("tx-1"))
                                .andExpect(jsonPath("$.transactions[0].childId").value(childId))
                                .andExpect(jsonPath("$.transactions[0].amount").value(10.00))
                                .andExpect(jsonPath("$.balance").value(10.00));
        }

        @Test
        void shouldReturnForbiddenWhenParentIsNotAuthorized() throws Exception {
                // Given
                String childId = "child-id-1";
                String authorizedParentId = "authorized@example.com";
                String unauthorizedParentId = "unauthorized@example.com";

                User child = User.builder().id(childId).parentId(authorizedParentId).build();
                when(userRepository.findByIdSync(childId)).thenReturn(child);

                // When & Then
                mockMvc.perform(
                                get("/api/v1/children/{childId}/ledger", childId)
                                                .param("parent_id", unauthorizedParentId))
                                .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturnEmptyLedgerWhenNoTransactions() throws Exception {
                // Given
                String childId = "child-id-1";
                String parentId = "parent@example.com";
                User child = User.builder().id(childId).parentId(parentId).build();
                when(userRepository.findByIdSync(childId)).thenReturn(child);

                LedgerResponse ledgerResponse = LedgerResponse.builder()
                                .transactions(Collections.emptyList())
                                .balance(BigDecimal.ZERO)
                                .build();

                when(ledgerService.getTransactions(eq(childId), eq(parentId))).thenReturn(ledgerResponse);

                // When & Then
                mockMvc.perform(
                                get("/api/v1/children/{childId}/ledger", childId)
                                                .param("parent_id", parentId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.transactions").isEmpty())
                                .andExpect(jsonPath("$.balance").value(0));
        }
}
