package com.fazquepaga.taskandpay.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TaskSchedulerService {

    private final TaskRepository taskRepository;
    private final ObjectMapper objectMapper;

    public TaskSchedulerService(TaskRepository taskRepository, ObjectMapper objectMapper) {
        this.taskRepository = taskRepository;
        this.objectMapper = objectMapper;
    }

    @Bean
    @ServiceActivator(inputChannel = "taskResetChannel")
    public MessageHandler taskResetMessageReceiver() {
        return message -> {
            String payload = new String((byte[]) message.getPayload());
            log.info("Processing task reset message: {}", payload);
            try {
                JsonNode json = objectMapper.readTree(payload);
                if (json.has("action") && "RESET_TASKS".equals(json.get("action").asText())) {
                    resetRecurringTasks();
                } else {
                    log.error("Invalid action in task reset message: {}", payload);
                }
            } catch (Exception e) {
                log.error(
                        "Error processing task reset message: {} - {}", payload, e.getMessage());
            }

            BasicAcknowledgeablePubsubMessage originalMessage = message.getHeaders()
                    .get(
                            GcpPubSubHeaders.ORIGINAL_MESSAGE,
                            BasicAcknowledgeablePubsubMessage.class);
            if (originalMessage != null) {
                originalMessage.ack();
            }
        };
    }

    public void resetRecurringTasks() throws ExecutionException, InterruptedException {
        log.info("Starting daily recurring task reset...");

        resetDailyTasks();
        resetWeeklyTasks();

        log.info("Finished daily recurring task reset.");
    }

    private void resetDailyTasks() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> dailyTasks = taskRepository.findRecurringTasks("DAILY").get().getDocuments();

        for (QueryDocumentSnapshot doc : dailyTasks) {
            processTaskReset(doc);
        }
    }

    private void resetWeeklyTasks() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> weeklyTasks = taskRepository.findRecurringTasks("WEEKLY").get().getDocuments();

        // 1 (Mon) to 7 (Sun)
        int currentDayOfWeek = LocalDate.now().getDayOfWeek().getValue();

        for (QueryDocumentSnapshot doc : weeklyTasks) {
            Task task = doc.toObject(Task.class);
            // Only reset if it is intended for today
            if (task.getDayOfWeek() != null && task.getDayOfWeek() == currentDayOfWeek) {
                processTaskReset(doc);
            }
        }
    }

    private void processTaskReset(QueryDocumentSnapshot doc) {
        try {
            Task task = doc.toObject(Task.class);

            // Only reset if status is not PENDING (i.e., it was done/approved previously)
            // Actually, we should ALWAYS evaluate if we need to reset to PENDING for the
            // new day.
            // If it was already PENDING from yesterday, it remains PENDING (child didn't do
            // it).
            // But for a NEW day, it is technically a "new" instance of the task.
            // However, sticking to the requirement: if it was completed yesterday, it needs
            // to be
            // PENDING today.
            if (task.getStatus() != Task.TaskStatus.PENDING) {
                // Determine userId from parent path: users/{userId}/tasks/{taskId}
                // Parent is "tasks", Parent of Parent is "{userId}"
                String userId = doc.getReference().getParent().getParent().getId();

                task.setStatus(Task.TaskStatus.PENDING);
                task.setAcknowledged(false);
                task.setAiValidated(false); // Reset AI validation if any

                taskRepository.save(userId, task);
                log.info("Reset task {} for user {}", task.getId(), userId);
            }
        } catch (Exception e) {
            log.error("Failed to reset task {}: {}", doc.getId(), e.getMessage());
        }
    }
}
