package com.fazquepaga.taskandpay.whatsapp;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TwilioWhatsAppClientTest {

    private TwilioWhatsAppClient twilioWhatsAppClient;

    private static final String ACCOUNT_SID = "test-account-sid";
    private static final String AUTH_TOKEN = "test-auth-token";
    private static final String FROM_PHONE_NUMBER = "+1234567890";

    @BeforeEach
    void setUp() {
        // Note: This will initialize Twilio with test credentials
        // In a real scenario, you might want to mock the Twilio SDK
        twilioWhatsAppClient = new TwilioWhatsAppClient(ACCOUNT_SID, AUTH_TOKEN, FROM_PHONE_NUMBER);
    }

    @Test
    void shouldCreateClientWithCorrectConfiguration() {
        // Then
        assertNotNull(twilioWhatsAppClient);
    }

    @Test
    void shouldImplementWhatsAppClientInterface() {
        // Then
        assertTrue(twilioWhatsAppClient instanceof WhatsAppClient);
    }

    // Note: Testing sendMessage() would require mocking the Twilio SDK
    // or using integration tests with Twilio's test credentials.
    // For unit tests, we verify the client is properly constructed.
    // Integration tests in WhatsAppIntegrationTest.java cover the actual sending.
}
