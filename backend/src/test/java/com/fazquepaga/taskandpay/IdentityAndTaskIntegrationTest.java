package com.fazquepaga.taskandpay;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.FirestoreEmulatorContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Disabled("Integration tests disabled - requires Docker/Testcontainers")
class IdentityAndTaskIntegrationTest {

    @Container
    private static final FirestoreEmulatorContainer firestoreEmulator =
            new FirestoreEmulatorContainer(
                    DockerImageName.parse("google/cloud-sdk:latest")
                            .asCompatibleSubstituteFor(
                                    "gcr.io/google.com/cloudsdktool/google-cloud-cli"));

    @DynamicPropertySource
    static void firestoreProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.cloud.gcp.firestore.host-port", firestoreEmulator::getEmulatorEndpoint);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public CredentialsProvider googleCredentials() {
            return NoCredentialsProvider.create();
        }
    }

    @Autowired private MockMvc mockMvc;

    @Autowired private FirestoreTemplate firestoreTemplate;

    @Autowired private ObjectMapper objectMapper;

    @AfterEach
    void cleanup() {
        firestoreTemplate.deleteAll(User.class).block();
    }

    @Test
    void shouldRegisterParentAndCreateChildAndTasks() throws Exception {
        // 1. Register Parent
        CreateParentRequest parentRequest = new CreateParentRequest();
        parentRequest.setName("Test Parent");
        parentRequest.setEmail("parent@test.com");

        String parentJson = objectMapper.writeValueAsString(parentRequest);

        String registeredParentJson =
                mockMvc.perform(
                                post("/api/v1/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(parentJson))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").exists())
                        .andExpect(jsonPath("$.name").value("Test Parent"))
                        .andExpect(jsonPath("$.role").value("PARENT"))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        User registeredParent = objectMapper.readValue(registeredParentJson, User.class);
        String parentId = registeredParent.getId();

        // 2. Create Child
        CreateChildRequest childRequest = new CreateChildRequest();
        childRequest.setName("Test Child");
        childRequest.setPhoneNumber("1234567890");
        childRequest.setParentId(parentId);

        String childJson = objectMapper.writeValueAsString(childRequest);

        String createdChildJson =
                mockMvc.perform(
                                post("/api/v1/children")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(childJson))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").exists())
                        .andExpect(jsonPath("$.name").value("Test Child"))
                        .andExpect(jsonPath("$.role").value("CHILD"))
                        .andExpect(jsonPath("$.parentId").value(parentId))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        User createdChild = objectMapper.readValue(createdChildJson, User.class);
        String childId = createdChild.getId();

        // 3. Create Task for Child
        CreateTaskRequest task1Request = new CreateTaskRequest();
        task1Request.setDescription("Do homework");
        task1Request.setType(Task.TaskType.DAILY);
        task1Request.setWeight(Task.TaskWeight.MEDIUM);

        String task1Json = objectMapper.writeValueAsString(task1Request);

        mockMvc.perform(
                        post("/api/v1/tasks")
                                .param("child_id", childId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(task1Json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description").value("Do homework"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        // 4. Create another Task for Child
        CreateTaskRequest task2Request = new CreateTaskRequest();
        task2Request.setDescription("Clean room");
        task2Request.setType(Task.TaskType.WEEKLY);
        task2Request.setWeight(Task.TaskWeight.HIGH);

        String task2Json = objectMapper.writeValueAsString(task2Request);

        mockMvc.perform(
                        post("/api/v1/tasks")
                                .param("child_id", childId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(task2Json))
                .andExpect(status().isCreated());

        // 5. Get all tasks for the child
        mockMvc.perform(get("/api/v1/tasks").param("child_id", childId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingChildWithNoParentId() throws Exception {
        CreateChildRequest childRequest = new CreateChildRequest();
        childRequest.setName("Orphan Child");

        String childJson = objectMapper.writeValueAsString(childRequest);

        mockMvc.perform(
                        post("/api/v1/children")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(childJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parent ID is required to create a child."));
    }
}
