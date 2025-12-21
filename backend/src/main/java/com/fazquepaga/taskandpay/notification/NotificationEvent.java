package com.fazquepaga.taskandpay.notification;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private NotificationType type;
    private String recipientPhone;
    private String recipientName;
    private Map<String, String> data; // Dynamic data for templates (e.g., taskName, amount)
}
