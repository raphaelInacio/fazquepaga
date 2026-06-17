package com.fazquepaga.taskandpay.giftcard;

import org.springframework.http.HttpStatusCode;

public class RVHubIntegrationException extends RuntimeException {
    private final HttpStatusCode statusCode;
    private final String responseBody;

    public RVHubIntegrationException(
            String message, HttpStatusCode statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
