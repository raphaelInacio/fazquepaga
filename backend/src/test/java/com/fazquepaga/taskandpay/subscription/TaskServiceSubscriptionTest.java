package com.fazquepaga.taskandpay.subscription;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.fazquepaga.taskandpay.tasks.Task;
import com.fazquepaga.taskandpay.tasks.TaskRepository;
import com.fazquepaga.taskandpay.tasks.TaskService;
import com.fazquepaga.taskandpay.tasks.dto.CreateTaskRequest;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Integration tests for TaskService with subscription limits.
 */
class TaskServiceSubscriptionTest {

        @Mock
        private TaskRepository taskRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private SubscriptionService subscriptionService;

        @Mock
        private com.fazquepaga.taskandpay.allowance.LedgerService ledgerService;

        @Mock
        private com.fazquepaga.taskandpay.allowance.AllowanceService allowanceService;

        private TaskService taskService;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
                taskService = new TaskService(taskRepository, userRepository, subscriptionService, ledgerService,
                                allowanceService);
        }

        @Test
        void testCreateRecurringTask_FreeUserWithinLimit_ShouldSucceed()
                        throws ExecutionException, InterruptedException {
                // Given
                String childId = "child-id";
                String parentId = "parent-id";

                User child = User.builder()
                                .id(childId)
                                .role(User.Role.CHILD)
                                .parentId(parentId)
                                .build();

                User parent = User.builder()
                                .id(parentId)
                                .role(User.Role.PARENT)
                                .subscriptionTier(User.SubscriptionTier.FREE)
                                .subscriptionStatus(User.SubscriptionStatus.ACTIVE)
                                .build();

                CreateTaskRequest request = new CreateTaskRequest();
                request.setDescription("Daily Task");
                request.setType(Task.TaskType.DAILY);
                request.setWeight(Task.TaskWeight.MEDIUM);
                request.setRequiresProof(false);

                when(userRepository.findByIdSync(childId)).thenReturn(child);
                when(userRepository.findByIdSync(parentId)).thenReturn(parent);
                when(subscriptionService.canCreateTask(parent, 0)).thenReturn(true);
                when(taskRepository.save(eq(childId), any(Task.class)))
                                .thenReturn(ApiFutures.immediateFuture(null));

                // Mock empty task list (0 recurring tasks)
                QuerySnapshot querySnapshot = Mockito.mock(QuerySnapshot.class);
                when(taskRepository.findTasksByUserId(childId))
                                .thenReturn(ApiFutures.immediateFuture(querySnapshot));
                when(querySnapshot.getDocuments()).thenReturn(Collections.emptyList());

                // When
                Task result = taskService.createTask(childId, request);

                // Then
                assertNotNull(result);
                assertEquals("Daily Task", result.getDescription());
        }

        @Test
        void testCreateRecurringTask_FreeUserExceedsLimit_ShouldThrowException()
                        throws ExecutionException, InterruptedException {
                // Given
                String childId = "child-id";
                String parentId = "parent-id";

                User child = User.builder()
                                .id(childId)
                                .role(User.Role.CHILD)
                                .parentId(parentId)
                                .build();

                User parent = User.builder()
                                .id(parentId)
                                .role(User.Role.PARENT)
                                .subscriptionTier(User.SubscriptionTier.FREE)
                                .subscriptionStatus(User.SubscriptionStatus.ACTIVE)
                                .build();

                CreateTaskRequest request = new CreateTaskRequest();
                request.setDescription("6th Daily Task");
                request.setType(Task.TaskType.DAILY);
                request.setWeight(Task.TaskWeight.MEDIUM);
                request.setRequiresProof(false);

                when(userRepository.findByIdSync(childId)).thenReturn(child);
                when(userRepository.findByIdSync(parentId)).thenReturn(parent);
                when(subscriptionService.canCreateTask(parent, 5)).thenReturn(false);

                // Mock 5 existing recurring tasks
                QuerySnapshot querySnapshot = Mockito.mock(QuerySnapshot.class);
                when(taskRepository.findTasksByUserId(childId))
                                .thenReturn(ApiFutures.immediateFuture(querySnapshot));

                QueryDocumentSnapshot doc1 = Mockito.mock(QueryDocumentSnapshot.class);
                QueryDocumentSnapshot doc2 = Mockito.mock(QueryDocumentSnapshot.class);
                QueryDocumentSnapshot doc3 = Mockito.mock(QueryDocumentSnapshot.class);
                QueryDocumentSnapshot doc4 = Mockito.mock(QueryDocumentSnapshot.class);
                QueryDocumentSnapshot doc5 = Mockito.mock(QueryDocumentSnapshot.class);

                when(querySnapshot.getDocuments()).thenReturn(Arrays.asList(doc1, doc2, doc3, doc4, doc5));

                Task dailyTask = Task.builder().type(Task.TaskType.DAILY).build();
                when(doc1.toObject(Task.class)).thenReturn(dailyTask);
                when(doc2.toObject(Task.class)).thenReturn(dailyTask);
                when(doc3.toObject(Task.class)).thenReturn(dailyTask);
                when(doc4.toObject(Task.class)).thenReturn(dailyTask);
                when(doc5.toObject(Task.class)).thenReturn(dailyTask);

                // When & Then
                SubscriptionLimitReachedException exception = assertThrows(
                                SubscriptionLimitReachedException.class,
                                () -> taskService.createTask(childId, request));

                assertTrue(exception.getMessage().contains("Recurring task limit reached"));
        }

        @Test
        void testCreateOneTimeTask_FreeUser_ShouldNotCheckLimit() throws ExecutionException, InterruptedException {
                // Given
                String childId = "child-id";
                String parentId = "parent-id";

                User child = User.builder()
                                .id(childId)
                                .role(User.Role.CHILD)
                                .parentId(parentId)
                                .build();

                User parent = User.builder()
                                .id(parentId)
                                .role(User.Role.PARENT)
                                .subscriptionTier(User.SubscriptionTier.FREE)
                                .build();

                CreateTaskRequest request = new CreateTaskRequest();
                request.setDescription("One-time Task");
                request.setType(Task.TaskType.ONE_TIME);
                request.setWeight(Task.TaskWeight.HIGH);
                request.setRequiresProof(true);

                when(userRepository.findByIdSync(childId)).thenReturn(child);
                when(userRepository.findByIdSync(parentId)).thenReturn(parent);
                when(taskRepository.save(eq(childId), any(Task.class)))
                                .thenReturn(ApiFutures.immediateFuture(null));

                // When
                Task result = taskService.createTask(childId, request);

                // Then
                assertNotNull(result);
                assertEquals("One-time Task", result.getDescription());
                // Verify subscription service was NOT called for one-time tasks
                Mockito.verify(subscriptionService, Mockito.never()).canCreateTask(any(), any(Integer.class));
        }

        @Test
        void testCreateRecurringTask_PremiumUser_ShouldAllowUnlimited()
                        throws ExecutionException, InterruptedException {
                // Given
                String childId = "child-id";
                String parentId = "parent-id";

                User child = User.builder()
                                .id(childId)
                                .role(User.Role.CHILD)
                                .parentId(parentId)
                                .build();

                User parent = User.builder()
                                .id(parentId)
                                .role(User.Role.PARENT)
                                .subscriptionTier(User.SubscriptionTier.PREMIUM)
                                .subscriptionStatus(User.SubscriptionStatus.ACTIVE)
                                .build();

                CreateTaskRequest request = new CreateTaskRequest();
                request.setDescription("100th Weekly Task");
                request.setType(Task.TaskType.WEEKLY);
                request.setWeight(Task.TaskWeight.LOW);
                request.setRequiresProof(false);
                request.setDayOfWeek(1);

                when(userRepository.findByIdSync(childId)).thenReturn(child);
                when(userRepository.findByIdSync(parentId)).thenReturn(parent);
                when(subscriptionService.canCreateTask(parent, 99)).thenReturn(true);
                when(taskRepository.save(eq(childId), any(Task.class)))
                                .thenReturn(ApiFutures.immediateFuture(null));

                // Mock 99 existing recurring tasks
                QuerySnapshot querySnapshot = Mockito.mock(QuerySnapshot.class);
                when(taskRepository.findTasksByUserId(childId))
                                .thenReturn(ApiFutures.immediateFuture(querySnapshot));

                // Create mock documents that return Task objects with type
                QueryDocumentSnapshot mockDoc = Mockito.mock(QueryDocumentSnapshot.class);
                Task weeklyTask = Task.builder().type(Task.TaskType.WEEKLY).build();
                when(mockDoc.toObject(Task.class)).thenReturn(weeklyTask);

                when(querySnapshot.getDocuments())
                                .thenReturn(Collections.nCopies(99, mockDoc));

                // When
                Task result = taskService.createTask(childId, request);

                // Then
                assertNotNull(result);
                assertEquals("100th Weekly Task", result.getDescription());
        }
}
