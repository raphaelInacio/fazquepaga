package com.fazquepaga.taskandpay.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazquepaga.taskandpay.whatsapp.WhatsAppClient;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHandler;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final ObjectMapper objectMapper;
    private final WhatsAppClient whatsAppClient;

    @Bean(name = "taskNotificationListener")
    @ServiceActivator(inputChannel = "notificationInputChannel")
    public MessageHandler notificationMessageReceiver() {
        return message -> {
            log.info("Message received from Pub/Sub");
            String payload = new String((byte[]) message.getPayload());
            BasicAcknowledgeablePubsubMessage originalMessage = message.getHeaders()
                    .get(
                            GcpPubSubHeaders.ORIGINAL_MESSAGE,
                            BasicAcknowledgeablePubsubMessage.class);

            try {
                NotificationEvent event = objectMapper.readValue(payload, NotificationEvent.class);
                processNotification(event);
                if (originalMessage != null) {
                    originalMessage.ack();
                }
            } catch (Exception e) {
                log.error("Error processing notification message. Acknowledging to prevent loop.", e);
                if (originalMessage != null) {
                    originalMessage.ack();
                }
            }
        };
    }

    private void processNotification(NotificationEvent event) {
        String messageBody = getMessageBody(event);
        if (messageBody != null && event.getRecipientPhone() != null) {
            whatsAppClient.sendMessage(event.getRecipientPhone(), messageBody);
            log.info(
                    "WhatsApp sent to {} for event {}", event.getRecipientPhone(), event.getType());
        } else {
            log.warn("Skipping notification: Invalid recipient or body. Event: {}", event);
        }
    }

    private String getMessageBody(NotificationEvent event) {
        switch (event.getType()) {
            case TASK_COMPLETED:
                return String.format(
                        "Olá %s! %s completou a tarefa '%s'. Acesse o app para aprovar!",
                        event.getRecipientName(),
                        event.getData().get("childName"),
                        event.getData().get("taskName"));
            case TASK_APPROVED:
                return String.format(
                        "Parabéns %s! Sua tarefa '%s' foi aprovada e você ganhou R$ %s.",
                        event.getRecipientName(),
                        event.getData().get("taskName"),
                        event.getData().get("reward"));
            case WITHDRAWAL_REQUESTED:
                return String.format(
                        "Olá %s! Solicitação de saque de R$ %s recebida.",
                        event.getRecipientName(), event.getData().get("amount"));
            case WITHDRAWAL_PAID:
                return String.format(
                        "Ótima notícia %s! O saque de R$ %s foi pago.",
                        event.getRecipientName(), event.getData().get("amount"));
            default:
                return null;
        }
    }
}
