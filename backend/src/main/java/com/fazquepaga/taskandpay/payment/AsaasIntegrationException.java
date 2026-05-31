package com.fazquepaga.taskandpay.payment;

import org.springframework.http.HttpStatusCode;

public class AsaasIntegrationException extends RuntimeException {
    private final HttpStatusCode statusCode;
    private final String responseBody;

    public AsaasIntegrationException(
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
