package com.fazquepaga.taskandpay.giftcard;

import com.fazquepaga.taskandpay.giftcard.dto.RVHubCaptureResponse;
import com.fazquepaga.taskandpay.giftcard.dto.RVHubTokenResponse;
import com.fazquepaga.taskandpay.giftcard.dto.RVHubTransactionRequest;
import com.fazquepaga.taskandpay.giftcard.dto.RVHubTransactionResponse;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class RVHubClientImpl implements RVHubClient {

    private static final long DEFAULT_TOKEN_EXPIRES_IN_SECONDS = 3600L;
    private static final long TOKEN_EXPIRY_BUFFER_SECONDS = 60L;

    private final String clientId;
    private final String clientSecret;
    private final String apiUrl;
    private final String authUrl;
    private final RestTemplate restTemplate;

    private String cachedToken;
    private Instant tokenExpiryInstant;

    public RVHubClientImpl(
            @Value("${rvhub.client-id}") String clientId,
            @Value("${rvhub.client-secret}") String clientSecret,
            @Value("${rvhub.url}") String apiUrl,
            @Value("${rvhub.auth-url}") String authUrl,
            RestTemplateBuilder restTemplateBuilder) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.apiUrl = apiUrl;
        this.authUrl = authUrl;
        this.restTemplate =
                restTemplateBuilder
                        .setConnectTimeout(Duration.ofSeconds(5))
                        .setReadTimeout(Duration.ofSeconds(10))
                        .build();
    }

    @Override
    public synchronized String authenticate() {
        log.info("Requesting new authentication token from RVHub");
        String url = authUrl + "/oauth2/token?grant_type=client_credentials";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String auth = clientId + ":" + clientSecret;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.US_ASCII));
        String authHeader = "Basic " + new String(encodedAuth);
        headers.set("Authorization", authHeader);

        HttpEntity<String> entity = new HttpEntity<>("", headers);

        try {
            ResponseEntity<RVHubTokenResponse> response =
                    restTemplate.postForEntity(url, entity, RVHubTokenResponse.class);

            if (response.getBody() != null && response.getBody().getAccessToken() != null) {
                RVHubTokenResponse tokenResponse = response.getBody();
                this.cachedToken = tokenResponse.getAccessToken();
                // Expira em expiresIn segundos, mas com margem de segurança de 60 segundos
                long expiresIn =
                        tokenResponse.getExpiresIn() != null
                                ? tokenResponse.getExpiresIn()
                                : DEFAULT_TOKEN_EXPIRES_IN_SECONDS;
                this.tokenExpiryInstant = Instant.now().plusSeconds(expiresIn);
                log.info("RVHub authentication successful. Token expires in {} seconds", expiresIn);
                return this.cachedToken;
            } else {
                throw new RVHubIntegrationException(
                        "Received empty token response from RVHub",
                        response.getStatusCode(),
                        "Empty body");
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error(
                    "Error authenticating with RVHub. Status: {}, Response: {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new RVHubIntegrationException(
                    "Failed to authenticate with RVHub",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Unexpected error authenticating with RVHub", e);
            throw new RuntimeException("Unexpected error authenticating with RVHub", e);
        }
    }

    private synchronized String getValidToken() {
        if (cachedToken == null
                || tokenExpiryInstant == null
                || Instant.now()
                        .isAfter(tokenExpiryInstant.minusSeconds(TOKEN_EXPIRY_BUFFER_SECONDS))) {
            return authenticate();
        }
        return cachedToken;
    }

    @Override
    public RVHubTransactionResponse requestPinTopup(
            String productId, BigDecimal amount, String idempotencyKey) {
        log.info(
                "Requesting PIN Topup from RVHub for product: {}, amount: {}, idempotencyKey: {}",
                productId,
                amount,
                idempotencyKey);
        String url = apiUrl + "/pin-topups/transactions";

        String token = getValidToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        headers.set("X-Idempotency-Key", idempotencyKey);

        RVHubTransactionRequest request =
                RVHubTransactionRequest.builder().productId(productId).amount(amount).build();

        HttpEntity<RVHubTransactionRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<RVHubTransactionResponse> response =
                    restTemplate.postForEntity(url, entity, RVHubTransactionResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error(
                    "Error requesting PIN Topup from RVHub. Status: {}, Response: {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new RVHubIntegrationException(
                    "Failed to request PIN Topup from RVHub",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Unexpected error requesting PIN Topup from RVHub", e);
            throw new RuntimeException("Unexpected error requesting PIN Topup from RVHub", e);
        }
    }

    @Override
    public RVHubCaptureResponse capturePinTopup(String transactionId) {
        log.info("Capturing PIN Topup for transactionId: {}", transactionId);
        String url = apiUrl + "/pin-topups/transactions/" + transactionId + "/capture";

        String token = getValidToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<String> entity = new HttpEntity<>("{}", headers);

        try {
            ResponseEntity<RVHubCaptureResponse> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, RVHubCaptureResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error(
                    "Error capturing PIN Topup from RVHub. Status: {}, Response: {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new RVHubIntegrationException(
                    "Failed to capture PIN Topup from RVHub",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Unexpected error capturing PIN Topup from RVHub", e);
            throw new RuntimeException("Unexpected error capturing PIN Topup from RVHub", e);
        }
    }
}
