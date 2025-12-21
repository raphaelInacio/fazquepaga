package com.fazquepaga.taskandpay.payment;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.fazquepaga.taskandpay.payment.dto.AsaasCustomerRequest;
import com.fazquepaga.taskandpay.payment.dto.AsaasCustomerResponse;
import com.fazquepaga.taskandpay.payment.dto.AsaasCheckoutRequest;
import com.fazquepaga.taskandpay.payment.dto.AsaasCheckoutResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class AsaasService {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    public AsaasService(@Qualifier("asaasRestTemplate") RestTemplate restTemplate, UserRepository userRepository) {
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
    }

    public String createCustomer(User user) {
        if (user.getAsaasCustomerId() != null && !user.getAsaasCustomerId().isEmpty()) {
            log.info("User {} already has Asaas ID: {}", user.getEmail(), user.getAsaasCustomerId());
            return user.getAsaasCustomerId();
        }

        log.info("Creating Asaas Customer for user: {}", user.getEmail());

        AsaasCustomerRequest request = AsaasCustomerRequest.builder()
                .name(user.getName())
                .email(user.getEmail())
                .cpfCnpj(user.getDocument())
                .mobilePhone(user.getPhoneNumber())
                .externalReference(user.getId())
                .notificationDisabled(false)
                .build();

        // Assuming sandbox environment which might be lenient on CPF, or User doesn't
        // have it yet.
        // If CPF is required, we need to add to User model. For now sending empty
        // string or handling based on actual reqs.
        // Guide says CPF is required in schema. But User model doesn't have it.
        // I will stick to what I have, maybe sending "00000000000" for sandbox if
        // needed, or just null.

        // Actually, let's fix the request construction to likely fail if fields
        // missing,
        // but for now this is the implementation.

        try {
            AsaasCustomerResponse response = restTemplate.postForObject("/customers", request,
                    AsaasCustomerResponse.class);

            if (response != null && response.getId() != null) {
                user.setAsaasCustomerId(response.getId());
                userRepository.save(user); // Async in Firestore repo, but usually fine.
                log.info("Asaas Customer Created: {}", response.getId());
                return response.getId();
            } else {
                throw new RuntimeException("Failed to create Asaas Customer: Empty response");
            }
        } catch (Exception e) {
            log.error("Error creating Asaas Customer", e);
            throw new RuntimeException("Error communicating with Asaas", e);
        }
    }

    public String createCheckoutSession(User user) {
        log.info("Creating Checkout Session for user: {}", user.getEmail());

        // TODO: Extract these to application.properties
        String successUrl = "http://localhost:3000/app/settings?success=true";
        String cancelUrl = "http://localhost:3000/app/settings?cancel=true";

        AsaasCheckoutRequest request = AsaasCheckoutRequest.builder()
                .chargeTypes(java.util.List.of("RECURRENT"))
                .billingTypes(java.util.List.of("CREDIT_CARD", "PIX", "BOLETO"))
                .items(java.util.List.of(AsaasCheckoutRequest.Item.builder()
                        .name("TaskAndPay Premium")
                        .value(new java.math.BigDecimal("29.90"))
                        .build()))
                .subscription(AsaasCheckoutRequest.SubscriptionInfo.builder()
                        .cycle("MONTHLY")
                        .description("Assinatura Mensal TaskAndPay")
                        .build())
                .callback(AsaasCheckoutRequest.CallbackInfo.builder()
                        .successUrl(successUrl)
                        .cancelUrl(cancelUrl)
                        .build())
                .externalReference(user.getId())
                .notificationEnabled(true)
                .build();

        try {
            AsaasCheckoutResponse response = restTemplate.postForObject("/checkouts", request,
                    AsaasCheckoutResponse.class);
            if (response != null && response.getCheckoutUrl() != null) {
                log.info("Checkout Session created: {}", response.getId());
                return response.getCheckoutUrl();
            } else {
                throw new RuntimeException("Failed to create Checkout Session: Empty response");
            }
        } catch (Exception e) {
            log.error("Error creating Checkout Session", e);
            throw new RuntimeException("Error communicating with Asaas for Checkout", e);
        }
    }
}
