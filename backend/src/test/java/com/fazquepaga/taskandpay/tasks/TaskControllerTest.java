package com.fazquepaga.taskandpay.tasks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazquepaga.taskandpay.config.SecurityConfig;
import com.fazquepaga.taskandpay.tasks.dto.CreateTaskRequest;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(controllers = TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private TaskService taskService;
        @MockBean
        private com.fazquepaga.taskandpay.identity.UserRepository userRepository;
        @MockBean
        private com.fazquepaga.taskandpay.security.JwtService jwtService;

        @Test
        void shouldCreateTaskSuccessfully() throws Exception {
                // Given
                String childId = "child-id";
                CreateTaskRequest request = new CreateTaskRequest();
                request.setDescription("Clean your room");
                request.setType(Task.TaskType.ONE_TIME);
                request.setWeight(Task.TaskWeight.MEDIUM);
                request.setRequiresProof(false);

                Task createdTask = Task.builder()
                                .id("task-id")
                                .description("Clean your room")
                                .type(Task.TaskType.ONE_TIME)
                                .weight(Task.TaskWeight.MEDIUM)
                                .status(Task.TaskStatus.PENDING)
                                .requiresProof(false)
                                .createdAt(Instant.now())
                                .build();

                when(taskService.createTask(eq(childId), any(CreateTaskRequest.class)))
                                .thenReturn(createdTask);

                // When & Then
                mockMvc.perform(
                                post("/api/v1/tasks")
                                                .param("child_id", childId)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value("task-id"))
                                .andExpect(jsonPath("$.description").value("Clean your room"))
                                .andExpect(jsonPath("$.type").value("ONE_TIME"))
                                .andExpect(jsonPath("$.weight").value("MEDIUM"))
                                .andExpect(jsonPath("$.status").value("PENDING"))
                                .andExpect(jsonPath("$.requiresProof").value(false));
        }

        @Test
        void shouldCreateTaskWithProofRequired() throws Exception {
                // Given
                String childId = "child-id";
                CreateTaskRequest request = new CreateTaskRequest();
                request.setDescription("Mow the lawn");
                request.setType(Task.TaskType.ONE_TIME);
                request.setWeight(Task.TaskWeight.HIGH);
                request.setRequiresProof(true);

                Task createdTask = Task.builder()
                                .id("task-id-2")
                                .description("Mow the lawn")
                                .type(Task.TaskType.ONE_TIME)
                                .weight(Task.TaskWeight.HIGH)
                                .status(Task.TaskStatus.PENDING)
                                .requiresProof(true)
                                .createdAt(Instant.now())
                                .build();

                when(taskService.createTask(eq(childId), any(CreateTaskRequest.class)))
                                .thenReturn(createdTask);

                // When & Then
                mockMvc.perform(
                                post("/api/v1/tasks")
                                                .param("child_id", childId)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.requiresProof").value(true))
                                .andExpect(jsonPath("$.weight").value("HIGH"));
        }

        @Test
        void shouldGetTasksForChild() throws Exception {
                // Given
                String childId = "child-id";
                List<Task> tasks = Arrays.asList(
                                Task.builder()
                                                .id("task-1")
                                                .description("Task 1")
                                                .status(Task.TaskStatus.PENDING)
                                                .createdAt(Instant.now())
                                                .build(),
                                Task.builder()
                                                .id("task-2")
                                                .description("Task 2")
                                                .status(Task.TaskStatus.COMPLETED)
                                                .createdAt(Instant.now())
                                                .build());

                when(taskService.getTasksByUserId(childId)).thenReturn(tasks);

                // When & Then
                mockMvc.perform(get("/api/v1/tasks").param("child_id", childId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$.length()").value(2))
                                .andExpect(jsonPath("$[0].id").value("task-1"))
                                .andExpect(jsonPath("$[0].description").value("Task 1"))
                                .andExpect(jsonPath("$[0].status").value("PENDING"))
                                .andExpect(jsonPath("$[1].id").value("task-2"))
                                .andExpect(jsonPath("$[1].status").value("COMPLETED"));
        }

        @Test
        void shouldReturnEmptyListWhenNoTasks() throws Exception {
                // Given
                String childId = "child-with-no-tasks";
                when(taskService.getTasksByUserId(childId)).thenReturn(List.of());

                // When & Then
                mockMvc.perform(get("/api/v1/tasks").param("child_id", childId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        void shouldReturnBadRequestWhenChildNotFound() throws Exception {
                // Given
                String childId = "non-existent-child";
                CreateTaskRequest request = new CreateTaskRequest();
                request.setDescription("Test task");

                when(taskService.createTask(eq(childId), any(CreateTaskRequest.class)))
                                .thenThrow(new IllegalArgumentException("Child not found"));

                // When & Then
                mockMvc.perform(
                                post("/api/v1/tasks")
                                                .param("child_id", childId)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Child not found"));
        }

        @Test
        void shouldReturnBadRequestWhenParentIsNotAuthorizedToApproveTask() throws Exception {
                // Given
                String taskId = "task-id";
                String childId = "child-id";
                String unauthorizedParentId = "unauthorized@example.com";

                when(taskService.approveTask(childId, taskId, unauthorizedParentId))
                                .thenThrow(
                                                new IllegalArgumentException(
                                                                "Child not found or does not belong to this parent"));

                // When & Then
                mockMvc.perform(
                                post("/api/v1/tasks/{taskId}/approve", taskId)
                                                .param("child_id", childId)
                                                .param("parent_id", unauthorizedParentId))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldApproveTaskSuccessfully() throws Exception {
                // Given
                String taskId = "task-id";
                String childId = "child-id";
                String parentId = "parent@example.com";

                Task approvedTask = Task.builder()
                                .id(taskId)
                                .description("Clean your room")
                                .status(Task.TaskStatus.APPROVED)
                                .build();

                when(taskService.approveTask(childId, taskId, parentId)).thenReturn(approvedTask);

                // When & Then
                mockMvc.perform(
                                post("/api/v1/tasks/{taskId}/approve", taskId)
                                                .param("child_id", childId)
                                                .param("parent_id", parentId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(taskId))
                                .andExpect(jsonPath("$.status").value("APPROVED"));
        }

        @Test
        void shouldDeleteTaskSuccessfully() throws Exception {
                // Given
                String taskId = "task-id";
                String childId = "child-id";
                String parentId = "parent@example.com";

                // When & Then
                mockMvc.perform(
                                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                                .delete("/api/v1/tasks/{taskId}", taskId)
                                                .param("child_id", childId)
                                                .param("parent_id", parentId))
                                .andExpect(status().isNoContent());

                org.mockito.Mockito.verify(taskService).deleteTask(childId, taskId, parentId);
        }
}
