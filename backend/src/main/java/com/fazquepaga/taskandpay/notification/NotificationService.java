package com.fazquepaga.taskandpay.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.tasks.Task;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final PubSubTemplate pubSubTemplate;
    private final ObjectMapper objectMapper;

    @Value("${pubsub.notification-topic}")
    private String notificationTopic;

    public void sendTaskCompleted(Task task, User child, User parent) {
        publish(NotificationEvent.builder()
                .type(NotificationType.TASK_COMPLETED)
                .recipientPhone(parent.getPhoneNumber())
                .recipientName(parent.getName())
                .data(Map.of(
                        "childName", child.getName(),
                        "taskName", task.getDescription(), // Fixed: getTitle -> getDescription
                        "taskId", task.getId()))
                .build());
    }

    public void sendTaskApproved(Task task, User child) {
        publish(NotificationEvent.builder()
                .type(NotificationType.TASK_APPROVED)
                .recipientPhone(child.getPhoneNumber())
                .recipientName(child.getName())
                .data(Map.of(
                        "taskName", task.getDescription(), // Fixed: getTitle -> getDescription
                        "reward", task.getValue() != null ? String.valueOf(task.getValue()) : "0" // Fixed: getReward ->
                                                                                                  // getValue
                ))
                .build());
    }

    private void publish(NotificationEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            pubSubTemplate.publish(notificationTopic, json);
            log.info("Notification published: Type={}, Recipient={}", event.getType(), event.getRecipientPhone());
        } catch (Exception e) {
            log.error("Failed to publish notification event", e);
        }
    }
}
