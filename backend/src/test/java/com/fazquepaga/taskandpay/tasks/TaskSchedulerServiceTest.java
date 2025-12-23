package com.fazquepaga.taskandpay.tasks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.GenericMessage;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskSchedulerServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ApiFuture<QuerySnapshot> queryFuture;

    @Mock
    private QuerySnapshot querySnapshot;

    @Mock
    private QueryDocumentSnapshot documentSnapshot;

    @Mock
    private DocumentReference docRef;

    @Mock
    private CollectionReference colRef;

    @Mock
    private DocumentReference userRef;

    private TaskSchedulerService taskSchedulerService;

    @BeforeEach
    void setUp() {
        taskSchedulerService = new TaskSchedulerService(taskRepository, objectMapper);
    }

    @Test
    void resetRecurringTasks_shouldResetDailyTasks() throws ExecutionException, InterruptedException {
        // Arrange
        Task dailyTask = new Task();
        dailyTask.setId("task1");
        dailyTask.setType(Task.TaskType.DAILY);
        dailyTask.setStatus(Task.TaskStatus.COMPLETED);

        when(taskRepository.findRecurringTasks("DAILY")).thenReturn(queryFuture);
        when(taskRepository.findRecurringTasks("WEEKLY")).thenReturn(queryFuture); // Return empty for weekly
        when(queryFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(
                List.of(documentSnapshot), // Returns list for daily
                Collections.emptyList() // Returns empty for weekly
        );

        when(documentSnapshot.toObject(Task.class)).thenReturn(dailyTask);

        // Mocking the path users/{userId}/tasks/{taskId}
        when(documentSnapshot.getReference()).thenReturn(docRef);
        when(docRef.getParent()).thenReturn(colRef);
        when(colRef.getParent()).thenReturn(userRef);
        when(userRef.getId()).thenReturn("user-123");

        // Act
        taskSchedulerService.resetRecurringTasks();

        // Assert
        verify(taskRepository).save(eq("user-123"), any(Task.class));
    }

    @Test
    void resetRecurringTasks_shouldResetWeeklyTasks_whenDayMatches() throws ExecutionException, InterruptedException {
        // Arrange
        int today = LocalDate.now().getDayOfWeek().getValue();

        Task weeklyTask = new Task();
        weeklyTask.setId("task2");
        weeklyTask.setType(Task.TaskType.WEEKLY);
        weeklyTask.setStatus(Task.TaskStatus.APPROVED);
        weeklyTask.setDayOfWeek(today);

        // Daily returns empty
        when(taskRepository.findRecurringTasks("DAILY")).thenReturn(queryFuture);
        when(queryFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(Collections.emptyList());

        // Weekly returns 1 task
        ApiFuture<QuerySnapshot> weeklyFuture = mock(ApiFuture.class);
        QuerySnapshot weeklySnapshot = mock(QuerySnapshot.class);
        when(taskRepository.findRecurringTasks("WEEKLY")).thenReturn(weeklyFuture);
        when(weeklyFuture.get()).thenReturn(weeklySnapshot);
        when(weeklySnapshot.getDocuments()).thenReturn(List.of(documentSnapshot));

        when(documentSnapshot.toObject(Task.class)).thenReturn(weeklyTask);

        when(documentSnapshot.getReference()).thenReturn(docRef);
        when(docRef.getParent()).thenReturn(colRef);
        when(colRef.getParent()).thenReturn(userRef);
        when(userRef.getId()).thenReturn("user-456");

        // Act
        taskSchedulerService.resetRecurringTasks();

        // Assert
        verify(taskRepository).save(eq("user-456"), argThat(task -> task.getStatus() == Task.TaskStatus.PENDING &&
                !task.getAcknowledged()));
    }

    @Test
    void messageReceiver_shouldCallResetRecurringTasks_whenActionIsResetTasks() throws Exception {
        // Arrange
        String payload = "{\"action\": \"RESET_TASKS\"}";
        Message<byte[]> message = new GenericMessage<>(payload.getBytes());

        JsonNode jsonNode = mock(JsonNode.class);
        when(objectMapper.readTree(payload)).thenReturn(jsonNode);
        when(jsonNode.has("action")).thenReturn(true);
        when(jsonNode.get("action")).thenReturn(jsonNode);
        when(jsonNode.asText()).thenReturn("RESET_TASKS");

        // Mock internal reset call by mocking what resetRecurringTasks does,
        // OR we just assume resetRecurringTasks logic runs.
        // Since we are testing the service itself (not a spy), calling the handler will
        // execute resetRecurringTasks.
        // We can verify taskRepository interaction which happens inside
        // resetRecurringTasks.

        // Mock empty return for daily tasks to avoid NPE/complex setup for this
        // specific test
        when(taskRepository.findRecurringTasks("DAILY")).thenReturn(queryFuture);
        when(queryFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(Collections.emptyList());
        when(taskRepository.findRecurringTasks("WEEKLY")).thenReturn(queryFuture);

        // Act
        MessageHandler handler = taskSchedulerService.taskResetMessageReceiver();
        handler.handleMessage(message);

        // Assert
        verify(taskRepository, times(1)).findRecurringTasks("DAILY");
    }
}
