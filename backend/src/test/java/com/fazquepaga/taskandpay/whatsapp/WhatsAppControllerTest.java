package com.fazquepaga.taskandpay.whatsapp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = WhatsAppController.class)
@AutoConfigureMockMvc(addFilters = false)
class WhatsAppControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockBean private WhatsAppService whatsAppService;
    @MockBean private com.fazquepaga.taskandpay.identity.UserRepository userRepository;
    @MockBean private com.fazquepaga.taskandpay.security.JwtService jwtService;
    @MockBean private com.fazquepaga.taskandpay.security.RateLimitService rateLimitService;
    @MockBean private com.fazquepaga.taskandpay.security.RateLimitConfig rateLimitConfig;

    @MockBean private TwilioRequestValidator requestValidator;

    @Test
    void shouldCallWebhookHandler() throws Exception {
        Map<String, String> payload =
                Map.of(
                        "Body", "test message",
                        "From", "whatsapp:+1234567890");

        when(requestValidator.validate(any(), any())).thenReturn(true);

        mockMvc.perform(
                        post("/api/v1/whatsapp/webhook")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        verify(whatsAppService).handleWebhook(payload);
    }
}
