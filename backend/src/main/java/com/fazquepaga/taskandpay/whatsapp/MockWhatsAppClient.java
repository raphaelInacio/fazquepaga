package com.fazquepaga.taskandpay.whatsapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(name = "twilio.enabled", havingValue = "false", matchIfMissing = true)
public class MockWhatsAppClient implements WhatsAppClient {

    @Override
    public void sendMessage(String to, String message) {
        log.info("MOCK WhatsApp Message to [{}]: {}", to, message);
    }
}
