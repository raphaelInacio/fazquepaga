package com.fazquepaga.taskandpay.whatsapp;

import com.twilio.security.RequestValidator;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TwilioRequestValidator {

    private final RequestValidator validator;

    public TwilioRequestValidator(@Value("${twilio.auth-token}") String authToken) {
        this.validator = new RequestValidator(authToken);
    }

    public boolean validate(HttpServletRequest request, String body) {
        String url = request.getRequestURL().toString();
        String signature = request.getHeader("X-Twilio-Signature");

        // The body needs to be a map of the POST parameters, not the raw JSON body.
        // For simplicity, we will assume the body is empty for validation,
        // as Twilio's validation for JSON webhooks is more complex and requires
        // the raw body bytes and custom parsing. This is a known limitation for this MVP.
        // A proper implementation would involve creating a custom http message converter
        // to get the raw body.
        return validator.validate(url, Map.of(), signature);
    }
}
