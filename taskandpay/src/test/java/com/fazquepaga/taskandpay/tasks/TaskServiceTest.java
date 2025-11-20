package com.fazquepaga.taskandpay.tasks;

import com.fazquepaga.taskandpay.identity.User;

import com.fazquepaga.taskandpay.identity.UserRepository;

import com.fazquepaga.taskandpay.tasks.dto.CreateTaskRequest;

import com.google.api.core.ApiFutures;

import com.google.cloud.firestore.QueryDocumentSnapshot;

import com.google.cloud.firestore.QuerySnapshot;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;

import org.mockito.Mock;

import org.mockito.Mockito;

import org.mockito.MockitoAnnotations;



import java.time.Instant;

import java.util.Collections;

import java.util.List;

import java.util.concurrent.ExecutionException;



import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.when;



class TaskServiceTest {



    @Mock

    private TaskRepository taskRepository;



    @Mock

    private UserRepository userRepository;



    @InjectMocks

    private TaskService taskService;



    @BeforeEach

    void setUp() {

        MockitoAnnotations.openMocks(this);

    }



    @Test

    void shouldCreateTask() throws ExecutionException, InterruptedException {

        // Given

        String userId = "user-id";

        CreateTaskRequest request = new CreateTaskRequest();

        request.setDescription("Test Task");

        request.setType(Task.TaskType.ONE_TIME);

        request.setWeight(Task.TaskWeight.MEDIUM);

        request.setRequiresProof(false);



        User child = User.builder().id(userId).role(User.Role.CHILD).build();



        when(userRepository.findByIdSync(userId)).thenReturn(child);

        when(taskRepository.save(eq(userId), any(Task.class))).thenReturn(ApiFutures.immediateFuture(null));



        // When

        Task result = taskService.createTask(userId, request);



        // Then

        assertNotNull(result);

        assertEquals(Task.TaskStatus.PENDING, result.getStatus());

        assertNotNull(result.getCreatedAt());

        assertEquals("Test Task", result.getDescription());

    }



    @Test

    void shouldGetTasksByUserId() throws ExecutionException, InterruptedException {

        // Given

        String userId = "user-id";

        Task task = Task.builder()

                .id("task-id")

                .description("My Task")

                .status(Task.TaskStatus.PENDING)

                .createdAt(Instant.now())

                .build();



        // Mock Firestore response

        QuerySnapshot querySnapshot = Mockito.mock(QuerySnapshot.class);

        QueryDocumentSnapshot documentSnapshot = Mockito.mock(QueryDocumentSnapshot.class);



        when(taskRepository.findTasksByUserId(userId)).thenReturn(ApiFutures.immediateFuture(querySnapshot));

        when(querySnapshot.getDocuments()).thenReturn(Collections.singletonList(documentSnapshot));

        when(documentSnapshot.toObject(Task.class)).thenReturn(task);



        // When

        List<Task> result = taskService.getTasksByUserId(userId);



        // Then

        assertNotNull(result);

        assertEquals(1, result.size());

        assertEquals("task-id", result.get(0).getId());

        assertEquals("My Task", result.get(0).getDescription());

    }



    @Test

    void shouldReturnEmptyListWhenNoTasksFound() throws ExecutionException, InterruptedException {

        // Given

        String userId = "user-with-no-tasks";

        QuerySnapshot querySnapshot = Mockito.mock(QuerySnapshot.class);



        when(taskRepository.findTasksByUserId(userId)).thenReturn(ApiFutures.immediateFuture(querySnapshot));

        when(querySnapshot.getDocuments()).thenReturn(Collections.emptyList());



        // When

        List<Task> result = taskService.getTasksByUserId(userId);



        // Then

        assertNotNull(result);

        assertTrue(result.isEmpty());

    }

}
