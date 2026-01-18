package com.fazquepaga.taskandpay.ai;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazquepaga.taskandpay.identity.IdentityService;
import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import java.time.LocalDate;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.annotation.DirtiesContext;

@Disabled("Integration tests temporarily disabled")
@SpringBootTest
@AutoConfigureMockMvc
// Ensure rate limits don't interfere with quota tests (set high or mock config)
@TestPropertySource(properties = {
                "ratelimit.enabled=false",
                "debug=true"
})
@DirtiesContext
class AIQuotaIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private AIQuotaRepository aiQuotaRepository;

        @MockitoBean
        private UserRepository userRepository;

        @MockitoBean
        private IdentityService identityService;

        @MockitoBean
        private AiSuggestionService aiSuggestionService;

        @BeforeEach
        void setup() throws Exception {
                when(aiSuggestionService.getSuggestions(any(Integer.class), any(String.class), any()))
                                .thenReturn(Collections.singletonList("Suggestion 1"));

                User mockUser = new User();
                mockUser.setId("test-user-id");
                mockUser.setName("Test");
                mockUser.setSubscriptionTier(User.SubscriptionTier.FREE);
                mockUser.setSubscriptionStatus(User.SubscriptionStatus.ACTIVE);

                when(userRepository.findByIdSync("test-user-id")).thenReturn(mockUser);
        }

        @Test
        void shouldAllowRequest_whenQuotaAvailable() throws Exception {
                // Mock quota: Used 0, Limit 5
                AIQuota quota = AIQuota.builder()
                                .userId("test-user-id")
                                .usedToday(0)
                                .dailyLimit(5)
                                .lastResetDate(LocalDate.now().toString())
                                .build();

                when(aiQuotaRepository.findByUserId("test-user-id")).thenReturn(quota);

                User userPrincipal = User.builder()
                                .id("test-user-id")
                                .email("test@test.com")
                                .role(User.Role.PARENT)
                                .build();

                mockMvc.perform(get("/api/v1/ai/tasks/suggestions?age=10")
                                .with(user(userPrincipal)))
                                .andExpect(status().isOk());
        }

        @Test
        void shouldBlockRequest_whenQuotaExceeded() throws Exception {
                // Mock quota: Used 5, Limit 5
                AIQuota quota = AIQuota.builder()
                                .userId("test-user-id")
                                .usedToday(5)
                                .dailyLimit(5)
                                .lastResetDate(LocalDate.now().toString())
                                .build();

                when(aiQuotaRepository.findByUserId("test-user-id")).thenReturn(quota);

                User userPrincipal = User.builder()
                                .id("test-user-id")
                                .email("test@test.com")
                                .role(User.Role.PARENT)
                                .build();

                mockMvc.perform(get("/api/v1/ai/tasks/suggestions?age=10")
                                .with(user(userPrincipal)))
                                .andExpect(status().isTooManyRequests()); // ExceptionHandler maps to 429
        }
}
