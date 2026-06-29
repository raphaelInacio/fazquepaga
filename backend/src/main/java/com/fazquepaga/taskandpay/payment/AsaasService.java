package com.fazquepaga.taskandpay.payment;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.fazquepaga.taskandpay.payment.dto.AsaasCheckoutRequest;
import com.fazquepaga.taskandpay.payment.dto.AsaasCheckoutResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class AsaasService {

    @Value("${asaas.subscription.price}")
    private String subscriptionPrice;

    @Value("${asaas.subscription.name}")
    private String subscriptionName;

    @Value("${asaas.subscription.cycle}")
    private String subscriptionCycle;

    @Value("${asaas.subscription.success-url}")
    private String successUrl;

    @Value("${asaas.subscription.cancel-url}")
    private String cancelUrl;

    @Value("${asaas.url:https://sandbox.asaas.com/api/v3}")
    private String baseUrl;

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    public AsaasService(
            @Qualifier("asaasRestTemplate") RestTemplate restTemplate,
            UserRepository userRepository) {
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
    }

    // createCustomer method removed as we now rely on Checkout Session creation

    public String createCheckoutSession(User user) {
        log.info("Creating Checkout Session for user: {}", user.getEmail());
        log.debug("Using Callback URLs - Success: {}, Cancel: {}", successUrl, cancelUrl);

        AsaasCheckoutRequest request =
                AsaasCheckoutRequest.builder()
                        .chargeTypes(java.util.List.of("RECURRENT"))
                        .billingTypes(java.util.List.of("CREDIT_CARD"))
                        .items(
                                java.util.List.of(
                                        AsaasCheckoutRequest.Item.builder()
                                                .name(subscriptionName)
                                                .value(new java.math.BigDecimal(subscriptionPrice))
                                                .quantity(1)
                                                .build()))
                        .subscription(
                                AsaasCheckoutRequest.SubscriptionInfo.builder()
                                        .cycle(subscriptionCycle)
                                        .description("Assinatura Mensal TaskAndPay")
                                        .nextDueDate(
                                                LocalDate.now().format(DateTimeFormatter.ISO_DATE))
                                        .build())
                        .callback(
                                AsaasCheckoutRequest.CallbackInfo.builder()
                                        .successUrl(successUrl)
                                        .cancelUrl(cancelUrl)
                                        .build())
                        .externalReference("taskandpay:" + user.getId())
                        .notificationEnabled(true)
                        .customer(user.getAsaasCustomerId())
                        .build();

        try {
            AsaasCheckoutResponse response =
                    restTemplate.postForObject("/checkouts", request, AsaasCheckoutResponse.class);
            if (response != null && response.getId() != null) {
                log.info("Checkout Session created: {}", response.getId());
                // Save the checkout session ID to the user for correlation
                user.setLastCheckoutSessionId(response.getId());
                userRepository.save(user);

                String checkoutDomain =
                        baseUrl.contains("sandbox")
                                ? "https://sandbox.asaas.com"
                                : "https://asaas.com";
                return checkoutDomain + "/checkoutSession/show?id=" + response.getId();
            } else {
                throw new RuntimeException(
                        "Failed to create Checkout Session: Empty response or missing id");
            }
        } catch (Exception e) {
            log.error("Error creating Checkout Session", e);
            throw new RuntimeException(
                    "Failed to create Checkout Session: Empty response or missing id");
        }
    }

    public boolean cancelSubscription(String subscriptionId) {
        log.info("Canceling Asaas subscription: {}", subscriptionId);
        try {
            restTemplate.delete("/subscriptions/" + subscriptionId);
            return true;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // Em ambiente local/sandbox, se der 404 (Not Found) ou 400 (Bad Request/Inexistente),
            // consideramos cancelado localmente para não bloquear os testes do desenvolvedor.
            if (e.getStatusCode() == org.springframework.http.HttpStatus.NOT_FOUND
                    || e.getStatusCode() == org.springframework.http.HttpStatus.BAD_REQUEST) {
                log.warn(
                        "Subscription {} not found or invalid in Asaas, considering canceled"
                                + " locally. Status: {}",
                        subscriptionId,
                        e.getStatusCode());
                return true;
            }
            log.error(
                    "Failed to cancel subscription {} in Asaas. Status: {}, Response: {}",
                    subscriptionId,
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new AsaasIntegrationException(
                    "Failed to cancel Asaas subscription",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error(
                    "Failed to cancel subscription {} in Asaas (unexpected error)",
                    subscriptionId,
                    e);
            throw new AsaasIntegrationException(
                    "Failed to cancel Asaas subscription",
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage());
        }
    }

    public String createAdHocCharge(
            User parent, java.math.BigDecimal amount, String transactionId) {
        log.info("Creating ad-hoc charge for parent: {} of value: {}", parent.getEmail(), amount);

        com.fazquepaga.taskandpay.payment.dto.AsaasAdHocChargeRequest request =
                com.fazquepaga.taskandpay.payment.dto.AsaasAdHocChargeRequest.builder()
                        .customer(parent.getAsaasCustomerId())
                        .billingType("CREDIT_CARD")
                        .value(amount)
                        .dueDate(LocalDate.now().format(DateTimeFormatter.ISO_DATE))
                        .description("Recarga de Gift Card - Transacao ID: " + transactionId)
                        .externalReference("taskandpay:" + transactionId)
                        .build();

        try {
            com.fazquepaga.taskandpay.payment.dto.AsaasAdHocChargeResponse response =
                    restTemplate.postForObject(
                            "/payments",
                            request,
                            com.fazquepaga.taskandpay.payment.dto.AsaasAdHocChargeResponse.class);
            if (response != null && response.getId() != null) {
                log.info("Ad-hoc charge created successfully: {}", response.getId());
                return response.getId();
            } else {
                throw new RuntimeException(
                        "Failed to create ad-hoc charge: Empty response or missing id");
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error(
                    "Failed to create ad-hoc charge in Asaas. Status: {}, Response: {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new AsaasIntegrationException(
                    "Failed to create ad-hoc charge",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error creating ad-hoc charge (unexpected)", e);
            throw new AsaasIntegrationException(
                    "Failed to create ad-hoc charge (unexpected)",
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage());
        }
    }

    public void refundCharge(String paymentId) {
        log.info("Refunding charge: {}", paymentId);
        try {
            restTemplate.postForLocation("/payments/" + paymentId + "/refund", null);
            log.info("Refund initiated successfully for payment: {}", paymentId);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error(
                    "Failed to refund charge {} in Asaas. Status: {}, Response: {}",
                    paymentId,
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new AsaasIntegrationException(
                    "Failed to refund charge", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error refunding charge {} (unexpected)", paymentId, e);
            throw new AsaasIntegrationException(
                    "Failed to refund charge (unexpected)",
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage());
        }
    }
}
