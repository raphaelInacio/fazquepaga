package com.fazquepaga.taskandpay.shared.logging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazquepaga.taskandpay.security.RateLimitConfig;
import com.fazquepaga.taskandpay.security.RateLimitFilter;
import com.fazquepaga.taskandpay.security.RateLimitService;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ClientLogControllerTest {

    private MockMvc mockMvc;

    private ClientLogController clientLogController;

    @Mock private RateLimitService rateLimitService;

    @Mock private RateLimitConfig rateLimitConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        clientLogController = new ClientLogController();
        // Configura o filtro com o setup manual do rate limiter
        RateLimitFilter rateLimitFilter =
                new RateLimitFilter(rateLimitService, rateLimitConfig, objectMapper);
        mockMvc =
                MockMvcBuilders.standaloneSetup(clientLogController)
                        .addFilters(rateLimitFilter)
                        .build();
        // Configuração padrão dos mocks do rate limit para não bloquear por default
        lenient().when(rateLimitConfig.isEnabled()).thenReturn(true);
        lenient().when(rateLimitService.tryConsume(any(), any())).thenReturn(true);
    }

    @Test
    void shouldAcceptValidClientLogAndReturnAccepted() throws Exception {
        ClientLogRequest request =
                new ClientLogRequest(
                        "Cannot read properties of undefined",
                        "TypeError: ...",
                        "UserProfile",
                        "/dashboard/profile",
                        Map.of("browser", "Chrome"));
        mockMvc.perform(
                        post("/api/v1/logs/client")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
        // Verifica se consumiu do bucket de logs do cliente
        verify(rateLimitService).tryConsume(any(), eq(RateLimitService.BucketType.CLIENT_LOG));
    }

    @Test
    void shouldReturnBadRequestWhenRequiredFieldsAreMissing() throws Exception {
        // message em branco
        ClientLogRequest request =
                new ClientLogRequest(
                        "", "TypeError: ...", "UserProfile", "/dashboard/profile", Map.of());
        mockMvc.perform(
                        post("/api/v1/logs/client")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBlockRequestAndReturnTooManyRequestsWhenRateLimitIsExceeded() throws Exception {
        // Configura o rate limit para falhar
        when(rateLimitService.tryConsume(any(), eq(RateLimitService.BucketType.CLIENT_LOG)))
                .thenReturn(false);
        when(rateLimitService.getSecondsUntilRefill(
                        any(), eq(RateLimitService.BucketType.CLIENT_LOG)))
                .thenReturn(10L);
        ClientLogRequest request =
                new ClientLogRequest(
                        "Cannot read properties of undefined",
                        "TypeError: ...",
                        "UserProfile",
                        "/dashboard/profile",
                        Map.of());
        mockMvc.perform(
                        post("/api/v1/logs/client")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests());
    }
}
