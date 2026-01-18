package com.fazquepaga.taskandpay.payment;

import com.fazquepaga.taskandpay.security.JwtAuthenticationFilter;
import com.fazquepaga.taskandpay.security.RateLimitConfig;
import com.fazquepaga.taskandpay.security.RateLimitService;
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

                                        private RateLimitService rateLimitService;

                                

                                        @MockBean

                                        private RateLimitConfig rateLimitConfig;

        

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
        public void shouldActivateSubscriptionOnPaymentConfirmed() throws Exception {
                String json = "{" +
                                "\"event\": \"PAYMENT_CONFIRMED\"," +
                                "\"payment\": {" +
                                "\"externalReference\": \"user123\"," +
                                "\"subscription\": \"sub123\"," +
                                "\"customer\": \"cus_000007342312\"," +
                                "\"checkoutSession\": \"sess_12345\"" +
                                "}" +
                                "}";

                mockMvc.perform(post("/api/v1/webhooks/asaas")
                                .header("asaas-access-token", "test-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isOk());

                verify(subscriptionService).activateSubscription("cus_000007342312", "sub123", "sess_12345");
        }

        @Test
        public void shouldDeactivateSubscriptionOnPaymentRefunded() throws Exception {
                String json = "{" +
                                "\"event\": \"PAYMENT_REFUNDED\"," +
                                "\"payment\": {" +
                                "\"externalReference\": \"user123\"," +
                                "\"subscription\": \"sub123\"," +
                                "\"customer\": \"cus_000007342312\"," +
                                "\"checkoutSession\": \"sess_12345\"" +
                                "}" +
                                "}";

                mockMvc.perform(post("/api/v1/webhooks/asaas")
                                .header("asaas-access-token", "test-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isOk());

                verify(subscriptionService).deactivateSubscription("cus_000007342312",
                                com.fazquepaga.taskandpay.identity.User.SubscriptionStatus.CANCELED, "sess_12345");
        }

        @Test
        public void shouldDeactivateSubscriptionOnPaymentOverdue() throws Exception {
                String json = "{" +
                                "\"event\": \"PAYMENT_OVERDUE\"," +
                                "\"payment\": {" +
                                "\"externalReference\": \"user123\"," +
                                "\"subscription\": \"sub123\"," +
                                "\"customer\": \"cus_000007342312\"," +
                                "\"checkoutSession\": \"sess_12345\"" +
                                "}" +
                                "}";

                mockMvc.perform(post("/api/v1/webhooks/asaas")
                                .header("asaas-access-token", "test-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isOk());

                verify(subscriptionService).deactivateSubscription("cus_000007342312",
                                com.fazquepaga.taskandpay.identity.User.SubscriptionStatus.PAST_DUE, "sess_12345");
        }
}
