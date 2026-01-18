package com.fazquepaga.taskandpay.security;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazquepaga.taskandpay.identity.IdentityService;
import com.fazquepaga.taskandpay.identity.dto.RefreshTokenRequest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Disabled;

@Disabled("Integration tests temporarily disabled")
@SpringBootTest
@AutoConfigureMockMvc
class MockRefreshTokenIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private RefreshTokenService refreshTokenService;

        @MockitoBean
        private RecaptchaService recaptchaService; // Needed for IdentityController loading? Maybe not if I don't hit
                                                   // login.
        // Actually IdentityController is a bean, so its dependencies must be satisfied.
        // @SpringBootTest loads full context. Mocks replace beans.
        // If RecaptchaService is not mocked, it uses real one, which might fail if
        // config is missing (but config is present).
        // Safer to mock everything IdentityController needs if I don't use it.

        @MockitoBean
        private IdentityService identityService;

        @Test
        void shouldRefreshAccessToken_withValidRefreshToken() throws Exception {
                String validToken = "valid-refresh-token";
                String newAccessToken = "new-access-token";

                when(refreshTokenService.validateAndRefresh(validToken))
                                .thenReturn(Optional.of(newAccessToken));

                RefreshTokenRequest request = new RefreshTokenRequest();
                request.setRefreshToken(validToken);

                mockMvc.perform(post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").value(newAccessToken));
        }

        @Test
        void shouldReturn401_withInvalidRefreshToken() throws Exception {
                String invalidToken = "invalid-token";

                when(refreshTokenService.validateAndRefresh(invalidToken))
                                .thenReturn(Optional.empty());

                RefreshTokenRequest request = new RefreshTokenRequest();
                request.setRefreshToken(invalidToken);

                mockMvc.perform(post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.error").value("Invalid or expired refresh token"));
        }

        @Test
        @WithMockUser(username = "test-user")
        void shouldRevokeAllTokens_onLogoutAll() throws Exception {
                mockMvc.perform(post("/api/v1/auth/logout-all"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("All sessions logged out"));

                verify(refreshTokenService).revokeAllTokens("test-user");
        }
}
