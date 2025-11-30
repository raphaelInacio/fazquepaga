package com.fazquepaga.taskandpay.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.tasks.Task;
import com.fazquepaga.taskandpay.whatsapp.events.ProofSubmittedEvent;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.FirestoreEmulatorContainer;
import org.testcontainers.containers.PubSubEmulatorContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Disabled("Integration tests disabled - requires Docker/Testcontainers")
public class AiIntegrationTest {

    @Container
    private static final FirestoreEmulatorContainer firestoreEmulator =
            new FirestoreEmulatorContainer(
                    DockerImageName.parse("gcr.io/google.com/cloudsdktool/google-cloud-cli:latest")
                            .asCompatibleSubstituteFor("google/cloud-sdk"));

    @Container
    private static final PubSubEmulatorContainer pubsubEmulator =
            new PubSubEmulatorContainer(
                    DockerImageName.parse("gcr.io/google.com/cloudsdktool/google-cloud-cli:latest")
                            .asCompatibleSubstituteFor("google/cloud-sdk"));

    @DynamicPropertySource
    static void emulatorsProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.cloud.gcp.firestore.host-port", firestoreEmulator::getEmulatorEndpoint);
        registry.add("spring.cloud.gcp.pubsub.emulator-host", pubsubEmulator::getEmulatorEndpoint);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public CredentialsProvider googleCredentials() {
            return NoCredentialsProvider.create();
        }
    }

    @Autowired private PubSubTemplate pubSubTemplate;

    @Autowired private Firestore firestore;

    @MockBean private ChatModel chatModel;

    @Value("${pubsub.topic-name}")
    private String topicName;

    @AfterEach
    void cleanup() throws Exception {
        // Not the best way to clean up, but it works for tests
        for (com.google.cloud.firestore.CollectionReference collection :
                firestore.listCollections()) {
            for (com.google.cloud.firestore.DocumentReference doc : collection.listDocuments()) {
                doc.delete().get();
            }
        }
    }

    @Test
    void testProofValidationFlow() throws Exception {
        // 1. Mock the AI response
        Generation generation = new Generation(new AssistantMessage("yes"));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // 2. Create user and task in Firestore
        User child = User.builder().id("child1").name("Test Child").build();
        Task task =
                Task.builder()
                        .id("task1")
                        .description("Clean room")
                        .requiresProof(true)
                        .status(Task.TaskStatus.PENDING)
                        .build();

        firestore.collection("users").document("child1").set(child).get();
        firestore
                .collection("users")
                .document("child1")
                .collection("tasks")
                .document("task1")
                .set(task)
                .get();

        // 3. Publish event to Pub/Sub
        ProofSubmittedEvent event =
                new ProofSubmittedEvent("child1", "task1", "http://example.com/image.jpg");
        pubSubTemplate.publish(topicName, event);

        // 4. Wait for the listener to process the message
        TimeUnit.SECONDS.sleep(5);

        // 5. Verify the task was updated in Firestore
        com.google.cloud.firestore.DocumentSnapshot updatedTaskDoc =
                firestore
                        .collection("users")
                        .document("child1")
                        .collection("tasks")
                        .document("task1")
                        .get()
                        .get();
        Task updatedTask = updatedTaskDoc.toObject(Task.class);

        assertTrue(updatedTask.getAiValidated());
        assertEquals(Task.TaskStatus.PENDING_APPROVAL, updatedTask.getStatus());
    }
}
