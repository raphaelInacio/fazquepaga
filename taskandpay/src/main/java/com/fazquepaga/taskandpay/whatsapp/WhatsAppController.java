package com.fazquepaga.taskandpay.whatsapp;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/whatsapp")
public class WhatsAppController {

    private final WhatsAppService whatsAppService;
    private final TwilioRequestValidator requestValidator;

    public WhatsAppController(WhatsAppService whatsAppService, TwilioRequestValidator requestValidator) {
        this.whatsAppService = whatsAppService;
        this.requestValidator = requestValidator;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody Map<String, String> webhookPayload, HttpServletRequest request) {
        // The body needs to be a map of the POST parameters for validation, not the raw JSON.
        // We are passing an empty map for now.
        if (!requestValidator.validate(request, "")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        whatsAppService.handleWebhook(webhookPayload);
        return ResponseEntity.ok().build();
    }
}
