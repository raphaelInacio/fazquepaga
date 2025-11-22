package com.fazquepaga.taskandpay.ai;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazquepaga.taskandpay.tasks.Task;
import com.fazquepaga.taskandpay.tasks.TaskRepository;
import com.fazquepaga.taskandpay.whatsapp.events.ProofSubmittedEvent;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;

class TaskProofListenerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AiValidator aiValidator;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private Message<?> message;

    @Mock
    private BasicAcknowledgeablePubsubMessage pubsubMessage;

    @Mock
    private DocumentSnapshot documentSnapshot;

    private TaskProofListener taskProofListener;

    private MessageHandler messageHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        taskProofListener = new TaskProofListener(objectMapper, aiValidator, taskRepository);
        messageHandler = taskProofListener.messageReceiver();
    }

    @Test
    void shouldProcessValidProofSubmittedEvent() throws Exception {
        // Given
        String childId = "child-123";
        String taskId = "task-456";
        String imageUrl = "https://example.com/image.jpg";

        ProofSubmittedEvent event = new ProofSubmittedEvent(childId, taskId, imageUrl);
        String payload = "{\"childId\":\"" + childId + "\",\"taskId\":\"" + taskId + "\",\"imageUrl\":\"" + imageUrl
                + "\"}";

        Task task = Task.builder()
                .id(taskId)
                .description("Clean room")
                .status(Task.TaskStatus.PENDING)
                .requiresProof(true)
                .createdAt(Instant.now())
                .build();

        Map<String, Object> headers = new HashMap<>();
        headers.put(GcpPubSubHeaders.ORIGINAL_MESSAGE, pubsubMessage);

        doReturn(payload.getBytes()).when(message).getPayload();
        when(message.getHeaders()).thenReturn(new MessageHeaders(headers));
        when(objectMapper.readValue(payload, ProofSubmittedEvent.class)).thenReturn(event);
        when(taskRepository.findById(childId, taskId)).thenReturn(ApiFutures.immediateFuture(documentSnapshot));
        when(documentSnapshot.toObject(Task.class)).thenReturn(task);
        when(aiValidator.validateTaskCompletionImage(any(byte[].class), eq("Clean room"))).thenReturn(true);
        when(taskRepository.save(eq(childId), any(Task.class))).thenReturn(ApiFutures.immediateFuture(null));

        // When
        messageHandler.handleMessage(message);

        // Then
        verify(objectMapper).readValue(payload, ProofSubmittedEvent.class);
        verify(taskRepository).findById(childId, taskId);
        verify(aiValidator).validateTaskCompletionImage(any(byte[].class), eq("Clean room"));
        verify(taskRepository).save(eq(childId), any(Task.class));
        verify(pubsubMessage).ack();
    }

    @Test
    void shouldAckMessageEvenWhenTaskNotFound() throws Exception {
        // Given
        String childId = "child-123";
        String taskId = "non-existent";
        String payload = "{\"childId\":\"" + childId + "\",\"taskId\":\"" + taskId + "\",\"imageUrl\":\"url\"}";
        ProofSubmittedEvent event = new ProofSubmittedEvent(childId, taskId, "url");

        Map<String, Object> headers = new HashMap<>();
        headers.put(GcpPubSubHeaders.ORIGINAL_MESSAGE, pubsubMessage);

        doReturn(payload.getBytes()).when(message).getPayload();
        when(message.getHeaders()).thenReturn(new MessageHeaders(headers));
        when(objectMapper.readValue(payload, ProofSubmittedEvent.class)).thenReturn(event);
        when(taskRepository.findById(childId, taskId)).thenReturn(ApiFutures.immediateFuture(documentSnapshot));
        when(documentSnapshot.toObject(Task.class)).thenReturn(null);

        // When
        messageHandler.handleMessage(message);

        // Then
        verify(taskRepository).findById(childId, taskId);
        verify(aiValidator, never()).validateTaskCompletionImage(any(), any());
        verify(pubsubMessage).ack();
    }

    @Test
    void shouldRejectProofWhenAiValidationFails() throws Exception {
        // Given
        String childId = "child-123";
        String taskId = "task-456";
        String imageUrl = "https://example.com/image.jpg";

        ProofSubmittedEvent event = new ProofSubmittedEvent(childId, taskId, imageUrl);
        String payload = "{\"childId\":\"" + childId + "\",\"taskId\":\"" + taskId + "\",\"imageUrl\":\"" + imageUrl
                + "\"}";

        Task task = Task.builder()
                .id(taskId)
                .description("Do homework")
                .status(Task.TaskStatus.PENDING)
                .requiresProof(true)
                .createdAt(Instant.now())
                .build();

        Map<String, Object> headers = new HashMap<>();
        headers.put(GcpPubSubHeaders.ORIGINAL_MESSAGE, pubsubMessage);

        doReturn(payload.getBytes()).when(message).getPayload();
        when(message.getHeaders()).thenReturn(new MessageHeaders(headers));
        when(objectMapper.readValue(payload, ProofSubmittedEvent.class)).thenReturn(event);
        when(taskRepository.findById(childId, taskId)).thenReturn(ApiFutures.immediateFuture(documentSnapshot));
        when(documentSnapshot.toObject(Task.class)).thenReturn(task);
        when(aiValidator.validateTaskCompletionImage(any(byte[].class), eq("Do homework"))).thenReturn(false);
        when(taskRepository.save(eq(childId), any(Task.class))).thenReturn(ApiFutures.immediateFuture(null));

        // When
        messageHandler.handleMessage(message);

        // Then
        verify(aiValidator).validateTaskCompletionImage(any(byte[].class), eq("Do homework"));
        verify(taskRepository).save(eq(childId), any(Task.class));
        verify(pubsubMessage).ack();
    }

    @Test
    void shouldHandleJsonParsingError() throws Exception {
        // Given
        String invalidPayload = "invalid json";

        Map<String, Object> headers = new HashMap<>();
        headers.put(GcpPubSubHeaders.ORIGINAL_MESSAGE, pubsubMessage);

        doReturn(invalidPayload.getBytes()).when(message).getPayload();
        when(message.getHeaders()).thenReturn(new MessageHeaders(headers));
        when(objectMapper.readValue(invalidPayload, ProofSubmittedEvent.class))
                .thenThrow(new com.fasterxml.jackson.core.JsonParseException(null, "Invalid JSON"));

        // When
        messageHandler.handleMessage(message);

        // Then
        verify(objectMapper).readValue(invalidPayload, ProofSubmittedEvent.class);
        verify(taskRepository, never()).findById(any(), any());
        verify(pubsubMessage).ack();
    }
}
