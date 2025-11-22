package com.fazquepaga.taskandpay.tasks;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class TaskTest {

    @Test
    void shouldBuildTaskWithBuilder() {
        // Given & When
        Task task = Task.builder()
                .id("task-id")
                .description("Test Task")
                .type(Task.TaskType.ONE_TIME)
                .weight(Task.TaskWeight.MEDIUM)
                .status(Task.TaskStatus.PENDING)
                .requiresProof(true)
                .createdAt(Instant.now())
                .build();

        // Then
        assertNotNull(task);
        assertEquals("task-id", task.getId());
        assertEquals("Test Task", task.getDescription());
        assertEquals(Task.TaskType.ONE_TIME, task.getType());
        assertEquals(Task.TaskWeight.MEDIUM, task.getWeight());
        assertEquals(Task.TaskStatus.PENDING, task.getStatus());
        assertTrue(task.isRequiresProof());
        assertNotNull(task.getCreatedAt());
    }

    @Test
    void shouldSupportAllTaskTypes() {
        // Then
        assertNotNull(Task.TaskType.ONE_TIME);
        assertNotNull(Task.TaskType.DAILY);
        assertNotNull(Task.TaskType.WEEKLY);
        assertEquals(3, Task.TaskType.values().length);
    }

    @Test
    void shouldSupportAllTaskWeights() {
        // Then
        assertNotNull(Task.TaskWeight.LOW);
        assertNotNull(Task.TaskWeight.MEDIUM);
        assertNotNull(Task.TaskWeight.HIGH);
        assertEquals(3, Task.TaskWeight.values().length);
    }

    @Test
    void shouldSupportAllTaskStatuses() {
        // Then
        assertNotNull(Task.TaskStatus.PENDING);
        assertNotNull(Task.TaskStatus.COMPLETED);
        assertNotNull(Task.TaskStatus.PENDING_APPROVAL);
        assertNotNull(Task.TaskStatus.APPROVED);
        assertEquals(4, Task.TaskStatus.values().length);
    }

    @Test
    void shouldCreateTaskWithoutProof() {
        // Given & When
        Task task = Task.builder()
                .id("task-id")
                .description("Simple task")
                .requiresProof(false)
                .status(Task.TaskStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        // Then
        assertFalse(task.isRequiresProof());
    }

    @Test
    void shouldCreateDailyTask() {
        // Given & When
        Task task = Task.builder()
                .id("task-id")
                .description("Daily chore")
                .type(Task.TaskType.DAILY)
                .weight(Task.TaskWeight.LOW)
                .status(Task.TaskStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        // Then
        assertEquals(Task.TaskType.DAILY, task.getType());
    }

    @Test
    void shouldCreateHighWeightTask() {
        // Given & When
        Task task = Task.builder()
                .id("task-id")
                .description("Big chore")
                .weight(Task.TaskWeight.HIGH)
                .status(Task.TaskStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        // Then
        assertEquals(Task.TaskWeight.HIGH, task.getWeight());
    }
}
