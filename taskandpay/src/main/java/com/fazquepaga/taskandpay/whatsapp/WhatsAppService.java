package com.fazquepaga.taskandpay.whatsapp;

import com.fazquepaga.taskandpay.identity.IdentityService;
import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.fazquepaga.taskandpay.tasks.Task;
import com.fazquepaga.taskandpay.tasks.TaskService;
import com.fazquepaga.taskandpay.whatsapp.events.ProofSubmittedEvent;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class WhatsAppService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppService.class);

    private final IdentityService identityService;
    private final UserRepository userRepository;
    private final TaskService taskService;
    private final PubSubTemplate pubSubTemplate;
    private final String topicName;

    public WhatsAppService(
            IdentityService identityService,
            UserRepository userRepository,
            TaskService taskService,
            PubSubTemplate pubSubTemplate,
            @Value("${pubsub.topic-name}") String topicName) {
        this.identityService = identityService;
        this.userRepository = userRepository;
        this.taskService = taskService;
        this.pubSubTemplate = pubSubTemplate;
        this.topicName = topicName;
    }

    public void handleWebhook(Map<String, String> webhookPayload) {
        String messageBody = webhookPayload.get("Body");
        String fromNumber = webhookPayload.get("From");
        String imageUrl = webhookPayload.get("MediaUrl0");

        if (fromNumber == null) {
            logger.warn("Received webhook with missing 'From' number");
            return;
        }

        String cleanPhoneNumber = fromNumber.replace("whatsapp:", "");

        // If there is a message body, it might be an onboarding code
        if (messageBody != null && !messageBody.isEmpty()) {
            try {
                identityService.completeOnboarding(messageBody, cleanPhoneNumber);
                logger.info("Successfully completed onboarding for phone number: {}", cleanPhoneNumber);
                return;
            } catch (IllegalArgumentException e) {
                logger.debug("Message '{}' is not a valid onboarding code.", messageBody);
            } catch (ExecutionException | InterruptedException e) {
                logger.error("Error completing onboarding for code '{}'", messageBody, e);
                return;
            }
        }

        // If there is an image, it's a proof of completion
        if (imageUrl != null && !imageUrl.isEmpty()) {
            handleImageMessage(cleanPhoneNumber, imageUrl);
        } else {
            logger.info("Received a message from {} with no image and it was not a valid onboarding code.", cleanPhoneNumber);
        }
    }

    private void handleImageMessage(String phoneNumber, String imageUrl) {
        try {
            User child = userRepository.findByPhoneNumber(phoneNumber);
            if (child == null) {
                logger.warn("Received image from unknown phone number: {}", phoneNumber);
                return;
            }

            List<Task> tasks = taskService.getTasksByUserId(child.getId());
            Optional<Task> taskToComplete = tasks.stream()
                    .filter(t -> t.getStatus() == Task.TaskStatus.PENDING && t.isRequiresProof())
                    .findFirst();

            if (taskToComplete.isPresent()) {
                Task task = taskToComplete.get();
                logger.info("Found pending task '{}' for child '{}'. Publishing proof event.", task.getDescription(), child.getName());

                ProofSubmittedEvent event = new ProofSubmittedEvent(child.getId(), task.getId(), imageUrl);
                CompletableFuture<String> future = pubSubTemplate.publish(topicName, event);

                future.whenComplete((result, ex) -> {
                    if (ex != null) {
                        logger.error("Failed to publish message", ex);
                    } else {
                        logger.info("Successfully published message with ID: {}", result);
                    }
                });

            } else {
                logger.info("Received image from child '{}' but no pending tasks requiring proof were found.", child.getName());
            }

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error processing image message from phone number '{}'", phoneNumber, e);
        }
    }
}
