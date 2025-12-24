package com.fazquepaga.taskandpay.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.fazquepaga.taskandpay.whatsapp.MockWhatsAppClient;
import com.fazquepaga.taskandpay.whatsapp.WhatsAppClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "twilio.enabled=false")
public class NotificationListenerIntegrationTest {

    @Autowired
    private WhatsAppClient whatsAppClient;

    @Test
    public void shouldInjectMockWhatsAppClient_WhenTwilioDisabled() {
        assertThat(whatsAppClient).isInstanceOf(MockWhatsAppClient.class);
    }
}
