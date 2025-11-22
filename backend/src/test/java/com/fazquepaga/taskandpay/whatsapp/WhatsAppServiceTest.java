package com.fazquepaga.taskandpay.whatsapp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fazquepaga.taskandpay.identity.IdentityService;
import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.fazquepaga.taskandpay.tasks.Task;
import com.fazquepaga.taskandpay.tasks.TaskService;
import com.fazquepaga.taskandpay.whatsapp.events.ProofSubmittedEvent;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class WhatsAppServiceTest {

    @Mock
    private IdentityService identityService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskService taskService;

    @Mock
    private PubSubTemplate pubSubTemplate;

    private WhatsAppService whatsAppService;

    private static final String TOPIC_NAME = "test-topic";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        whatsAppService = new WhatsAppService(
                identityService, userRepository, taskService, pubSubTemplate, TOPIC_NAME);
    }

    @Test
    void shouldCompleteOnboardingWithValidCode() throws ExecutionException, InterruptedException {
        // Given
        String onboardingCode = "ABC123";
        String phoneNumber = "+1234567890";
        Map<String, String> payload = Map.of("Body", onboardingCode, "From", "whatsapp:" + phoneNumber);

        // completeOnboarding is void, so we don't need to mock its return value
        // Just verify it's called

        // When
        whatsAppService.handleWebhook(payload);

        // Then
        verify(identityService).completeOnboarding(onboardingCode, phoneNumber);
        verifyNoInteractions(userRepository, taskService, pubSubTemplate);
    }

    @Test
    void shouldHandleInvalidOnboardingCode() throws ExecutionException, InterruptedException {
        // Given
        String invalidCode = "INVALID";
        String phoneNumber = "+1234567890";
        Map<String, String> payload = Map.of("Body", invalidCode, "From", "whatsapp:" + phoneNumber);

        doThrow(new IllegalArgumentException("Invalid code"))
                .when(identityService)
                .completeOnboarding(invalidCode, phoneNumber);

        // When
        whatsAppService.handleWebhook(payload);

        // Then
        verify(identityService).completeOnboarding(invalidCode, phoneNumber);
        verifyNoInteractions(userRepository, taskService, pubSubTemplate);
    }

    @Test
    void shouldHandleImageMessageWithPendingTask() throws ExecutionException, InterruptedException {
        // Given
        String phoneNumber = "+1234567890";
        String imageUrl = "https://example.com/image.jpg";
        Map<String, String> payload = Map.of("From", "whatsapp:" + phoneNumber, "MediaUrl0", imageUrl);

        User child = User.builder()
                .id("child-id")
                .name("Test Child")
                .phoneNumber(phoneNumber)
                .role(User.Role.CHILD)
                .build();

        Task pendingTask = Task.builder()
                .id("task-id")
                .description("Test Task")
                .status(Task.TaskStatus.PENDING)
                .requiresProof(true)
                .createdAt(Instant.now())
                .build();

        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(child);
        when(taskService.getTasksByUserId("child-id")).thenReturn(List.of(pendingTask));
        when(pubSubTemplate.publish(eq(TOPIC_NAME), any(ProofSubmittedEvent.class)))
                .thenReturn(CompletableFuture.completedFuture("message-id"));

        // When
        whatsAppService.handleWebhook(payload);

        // Then
        verify(userRepository).findByPhoneNumber(phoneNumber);
        verify(taskService).getTasksByUserId("child-id");

        ArgumentCaptor<ProofSubmittedEvent> eventCaptor = ArgumentCaptor.forClass(ProofSubmittedEvent.class);
        verify(pubSubTemplate).publish(eq(TOPIC_NAME), eventCaptor.capture());

        ProofSubmittedEvent capturedEvent = eventCaptor.getValue();
        assertEquals("child-id", capturedEvent.getChildId());
        assertEquals("task-id", capturedEvent.getTaskId());
        assertEquals(imageUrl, capturedEvent.getImageUrl());
    }

    @Test
    void shouldHandleImageMessageWithUnknownPhoneNumber()
            throws ExecutionException, InterruptedException {
        // Given
        String phoneNumber = "+9999999999";
        String imageUrl = "https://example.com/image.jpg";
        Map<String, String> payload = Map.of("From", "whatsapp:" + phoneNumber, "MediaUrl0", imageUrl);

        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(null);

        // When
        whatsAppService.handleWebhook(payload);

        // Then
        verify(userRepository).findByPhoneNumber(phoneNumber);
        verifyNoInteractions(taskService, pubSubTemplate);
    }

    @Test
    void shouldHandleImageMessageWithNoPendingTasks()
            throws ExecutionException, InterruptedException {
        // Given
        String phoneNumber = "+1234567890";
        String imageUrl = "https://example.com/image.jpg";
        Map<String, String> payload = Map.of("From", "whatsapp:" + phoneNumber, "MediaUrl0", imageUrl);

        User child = User.builder()
                .id("child-id")
                .name("Test Child")
                .phoneNumber(phoneNumber)
                .role(User.Role.CHILD)
                .build();

        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(child);
        when(taskService.getTasksByUserId("child-id")).thenReturn(Collections.emptyList());

        // When
        whatsAppService.handleWebhook(payload);

        // Then
        verify(userRepository).findByPhoneNumber(phoneNumber);
        verify(taskService).getTasksByUserId("child-id");
        verifyNoInteractions(pubSubTemplate);
    }

    @Test
    void shouldHandleImageMessageWithNoTasksRequiringProof()
            throws ExecutionException, InterruptedException {
        // Given
        String phoneNumber = "+1234567890";
        String imageUrl = "https://example.com/image.jpg";
        Map<String, String> payload = Map.of("From", "whatsapp:" + phoneNumber, "MediaUrl0", imageUrl);

        User child = User.builder()
                .id("child-id")
                .name("Test Child")
                .phoneNumber(phoneNumber)
                .role(User.Role.CHILD)
                .build();

        Task taskWithoutProof = Task.builder()
                .id("task-id")
                .description("Test Task")
                .status(Task.TaskStatus.PENDING)
                .requiresProof(false)
                .createdAt(Instant.now())
                .build();

        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(child);
        when(taskService.getTasksByUserId("child-id")).thenReturn(List.of(taskWithoutProof));

        // When
        whatsAppService.handleWebhook(payload);

        // Then
        verify(userRepository).findByPhoneNumber(phoneNumber);
        verify(taskService).getTasksByUserId("child-id");
        verifyNoInteractions(pubSubTemplate);
    }

    @Test
    void shouldHandleWebhookWithMissingFromNumber() {
        // Given
        Map<String, String> payload = Map.of("Body", "test message");

        // When
        whatsAppService.handleWebhook(payload);

        // Then
        verifyNoInteractions(identityService, userRepository, taskService, pubSubTemplate);
    }

    @Test
    void shouldHandleMessageWithNoBodyAndNoImage() {
        // Given
        String phoneNumber = "+1234567890";
        Map<String, String> payload = Map.of("From", "whatsapp:" + phoneNumber);

        // When
        whatsAppService.handleWebhook(payload);

        // Then
        verifyNoInteractions(identityService, userRepository, taskService, pubSubTemplate);
    }
}
