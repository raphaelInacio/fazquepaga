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

        AsaasCheckoutRequest request = AsaasCheckoutRequest.builder()
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
                .externalReference(user.getId())
                .notificationEnabled(true)
                .customer(user.getAsaasCustomerId())
                .build();

        try {
            AsaasCheckoutResponse response = restTemplate.postForObject("/checkouts", request,
                    AsaasCheckoutResponse.class);
            if (response != null && response.getLink() != null) {
                log.info("Checkout Session created: {}", response.getId());
                // Save the checkout session ID to the user for correlation
                user.setLastCheckoutSessionId(response.getId());
                userRepository.save(user);

                return response.getLink();
            } else {
                throw new RuntimeException(
                        "Failed to create Checkout Session: Empty response or missing link");
            }
        } catch (Exception e) {
            log.error("Error creating Checkout Session", e);
            throw new RuntimeException(
                    "Failed to create Checkout Session: Empty response or missing link");
        }
    }
}
