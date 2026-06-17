package com.fazquepaga.taskandpay.giftcard;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fazquepaga.taskandpay.giftcard.dto.RVHubCaptureResponse;
import com.fazquepaga.taskandpay.giftcard.dto.RVHubTokenResponse;
import com.fazquepaga.taskandpay.giftcard.dto.RVHubTransactionResponse;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

class RVHubClientTest {

    @Mock private RestTemplate restTemplate;

    @Mock private RestTemplateBuilder restTemplateBuilder;

    private RVHubClientImpl rvhubClient;

    private final String clientId = "test-client-id";
    private final String clientSecret = "test-client-secret";
    private final String apiUrl = "https://api.sbx.rvhub.com.br";
    private final String authUrl = "https://auth.sbx.rvhub.com.br";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(restTemplateBuilder.setConnectTimeout(any())).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.setReadTimeout(any())).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        rvhubClient =
                new RVHubClientImpl(clientId, clientSecret, apiUrl, authUrl, restTemplateBuilder);
    }

    @Test
    void authenticate_ShouldReturnToken_WhenSuccessful() {
        // Arrange
        RVHubTokenResponse tokenResponse =
                RVHubTokenResponse.builder()
                        .accessToken("jwt-token-123")
                        .tokenType("Bearer")
                        .expiresIn(3600L)
                        .build();

        ResponseEntity<RVHubTokenResponse> responseEntity =
                new ResponseEntity<>(tokenResponse, HttpStatus.OK);

        when(restTemplate.postForEntity(
                        anyString(), any(HttpEntity.class), eq(RVHubTokenResponse.class)))
                .thenReturn(responseEntity);

        // Act
        String token = rvhubClient.authenticate();

        // Assert
        assertEquals("jwt-token-123", token);
        verify(restTemplate)
                .postForEntity(
                        eq(authUrl + "/oauth2/token?grant_type=client_credentials"),
                        any(HttpEntity.class),
                        eq(RVHubTokenResponse.class));
    }

    @Test
    void authenticate_ShouldThrowRVHubIntegrationException_WhenApiFails() {
        // Arrange
        when(restTemplate.postForEntity(
                        anyString(), any(HttpEntity.class), eq(RVHubTokenResponse.class)))
                .thenThrow(
                        new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized body"));

        // Act & Assert
        RVHubIntegrationException exception =
                assertThrows(RVHubIntegrationException.class, () -> rvhubClient.authenticate());

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Failed to authenticate with RVHub"));
    }

    @Test
    void requestPinTopup_ShouldUseCachedToken_WhenTokenIsValid() {
        // Arrange
        ReflectionTestUtils.setField(rvhubClient, "cachedToken", "valid-cached-token");
        ReflectionTestUtils.setField(
                rvhubClient, "tokenExpiryInstant", Instant.now().plusSeconds(600));

        RVHubTransactionResponse mockResponse =
                RVHubTransactionResponse.builder()
                        .id("trans-123")
                        .status("authorized")
                        .productId("prod-1")
                        .amount(BigDecimal.TEN)
                        .build();

        ResponseEntity<RVHubTransactionResponse> responseEntity =
                new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.postForEntity(
                        anyString(), any(HttpEntity.class), eq(RVHubTransactionResponse.class)))
                .thenReturn(responseEntity);

        // Act
        RVHubTransactionResponse response =
                rvhubClient.requestPinTopup("prod-1", BigDecimal.TEN, "idemp-key-999");

        // Assert
        assertNotNull(response);
        assertEquals("trans-123", response.getId());

        // Verify that authenticate was never called (no postForEntity to authUrl)
        verify(restTemplate, never())
                .postForEntity(
                        contains("/oauth2/token"),
                        any(HttpEntity.class),
                        eq(RVHubTokenResponse.class));

        // Verify request headers
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate)
                .postForEntity(
                        eq(apiUrl + "/pin-topups/transactions"),
                        entityCaptor.capture(),
                        eq(RVHubTransactionResponse.class));

        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        assertEquals(
                "Bearer valid-cached-token", capturedEntity.getHeaders().getFirst("Authorization"));
        assertEquals("idemp-key-999", capturedEntity.getHeaders().getFirst("X-Idempotency-Key"));
    }

    @Test
    void requestPinTopup_ShouldAuthenticate_WhenTokenIsExpired() {
        // Arrange
        // Expired token (set expiry to 30 seconds ago)
        ReflectionTestUtils.setField(rvhubClient, "cachedToken", "expired-token");
        ReflectionTestUtils.setField(
                rvhubClient, "tokenExpiryInstant", Instant.now().minusSeconds(30));

        // Mock Authentication response
        RVHubTokenResponse tokenResponse =
                RVHubTokenResponse.builder()
                        .accessToken("new-jwt-token")
                        .tokenType("Bearer")
                        .expiresIn(3600L)
                        .build();
        ResponseEntity<RVHubTokenResponse> authResponseEntity =
                new ResponseEntity<>(tokenResponse, HttpStatus.OK);

        when(restTemplate.postForEntity(
                        eq(authUrl + "/oauth2/token?grant_type=client_credentials"),
                        any(HttpEntity.class),
                        eq(RVHubTokenResponse.class)))
                .thenReturn(authResponseEntity);

        // Mock Topup response
        RVHubTransactionResponse topupResponse =
                RVHubTransactionResponse.builder().id("trans-777").status("authorized").build();
        ResponseEntity<RVHubTransactionResponse> topupResponseEntity =
                new ResponseEntity<>(topupResponse, HttpStatus.OK);

        when(restTemplate.postForEntity(
                        eq(apiUrl + "/pin-topups/transactions"),
                        any(HttpEntity.class),
                        eq(RVHubTransactionResponse.class)))
                .thenReturn(topupResponseEntity);

        // Act
        RVHubTransactionResponse response =
                rvhubClient.requestPinTopup("prod-1", BigDecimal.TEN, "idemp-key-999");

        // Assert
        assertNotNull(response);
        assertEquals("trans-777", response.getId());

        // Verify that authenticate was called
        verify(restTemplate)
                .postForEntity(
                        eq(authUrl + "/oauth2/token?grant_type=client_credentials"),
                        any(HttpEntity.class),
                        eq(RVHubTokenResponse.class));

        // Verify new token was used in topup request
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate)
                .postForEntity(
                        eq(apiUrl + "/pin-topups/transactions"),
                        entityCaptor.capture(),
                        eq(RVHubTransactionResponse.class));

        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        assertEquals("Bearer new-jwt-token", capturedEntity.getHeaders().getFirst("Authorization"));
    }

    @Test
    void capturePinTopup_ShouldReturnPinCode_WhenSuccessful() {
        // Arrange
        ReflectionTestUtils.setField(rvhubClient, "cachedToken", "valid-cached-token");
        ReflectionTestUtils.setField(
                rvhubClient, "tokenExpiryInstant", Instant.now().plusSeconds(600));

        RVHubCaptureResponse captureResponse =
                RVHubCaptureResponse.builder()
                        .id("trans-123")
                        .status("captured")
                        .pin(
                                RVHubCaptureResponse.PinInfo.builder()
                                        .code("PIN-CODE-VALUE")
                                        .pinCode("PIN-CODE-VALUE")
                                        .serial("SERIAL-111")
                                        .instructions("Instructions here")
                                        .build())
                        .build();

        ResponseEntity<RVHubCaptureResponse> responseEntity =
                new ResponseEntity<>(captureResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(RVHubCaptureResponse.class)))
                .thenReturn(responseEntity);

        // Act
        RVHubCaptureResponse response = rvhubClient.capturePinTopup("trans-123");

        // Assert
        assertNotNull(response);
        assertEquals("captured", response.getStatus());
        assertEquals("PIN-CODE-VALUE", response.getPin().getPinCode());

        // Verify endpoint URL
        verify(restTemplate)
                .exchange(
                        eq(apiUrl + "/pin-topups/transactions/trans-123/capture"),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(RVHubCaptureResponse.class));
    }

    @Test
    void capturePinTopup_ShouldThrowRVHubIntegrationException_WhenApiFails() {
        // Arrange
        ReflectionTestUtils.setField(rvhubClient, "cachedToken", "valid-cached-token");
        ReflectionTestUtils.setField(
                rvhubClient, "tokenExpiryInstant", Instant.now().plusSeconds(600));

        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(RVHubCaptureResponse.class)))
                .thenThrow(
                        new HttpServerErrorException(
                                HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));

        // Act & Assert
        RVHubIntegrationException exception =
                assertThrows(
                        RVHubIntegrationException.class,
                        () -> rvhubClient.capturePinTopup("trans-123"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Failed to capture PIN Topup from RVHub"));
    }
}
