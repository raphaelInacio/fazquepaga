package com.fazquepaga.taskandpay.notification;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.tasks.Task;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

class NotificationServiceTest {

    @Mock private PubSubTemplate pubSubTemplate;

    @Mock private ObjectMapper objectMapper;

    @InjectMocks private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(notificationService, "notificationTopic", "test-topic");
    }

    @Test
    void sendTaskCompleted_ShouldPublishEvent() throws JsonProcessingException {
        // Arrange
        Task task = Task.builder().id("task-1").description("Task 1").build();
        User child = User.builder().name("Child").build();
        User parent = User.builder().name("Parent").phoneNumber("123456789").build();

        when(objectMapper.writeValueAsString(
                        org.mockito.ArgumentMatchers.any(NotificationEvent.class)))
                .thenReturn("{\"type\":\"TASK_COMPLETED\"}");

        // Act
        notificationService.sendTaskCompleted(task, child, parent);

        // Assert
        verify(pubSubTemplate).publish(eq("test-topic"), anyString());
    }

    @Test
    void sendTaskApproved_ShouldPublishEvent() throws JsonProcessingException {
        // Arrange
        Task task = Task.builder().id("task-1").description("Task 1").value(BigDecimal.TEN).build();
        User child = User.builder().name("Child").phoneNumber("987654321").build();

        when(objectMapper.writeValueAsString(
                        org.mockito.ArgumentMatchers.any(NotificationEvent.class)))
                .thenReturn("{\"type\":\"TASK_APPROVED\"}");

        // Act
        notificationService.sendTaskApproved(task, child);

        // Assert
        verify(pubSubTemplate).publish(eq("test-topic"), anyString());
    }
}
