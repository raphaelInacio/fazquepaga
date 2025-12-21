package com.fazquepaga.taskandpay.tasks;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.fazquepaga.taskandpay.subscription.SubscriptionLimitReachedException;
import com.fazquepaga.taskandpay.tasks.dto.CreateTaskRequest;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class TaskServiceTest {

        @Mock
        private TaskRepository taskRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private com.fazquepaga.taskandpay.subscription.SubscriptionService subscriptionService;

        @Mock
        private com.fazquepaga.taskandpay.allowance.LedgerService ledgerService;

        @Mock
        private jakarta.inject.Provider<com.fazquepaga.taskandpay.allowance.AllowanceService> allowanceServiceProvider;

        @Mock
        private com.fazquepaga.taskandpay.allowance.AllowanceService allowanceService;

        @Mock
        private com.fazquepaga.taskandpay.notification.NotificationService notificationService;

        @InjectMocks
        private TaskService taskService;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                when(allowanceServiceProvider.get()).thenReturn(allowanceService);
        }

        @Test
        void shouldCreateTask() throws ExecutionException, InterruptedException {

                // Given
                String userId = "user-id";
                String parentId = "parent-id";

                CreateTaskRequest request = new CreateTaskRequest();
                request.setDescription("Test Task");
                request.setType(Task.TaskType.ONE_TIME);
                request.setWeight(Task.TaskWeight.MEDIUM);
                request.setRequiresProof(false);

                User child = User.builder().id(userId).role(User.Role.CHILD).parentId(parentId).build();

                User parent = User.builder()
                                .id(parentId)
                                .role(User.Role.PARENT)
                                .subscriptionTier(User.SubscriptionTier.FREE)
                                .build();

                when(userRepository.findByIdSync(userId)).thenReturn(child);
                when(userRepository.findByIdSync(parentId)).thenReturn(parent);

                doAnswer(
                                invocation -> {
                                        Task taskToSave = invocation.getArgument(1);
                                        taskToSave.setId("new-task-id");
                                        return ApiFutures.immediateFuture(null);
                                })
                                .when(taskRepository)
                                .save(eq(userId), any(Task.class));

                // Mock the behavior of getTasksByUserId to return the newly created task
                QuerySnapshot querySnapshot = Mockito.mock(QuerySnapshot.class);
                QueryDocumentSnapshot documentSnapshot = Mockito.mock(QueryDocumentSnapshot.class);
                when(taskRepository.findTasksByUserId(userId))
                                .thenReturn(ApiFutures.immediateFuture(querySnapshot));
                when(querySnapshot.getDocuments()).thenReturn(Collections.singletonList(documentSnapshot));

                Task createdTask = Task.builder()
                                .id("new-task-id")
                                .description("Test Task")
                                .type(Task.TaskType.ONE_TIME)
                                .weight(Task.TaskWeight.MEDIUM)
                                .requiresProof(false)
                                .status(Task.TaskStatus.PENDING)
                                .createdAt(Instant.now())
                                .build();
                when(documentSnapshot.toObject(Task.class)).thenReturn(createdTask);

                // When
                Task result = taskService.createTask(userId, request);

                // Then
                assertNotNull(result);
                assertEquals(Task.TaskStatus.PENDING, result.getStatus());
                assertNotNull(result.getCreatedAt());
                assertEquals("Test Task", result.getDescription());
                assertEquals("new-task-id", result.getId());
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

                when(taskRepository.findTasksByUserId(userId))
                                .thenReturn(ApiFutures.immediateFuture(querySnapshot));

                when(querySnapshot.getDocuments()).thenReturn(Collections.singletonList(documentSnapshot));

                when(documentSnapshot.toObject(Task.class)).thenReturn(task);

                // When

                List<Task> result = taskService.getTasksByUserId(userId);

                // Then

                assertNotNull(result);

                assertEquals(1, result.size());

                assertEquals("task-id", result.get(0).getId());

                assertEquals("My Task", result.get(0).getDescription());
                assertEquals("My Task", result.get(0).getDescription());
        }

        @Test
        void shouldGetTasksByUserIdFilteringArchived() throws ExecutionException, InterruptedException {
                // Given
                String userId = "user-id";
                Task activeTask = Task.builder().id("task-1").archived(false).build();
                Task archivedTask = Task.builder().id("task-2").archived(true).build();
                Task nullArchivedTask = Task.builder().id("task-3").archived(null).build();

                QuerySnapshot querySnapshot = Mockito.mock(QuerySnapshot.class);
                QueryDocumentSnapshot doc1 = Mockito.mock(QueryDocumentSnapshot.class);
                QueryDocumentSnapshot doc2 = Mockito.mock(QueryDocumentSnapshot.class);
                QueryDocumentSnapshot doc3 = Mockito.mock(QueryDocumentSnapshot.class);

                when(taskRepository.findTasksByUserId(userId)).thenReturn(ApiFutures.immediateFuture(querySnapshot));
                when(querySnapshot.getDocuments()).thenReturn(List.of(doc1, doc2, doc3));

                when(doc1.toObject(Task.class)).thenReturn(activeTask);
                when(doc2.toObject(Task.class)).thenReturn(archivedTask);
                when(doc3.toObject(Task.class)).thenReturn(nullArchivedTask);

                // When
                List<Task> result = taskService.getTasksByUserId(userId);

                // Then
                assertEquals(2, result.size());
                assertTrue(result.contains(activeTask));
                assertTrue(result.contains(nullArchivedTask));
                assertFalse(result.contains(archivedTask));
        }

        @Test
        void shouldReturnEmptyListWhenNoTasksFound() throws ExecutionException, InterruptedException {

                // Given

                String userId = "user-with-no-tasks";

                QuerySnapshot querySnapshot = Mockito.mock(QuerySnapshot.class);

                when(taskRepository.findTasksByUserId(userId))
                                .thenReturn(ApiFutures.immediateFuture(querySnapshot));

                when(querySnapshot.getDocuments()).thenReturn(Collections.emptyList());

                // When

                List<Task> result = taskService.getTasksByUserId(userId);

                // Then

                assertNotNull(result);

                assertTrue(result.isEmpty());
        }

        @Test
        void shouldApproveTask() throws ExecutionException, InterruptedException {
                // Given
                String childId = "child-id";
                String taskId = "task-id";
                String parentId = "parent-id";

                User parent = User.builder().id(parentId).role(User.Role.PARENT).build();
                User child = User.builder().id(childId).parentId(parentId).build();
                Task task = Task.builder().id(taskId).status(Task.TaskStatus.PENDING_APPROVAL).build();

                when(userRepository.findByIdSync(parentId)).thenReturn(parent);
                when(userRepository.findByIdSync(childId)).thenReturn(child);

                QuerySnapshot querySnapshot = Mockito.mock(QuerySnapshot.class);
                QueryDocumentSnapshot documentSnapshot = Mockito.mock(QueryDocumentSnapshot.class);
                when(taskRepository.findTasksByUserId(childId))
                                .thenReturn(ApiFutures.immediateFuture(querySnapshot));
                when(querySnapshot.getDocuments()).thenReturn(Collections.singletonList(documentSnapshot));
                when(documentSnapshot.toObject(Task.class)).thenReturn(task);

                when(allowanceServiceProvider.get().calculateValueForTask(childId, taskId))
                                .thenReturn(new BigDecimal("10.00"));
                when(taskRepository.save(childId, task)).thenReturn(ApiFutures.immediateFuture(null));

                // When
                Task result = taskService.approveTask(childId, taskId, parentId);

                // Then
                assertEquals(Task.TaskStatus.APPROVED, result.getStatus());
                assertTrue(result.getAcknowledged());
                verify(taskRepository).save(childId, task);
                verify(ledgerService)
                                .addTransaction(eq(childId), any(BigDecimal.class), anyString(), eq(
                                                com.fazquepaga.taskandpay.allowance.Transaction.TransactionType.CREDIT));
        }

        @Test
        void shouldAcknowledgeTask() throws ExecutionException, InterruptedException {
                // Given
                String childId = "child-id";
                String taskId = "task-id";
                String parentId = "parent-id";

                User parent = User.builder().id(parentId).role(User.Role.PARENT).build();
                User child = User.builder().id(childId).parentId(parentId).build();
                Task task = Task.builder().id(taskId).status(Task.TaskStatus.APPROVED).acknowledged(false).build();

                when(userRepository.findByIdSync(parentId)).thenReturn(parent);

                // Mock getTasksByUserId
                QuerySnapshot querySnapshot = Mockito.mock(QuerySnapshot.class);
                QueryDocumentSnapshot documentSnapshot = Mockito.mock(QueryDocumentSnapshot.class);
                when(taskRepository.findTasksByUserId(childId)).thenReturn(ApiFutures.immediateFuture(querySnapshot));
                when(querySnapshot.getDocuments()).thenReturn(Collections.singletonList(documentSnapshot));
                when(documentSnapshot.toObject(Task.class)).thenReturn(task);

                when(taskRepository.save(childId, task)).thenReturn(ApiFutures.immediateFuture(null));

                // When
                Task result = taskService.acknowledgeTask(childId, taskId, parentId);

                // Then
                assertTrue(result.getAcknowledged());
                verify(taskRepository).save(childId, task);
        }

        @Test
        void shouldRejectTask() throws ExecutionException, InterruptedException {
                // Given
                String childId = "child-id";
                String taskId = "task-id";
                String parentId = "parent-id";

                User parent = User.builder().id(parentId).role(User.Role.PARENT).build();
                User child = User.builder().id(childId).parentId(parentId).build();
                Task task = Task.builder()
                                .id(taskId)
                                .status(Task.TaskStatus.APPROVED)
                                .description("Task Desc")
                                .build();

                when(userRepository.findByIdSync(parentId)).thenReturn(parent);

                // Mock getTasksByUserId
                QuerySnapshot querySnapshot = Mockito.mock(QuerySnapshot.class);
                QueryDocumentSnapshot documentSnapshot = Mockito.mock(QueryDocumentSnapshot.class);
                when(taskRepository.findTasksByUserId(childId)).thenReturn(ApiFutures.immediateFuture(querySnapshot));
                when(querySnapshot.getDocuments()).thenReturn(Collections.singletonList(documentSnapshot));
                when(documentSnapshot.toObject(Task.class)).thenReturn(task);

                when(allowanceServiceProvider.get().calculateValueForTask(childId, taskId))
                                .thenReturn(new BigDecimal("10.00"));
                when(taskRepository.save(childId, task)).thenReturn(ApiFutures.immediateFuture(null));

                // When
                Task result = taskService.rejectTask(childId, taskId, parentId);

                // Then
                assertEquals(Task.TaskStatus.PENDING, result.getStatus());
                assertTrue(result.getAcknowledged());
                verify(taskRepository).save(childId, task);
                verify(ledgerService)
                                .addTransaction(
                                                eq(childId),
                                                any(BigDecimal.class),
                                                anyString(),
                                                eq(com.fazquepaga.taskandpay.allowance.Transaction.TransactionType.DEBIT));
        }

        @Test
        void shouldThrowExceptionWhenApproveTaskWithInvalidParent()
                        throws ExecutionException, InterruptedException {
                // Given
                String childId = "child-id";
                String taskId = "task-id";
                String parentId = "parent-id";

                when(userRepository.findByIdSync(parentId)).thenReturn(null);

                // When & Then
                assertThrows(
                                IllegalArgumentException.class,
                                () -> {
                                        taskService.approveTask(childId, taskId, parentId);
                                });
        }

        @Test
        void shouldThrowExceptionWhenRejectTaskNotApproved() throws ExecutionException, InterruptedException {
                // Given
                String childId = "child-id";
                String taskId = "task-id";
                String parentId = "parent-id";

                User parent = User.builder().id(parentId).role(User.Role.PARENT).build();
                Task task = Task.builder().id(taskId).status(Task.TaskStatus.PENDING).build();

                when(userRepository.findByIdSync(parentId)).thenReturn(parent);

                QuerySnapshot querySnapshot = Mockito.mock(QuerySnapshot.class);
                QueryDocumentSnapshot documentSnapshot = Mockito.mock(QueryDocumentSnapshot.class);
                when(taskRepository.findTasksByUserId(childId)).thenReturn(ApiFutures.immediateFuture(querySnapshot));
                when(querySnapshot.getDocuments()).thenReturn(Collections.singletonList(documentSnapshot));
                when(documentSnapshot.toObject(Task.class)).thenReturn(task);

                // When & Then
                assertThrows(IllegalStateException.class, () -> taskService.rejectTask(childId, taskId, parentId));
        }

        @Test
        void shouldThrowExceptionWhenApproveTaskAndChildNotBelongToParent()
                        throws ExecutionException, InterruptedException {
                // Given
                String childId = "child-id";
                String taskId = "task-id";
                String parentId = "parent-id";
                String anotherParentId = "another-parent-id";

                User parent = User.builder().id(parentId).role(User.Role.PARENT).build();
                User child = User.builder().id(childId).parentId(anotherParentId).build();

                when(userRepository.findByIdSync(parentId)).thenReturn(parent);
                when(userRepository.findByIdSync(childId)).thenReturn(child);

                // When & Then
                assertThrows(
                                IllegalArgumentException.class,
                                () -> {
                                        taskService.approveTask(childId, taskId, parentId);
                                });
        }

        @Test
        void shouldThrowExceptionWhenApproveTaskAndTaskNotFound()
                        throws ExecutionException, InterruptedException {
                // Given
                String childId = "child-id";
                String taskId = "task-id";
                String parentId = "parent-id";

                User parent = User.builder().id(parentId).role(User.Role.PARENT).build();
                User child = User.builder().id(childId).parentId(parentId).build();

                when(userRepository.findByIdSync(parentId)).thenReturn(parent);
                when(userRepository.findByIdSync(childId)).thenReturn(child);

                QuerySnapshot querySnapshot = Mockito.mock(QuerySnapshot.class);
                when(taskRepository.findTasksByUserId(childId))
                                .thenReturn(ApiFutures.immediateFuture(querySnapshot));
                when(querySnapshot.getDocuments()).thenReturn(Collections.emptyList());

                // When & Then
                assertThrows(
                                IllegalArgumentException.class,
                                () -> {
                                        taskService.approveTask(childId, taskId, parentId);
                                });
        }

        @Test
        void shouldThrowExceptionWhenApproveTaskAndTaskAlreadyApproved()
                        throws ExecutionException, InterruptedException {
                // Given
                String childId = "child-id";
                String taskId = "task-id";
                String parentId = "parent-id";

                User parent = User.builder().id(parentId).role(User.Role.PARENT).build();
                User child = User.builder().id(childId).parentId(parentId).build();
                Task task = Task.builder().id(taskId).status(Task.TaskStatus.APPROVED).build();

                when(userRepository.findByIdSync(parentId)).thenReturn(parent);
                when(userRepository.findByIdSync(childId)).thenReturn(child);

                QuerySnapshot querySnapshot = Mockito.mock(QuerySnapshot.class);
                QueryDocumentSnapshot documentSnapshot = Mockito.mock(QueryDocumentSnapshot.class);
                when(taskRepository.findTasksByUserId(childId))
                                .thenReturn(ApiFutures.immediateFuture(querySnapshot));
                when(querySnapshot.getDocuments()).thenReturn(Collections.singletonList(documentSnapshot));
                when(documentSnapshot.toObject(Task.class)).thenReturn(task);

                // When & Then
                assertThrows(
                                IllegalStateException.class,
                                () -> {
                                        taskService.approveTask(childId, taskId, parentId);
                                });
        }

        @Test
        void shouldThrowExceptionWhenCreateTaskAndChildNotFound()
                        throws ExecutionException, InterruptedException {
                // Given
                String userId = "user-id";
                CreateTaskRequest request = new CreateTaskRequest();
                when(userRepository.findByIdSync(userId)).thenReturn(null);

                // When & Then
                assertThrows(
                                IllegalArgumentException.class,
                                () -> {
                                        taskService.createTask(userId, request);
                                });
        }

        @Test
        void shouldThrowExceptionWhenCreateTaskAndParentNotFound()
                        throws ExecutionException, InterruptedException {
                // Given
                String userId = "user-id";
                String parentId = "parent-id";
                CreateTaskRequest request = new CreateTaskRequest();
                User child = User.builder().id(userId).role(User.Role.CHILD).parentId(parentId).build();

                when(userRepository.findByIdSync(userId)).thenReturn(child);
                when(userRepository.findByIdSync(parentId)).thenReturn(null);

                // When & Then
                assertThrows(
                                IllegalArgumentException.class,
                                () -> {
                                        taskService.createTask(userId, request);
                                });
        }

        @Test
        void shouldThrowExceptionWhenCreateTaskAndSubscriptionLimitReached()
                        throws ExecutionException, InterruptedException {
                // Given
                String userId = "user-id";
                String parentId = "parent-id";
                CreateTaskRequest request = new CreateTaskRequest();
                request.setType(Task.TaskType.DAILY);

                User child = User.builder().id(userId).role(User.Role.CHILD).parentId(parentId).build();
                User parent = User.builder().id(parentId).role(User.Role.PARENT).build();

                when(userRepository.findByIdSync(userId)).thenReturn(child);
                when(userRepository.findByIdSync(parentId)).thenReturn(parent);
                when(subscriptionService.canCreateTask(parent, 0)).thenReturn(false);

                QuerySnapshot querySnapshot = Mockito.mock(QuerySnapshot.class);
                when(taskRepository.findTasksByUserId(userId))
                                .thenReturn(ApiFutures.immediateFuture(querySnapshot));
                when(querySnapshot.getDocuments()).thenReturn(Collections.emptyList());

                // When & Then
                assertThrows(
                                SubscriptionLimitReachedException.class,
                                () -> {
                                        taskService.createTask(userId, request);
                                });
        }

        @Test
        void shouldUpdateTaskValue() throws ExecutionException, InterruptedException {
                // Given
                String childId = "child-id";
                Task task = Task.builder().id("task-id").build();
                when(taskRepository.save(childId, task)).thenReturn(ApiFutures.immediateFuture(null));

                // When
                taskService.updateTaskValue(childId, task);

                // Then
                verify(taskRepository).save(childId, task);
        }

        @Test
        void shouldDeleteTask() throws ExecutionException, InterruptedException {
                // Given
                String childId = "child-id";
                String taskId = "task-id";
                String parentId = "parent-id";

                User parent = User.builder().id(parentId).role(User.Role.PARENT).build();
                Task task = Task.builder().id(taskId).archived(false).build();

                when(userRepository.findByIdSync(parentId)).thenReturn(parent);

                QuerySnapshot querySnapshot = Mockito.mock(QuerySnapshot.class);
                QueryDocumentSnapshot documentSnapshot = Mockito.mock(QueryDocumentSnapshot.class);
                when(taskRepository.findTasksByUserId(childId)).thenReturn(ApiFutures.immediateFuture(querySnapshot));
                when(querySnapshot.getDocuments()).thenReturn(Collections.singletonList(documentSnapshot));
                when(documentSnapshot.toObject(Task.class)).thenReturn(task);

                when(taskRepository.save(childId, task)).thenReturn(ApiFutures.immediateFuture(null));

                // When
                taskService.deleteTask(childId, taskId, parentId);

                // Then
                assertTrue(task.getArchived());
                verify(taskRepository).save(childId, task);
        }

        @Test
        void shouldNotDeleteTaskIfAlreadyArchived() throws ExecutionException, InterruptedException {
                // Given
                String childId = "child-id";
                String taskId = "task-id";
                String parentId = "parent-id";

                User parent = User.builder().id(parentId).role(User.Role.PARENT).build();
                Task task = Task.builder().id(taskId).archived(true).build();

                when(userRepository.findByIdSync(parentId)).thenReturn(parent);

                QuerySnapshot querySnapshot = Mockito.mock(QuerySnapshot.class);
                QueryDocumentSnapshot documentSnapshot = Mockito.mock(QueryDocumentSnapshot.class);
                when(taskRepository.findTasksByUserId(childId)).thenReturn(ApiFutures.immediateFuture(querySnapshot));
                when(querySnapshot.getDocuments()).thenReturn(Collections.singletonList(documentSnapshot));
                when(documentSnapshot.toObject(Task.class)).thenReturn(task);

                // When
                taskService.deleteTask(childId, taskId, parentId);

                // Then
                // Should simply return without saving
                verify(taskRepository, Mockito.never()).save(anyString(), any(Task.class));
        }
}
