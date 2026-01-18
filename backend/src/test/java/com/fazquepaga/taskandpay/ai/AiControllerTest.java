package com.fazquepaga.taskandpay.ai;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fazquepaga.taskandpay.identity.User;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AiController.class)
@AutoConfigureMockMvc(addFilters = false)
class AiControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private AiSuggestionService aiSuggestionService;
        @MockBean
        private AIQuotaService aiQuotaService;
        @MockBean
        private com.fazquepaga.taskandpay.identity.UserRepository userRepository;
        @MockBean
        private com.fazquepaga.taskandpay.security.JwtService jwtService;
        @MockBean
        private com.fazquepaga.taskandpay.security.RateLimitService rateLimitService;
        @MockBean
        private com.fazquepaga.taskandpay.security.RateLimitConfig rateLimitConfig;

        private User testUser;

        @BeforeEach
        void setUp() {
                testUser = User.builder()
                                .id("user-123")
                                .email("parent@example.com")
                                .role(User.Role.PARENT)
                                .subscriptionTier(User.SubscriptionTier.PREMIUM)
                                .subscriptionStatus(User.SubscriptionStatus.ACTIVE)
                                .build();

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(testUser, null,
                                testUser.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);

                // Mock AIQuotaService to allow all calls by default
                doNothing().when(aiQuotaService).verifyQuotaOrThrow(anyString());
                doNothing().when(aiQuotaService).recordUsage(anyString());
        }

        @Test
        void shouldReturnTaskSuggestionsForAge() throws Exception {
                // Given
                int age = 10;
                List<String> suggestions = Arrays.asList(
                                "Make your bed",
                                "Clean your room",
                                "Do homework",
                                "Feed the pet",
                                "Water the plants");

                when(aiSuggestionService.getSuggestions(eq(age), anyString(), any()))
                                .thenReturn(suggestions);

                // When & Then
                mockMvc.perform(get("/api/v1/ai/tasks/suggestions").param("age", String.valueOf(age)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$.length()").value(5))
                                .andExpect(jsonPath("$[0]").value("Make your bed"))
                                .andExpect(jsonPath("$[1]").value("Clean your room"))
                                .andExpect(jsonPath("$[2]").value("Do homework"));
        }

        @Test
        void shouldReturnSuggestionsForYoungerChild() throws Exception {
                // Given
                int age = 5;
                List<String> suggestions = Arrays.asList("Put toys away", "Brush teeth", "Help set table");

                when(aiSuggestionService.getSuggestions(eq(age), anyString(), any()))
                                .thenReturn(suggestions);

                // When & Then
                mockMvc.perform(get("/api/v1/ai/tasks/suggestions").param("age", String.valueOf(age)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$.length()").value(3));
        }

        @Test
        void shouldReturnSuggestionsForOlderChild() throws Exception {
                // Given
                int age = 15;
                List<String> suggestions = Arrays.asList("Mow the lawn", "Wash the car", "Cook a meal", "Do laundry");

                when(aiSuggestionService.getSuggestions(eq(age), anyString(), any()))
                                .thenReturn(suggestions);

                // When & Then
                mockMvc.perform(get("/api/v1/ai/tasks/suggestions").param("age", String.valueOf(age)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$.length()").value(4));
        }

        @Test
        void shouldHandleEmptySuggestionsList() throws Exception {
                // Given
                int age = 10;
                when(aiSuggestionService.getSuggestions(anyInt(), anyString(), any()))
                                .thenReturn(List.of());

                // When & Then
                mockMvc.perform(get("/api/v1/ai/tasks/suggestions").param("age", String.valueOf(age)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$.length()").value(0));
        }
}
