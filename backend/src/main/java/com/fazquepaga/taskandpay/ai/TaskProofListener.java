package com.fazquepaga.taskandpay.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazquepaga.taskandpay.tasks.Task;
import com.fazquepaga.taskandpay.tasks.TaskRepository;
import com.fazquepaga.taskandpay.whatsapp.events.ProofSubmittedEvent;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

@Component
public class TaskProofListener {

    private static final Logger logger = LoggerFactory.getLogger(TaskProofListener.class);

    private final ObjectMapper objectMapper;
    private final AiValidator aiValidator;
    private final TaskRepository taskRepository;

    public TaskProofListener(
            ObjectMapper objectMapper, AiValidator aiValidator, TaskRepository taskRepository) {
        this.objectMapper = objectMapper;
        this.aiValidator = aiValidator;
        this.taskRepository = taskRepository;
    }

    @Bean
    @ServiceActivator(inputChannel = "proofsChannel")
    public MessageHandler messageReceiver() {
        return message -> {
            String payload = new String((byte[]) message.getPayload());
            logger.info("Message received: " + payload);
            try {
                ProofSubmittedEvent event =
                        objectMapper.readValue(payload, ProofSubmittedEvent.class);
                handleEvent(event);
            } catch (IOException | ExecutionException | InterruptedException e) {
                logger.error("Error processing message: " + payload, e);
            }
            BasicAcknowledgeablePubsubMessage originalMessage =
                    message.getHeaders()
                            .get(
                                    GcpPubSubHeaders.ORIGINAL_MESSAGE,
                                    BasicAcknowledgeablePubsubMessage.class);
            if (originalMessage != null) {
                originalMessage.ack();
            }
        };
    }

    private void handleEvent(ProofSubmittedEvent event)
            throws ExecutionException, InterruptedException {
        // This is a simplified implementation. In a real application, you would download the image
        // from the URL and pass the bytes to the validator.
        Task task =
                taskRepository
                        .findById(event.getChildId(), event.getTaskId())
                        .get()
                        .toObject(Task.class);
        if (task != null) {
            boolean isValid =
                    aiValidator.validateTaskCompletionImage(new byte[0], task.getDescription());
            task.setAiValidated(isValid);
            task.setStatus(Task.TaskStatus.PENDING_APPROVAL);
            taskRepository.save(event.getChildId(), task);
            logger.info(
                    "Task '{}' for child '{}' has been AI validated with result: {}",
                    task.getDescription(),
                    event.getChildId(),
                    isValid);
        } else {
            logger.warn(
                    "Task with ID '{}' not found for child '{}'",
                    event.getTaskId(),
                    event.getChildId());
        }
    }
}
