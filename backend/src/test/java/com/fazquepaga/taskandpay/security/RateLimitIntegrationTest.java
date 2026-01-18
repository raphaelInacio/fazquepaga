package com.fazquepaga.taskandpay.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazquepaga.taskandpay.ai.AIQuotaService;
import com.fazquepaga.taskandpay.ai.AiSuggestionService;
import com.fazquepaga.taskandpay.identity.IdentityService;
import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.dto.LoginRequest;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Disabled;

@Disabled("Integration tests temporarily disabled")
@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.annotation.DirtiesContext
@TestPropertySource(properties = {
        "ratelimit.enabled=true",
        "ratelimit.global-limit=10",
        "ratelimit.auth-limit=5",
        "ratelimit.ai-limit=3",
        "ratelimit.global-duration-seconds=60",
        "ratelimit.auth-duration-seconds=60",
        "ratelimit.ai-duration-seconds=60"
})
class RateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RecaptchaService recaptchaService;

    @MockitoBean
    private IdentityService identityService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private AIQuotaService aiQuotaService;

    @MockitoBean
    private AiSuggestionService aiSuggestionService;

    @BeforeEach
    void setup() throws Exception {
        when(recaptchaService.verify(any(), any())).thenReturn(true);
        when(identityService.authenticateParent(any(), any())).thenReturn(
                User.builder()
                        .id("test-id")
                        .name("Test User")
                        .email("test@test.com")
                        .role(User.Role.PARENT)
                        .build());
        when(jwtService.generateToken(any(User.class))).thenReturn("mock-token");
        when(refreshTokenService.createRefreshToken(any())).thenReturn("mock-refresh");

        when(aiSuggestionService.getSuggestions(any(Integer.class), any(String.class), any()))
                .thenReturn(Collections.singletonList("Suggestion"));
    }

    @Autowired
    private RateLimitConfig rateLimitConfig;

    @Test
    void shouldRateLimitGlobalRequests() throws Exception {
        if (rateLimitConfig.getGlobalLimit() != 10) {
            throw new IllegalStateException("Config not loaded correctly: " + rateLimitConfig.getGlobalLimit());
        }
        // Global limit is 10. Use root path
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            mockMvc.perform(get("/api/v1/global-test"))
                    .andExpect(result -> {
                        if (result.getResponse().getStatus() == 429) {
                            throw new AssertionError("Premature 429 exceeded at request " + finalI);
                        }
                    });
        }

        // The 11th request should be blocked
        mockMvc.perform(get("/api/v1/global-test"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("X-RateLimit-Limit", "10"))
                .andExpect(header().string("X-RateLimit-Remaining", "0"));
    }

    @Test
    void shouldRateLimitAuthRequests() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("pass");
        loginRequest.setRecaptchaToken("token");

        // Auth limit is 5.
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk());
        }

        // The 6th request should be blocked
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("X-RateLimit-Limit", "5"))
                .andExpect(header().string("X-RateLimit-Remaining", "0"));
    }

    @Test
    void shouldRateLimitAIRequests_perUser() throws Exception {
        // Must inject a User object for @AuthenticationPrincipal
        User mockUser = User.builder()
                .id("test-user-ai")
                .email("ai@test.com")
                .role(User.Role.PARENT)
                .subscriptionTier(User.SubscriptionTier.PREMIUM)
                .build();

        // AI limit is 3.
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/api/v1/ai/tasks/suggestions?age=10")
                    .with(user(mockUser)))
                    .andExpect(status().isOk());
        }

        // The 4th request should be blocked
        mockMvc.perform(get("/api/v1/ai/tasks/suggestions?age=10")
                .with(user(mockUser)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("X-RateLimit-Limit", "3"));
    }
}
