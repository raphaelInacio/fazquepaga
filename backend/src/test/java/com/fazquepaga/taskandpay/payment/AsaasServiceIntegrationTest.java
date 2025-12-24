package com.fazquepaga.taskandpay.payment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.fazquepaga.taskandpay.payment.dto.AsaasCustomerRequest;
import com.fazquepaga.taskandpay.payment.dto.AsaasCustomerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

class AsaasServiceIntegrationTest {

    @Mock private RestTemplate restTemplate;

    @Mock private UserRepository userRepository;

    @InjectMocks private AsaasService asaasService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCustomer_ShouldCreateAndReturnId_WhenSuccessful() {
        // Arrange
        User user =
                User.builder()
                        .id("123")
                        .name("John Doe")
                        .email("john@example.com")
                        .phoneNumber("11999999999")
                        .document("12345678900")
                        .build();

        AsaasCustomerResponse mockResponse = new AsaasCustomerResponse();
        mockResponse.setId("cus_0001");
        mockResponse.setName("John Doe");

        when(restTemplate.postForObject(
                        eq("/customers"),
                        any(AsaasCustomerRequest.class),
                        eq(AsaasCustomerResponse.class)))
                .thenReturn(mockResponse);

        // Act
        String customerId = asaasService.createCustomer(user);

        // Assert
        assertEquals("cus_0001", customerId);
        verify(userRepository).save(user); // Verify user is updated
        assertEquals("cus_0001", user.getAsaasCustomerId());
    }

    @Test
    void createCustomer_ShouldReturnExistingId_IfUserAlreadyHasOne() {
        // Arrange
        User user =
                User.builder().email("john@example.com").asaasCustomerId("cus_EXISTING").build();

        // Act
        String customerId = asaasService.createCustomer(user);

        // Assert
        assertEquals("cus_EXISTING", customerId);
        verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }
}
