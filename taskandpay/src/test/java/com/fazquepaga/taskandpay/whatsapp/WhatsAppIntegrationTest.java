package com.fazquepaga.taskandpay.whatsapp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.dto.CreateChildRequest;
import com.fazquepaga.taskandpay.identity.dto.CreateParentRequest;
import com.fazquepaga.taskandpay.tasks.Task;
import com.fazquepaga.taskandpay.tasks.dto.CreateTaskRequest;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.cloud.spring.data.firestore.FirestoreTemplate;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.FirestoreEmulatorContainer;
import org.testcontainers.containers.PubSubEmulatorContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public class WhatsAppIntegrationTest {

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

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private FirestoreTemplate firestoreTemplate;

    @MockBean private PubSubTemplate pubSubTemplate;

    @AfterEach
    void cleanup() {
        firestoreTemplate.deleteAll(User.class).block();
        firestoreTemplate.deleteAll(Task.class).block();
    }

    @Test
    void testOnboardingAndProofSubmission() throws Exception {
        // 1. Create Parent and Child
        CreateParentRequest parentRequest = new CreateParentRequest();
        parentRequest.setName("Test Parent");
        parentRequest.setEmail("parent@test.com");
        String parentResponse =
                mockMvc.perform(
                                post("/api/v1/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(parentRequest)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        User parent = objectMapper.readValue(parentResponse, User.class);

        CreateChildRequest childRequest = new CreateChildRequest();
        childRequest.setName("Test Child");
        childRequest.setParentId(parent.getId());
        String childResponse =
                mockMvc.perform(
                                post("/api/v1/children")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(childRequest)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        User child = objectMapper.readValue(childResponse, User.class);

        // 2. Generate Onboarding Code
        String codeResponse =
                mockMvc.perform(post("/api/v1/children/" + child.getId() + "/onboarding-code"))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        String onboardingCode = objectMapper.readTree(codeResponse).get("code").asText();

        // 3. Simulate Webhook for Onboarding
        String childPhoneNumber = "+1234567890";
        Map<String, String> onboardingPayload =
                Map.of("Body", onboardingCode, "From", "whatsapp:" + childPhoneNumber);
        mockMvc.perform(
                        post("/api/v1/whatsapp/webhook")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(onboardingPayload)))
                .andExpect(status().isOk());

        // 4. Create a Task requiring proof
        CreateTaskRequest taskRequest = new CreateTaskRequest();
        taskRequest.setDescription("Clean your room");
        taskRequest.setRequiresProof(true);
        taskRequest.setType(Task.TaskType.ONE_TIME);
        taskRequest.setWeight(Task.TaskWeight.MEDIUM);
        mockMvc.perform(
                        post("/api/v1/tasks?child_id=" + child.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated());

        // 5. Simulate Webhook for Proof Submission
        String imageUrl = "http://example.com/image.jpg";
        Map<String, String> proofPayload =
                Map.of("MediaUrl0", imageUrl, "From", "whatsapp:" + childPhoneNumber);

        mockMvc.perform(
                        post("/api/v1/whatsapp/webhook")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(proofPayload)))
                .andExpect(status().isOk());

        // 6. Verify event was published
        verify(pubSubTemplate).publish(anyString(), any(Object.class));
    }
}
