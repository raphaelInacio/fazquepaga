package com.fazquepaga.taskandpay.payment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.fazquepaga.taskandpay.payment.dto.AsaasCheckoutRequest;
import com.fazquepaga.taskandpay.payment.dto.AsaasCheckoutResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.junit.jupiter.api.Disabled;

@Disabled("Integration tests temporarily disabled")
class AsaasServiceIntegrationTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AsaasService asaasService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Inject values for properties used in createCheckoutSession
        ReflectionTestUtils.setField(asaasService, "subscriptionPrice", "29.90");
        ReflectionTestUtils.setField(asaasService, "subscriptionName", "Premium");
        ReflectionTestUtils.setField(asaasService, "subscriptionCycle", "MONTHLY");
        ReflectionTestUtils.setField(asaasService, "successUrl", "http://success");
        ReflectionTestUtils.setField(asaasService, "cancelUrl", "http://cancel");
    }

    @Test
    void createCheckoutSession_ShouldReturnLinkAndSaveSessionId_WhenSuccessful() {
        // Arrange
        User user = User.builder()
                .id("user123")
                .email("john@example.com")
                .asaasCustomerId("cus_EXISTING")
                .build();

        AsaasCheckoutResponse mockResponse = new AsaasCheckoutResponse();
        mockResponse.setId("sess_12345");
        mockResponse.setLink("https://sandbox.asaas.com/checkout/sess_12345");

        when(restTemplate.postForObject(
                eq("/checkouts"),
                any(AsaasCheckoutRequest.class),
                eq(AsaasCheckoutResponse.class)))
                .thenReturn(mockResponse);

        // Act
        String checkoutUrl = asaasService.createCheckoutSession(user);

        // Assert
        assertEquals("https://sandbox.asaas.com/checkout/sess_12345", checkoutUrl);

        // Verify user was updated with session ID
        assertEquals("sess_12345", user.getLastCheckoutSessionId());
        verify(userRepository).save(user);
    }
}
