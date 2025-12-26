package com.fazquepaga.taskandpay.payment;

import com.fazquepaga.taskandpay.security.JwtAuthenticationFilter;
import com.fazquepaga.taskandpay.subscription.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AsaasWebhookController.class, properties = "asaas.webhook.accessToken=test-token")
@AutoConfigureMockMvc(addFilters = false) // Optional: Verify if we want security filters or not. We DO want to test
                                          // security config, so maybe remove this or keep true.
// Actually, we modified SecurityConfig to ALLOW this endpoint. So we want
// security enabled to verify that permitted matchers work.
// So addFilters = true (default).
public class AsaasWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubscriptionService subscriptionService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter; // Required for SecurityConfig

    @Test
    public void shouldReturn403WhenTokenIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/webhooks/asaas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"event\": \"PAYMENT_RECEIVED\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturn403WhenTokenIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/webhooks/asaas")
                .header("asaas-access-token", "wrong-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"event\": \"PAYMENT_RECEIVED\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturn200WhenTokenIsValid() throws Exception {
        // Mock payload
        String json = "{" +
                "\"event\": \"PAYMENT_RECEIVED\"," +
                "\"payment\": {" +
                "\"externalReference\": \"user123\"," +
                "\"subscription\": \"sub123\"" +
                "}" +
                "}";

        mockMvc.perform(post("/api/v1/webhooks/asaas")
                .header("asaas-access-token", "test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }
}
