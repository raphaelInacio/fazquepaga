package com.fazquepaga.taskandpay.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Disabled;

@Disabled("Integration tests temporarily disabled")
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // We mock beans that might be initialized by SecurityConfig or Filters to avoid
    // startup errors
    @MockitoBean
    private com.fazquepaga.taskandpay.security.RecaptchaService recaptchaService;
    @MockitoBean
    private com.fazquepaga.taskandpay.security.JwtService jwtService;
    @MockitoBean
    private com.fazquepaga.taskandpay.security.RefreshTokenService refreshTokenService;
    @MockitoBean
    private com.fazquepaga.taskandpay.identity.IdentityService identityService;

    @Test
    void publicEndpoints_shouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/v1/auth/login")) // POST required but access should be allowed (400 or 405, not
                                                   // 403/401)
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError("Public endpoint blocked: " + status);
                    }
                });

        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk()); // If static resource exists
    }

    @Test
    void protectedEndpoints_shouldRequireAuth() throws Exception {
        mockMvc.perform(get("/api/v1/allowance/predicted"))
                .andExpect(status().isForbidden()); // 403 because CSRF disabled but no auth
        // Or 401 if JwtFilter runs and finds no token?
        // Spring Security default for anonymous is to allow until
        // FilterSecurityInterceptor.
        // If "authenticated()" is required, it returns 403 Forbidden (for anonymous) or
        // 401 (if entry point sends challenge).
        // With JWT filter, if no token, context is empty -> anonymous.
        // AnyRequest.authenticated() -> AccessDeniedException -> 403 (or 401 if
        // configured).
        // We just check it is NOT 200.
    }

    @Test
    @WithMockUser
    void protectedEndpoints_shouldAllowAuth() throws Exception {
        // Mock dependent services for the endpoint if needed.
        // checking a simple endpoint or just that 403 is NOT returned.
        mockMvc.perform(get("/api/v1/users/me")) // Assuming this exists or similar
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 401 || status == 403) {
                        throw new AssertionError("Authenticated request blocked: " + status);
                    }
                });
    }
}
