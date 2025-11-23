package com.fazquepaga.taskandpay.identity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazquepaga.taskandpay.identity.dto.CreateChildRequest;
import com.fazquepaga.taskandpay.identity.dto.CreateParentRequest;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(IdentityController.class)
class IdentityControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private IdentityService identityService;

        @Test
        void shouldRegisterParentSuccessfully() throws Exception {
                // Given
                CreateParentRequest request = new CreateParentRequest();
                request.setName("John Doe");
                request.setEmail("john@example.com");

                User parent = User.builder()
                                .id("parent-id")
                                .name("John Doe")
                                .email("john@example.com")
                                .role(User.Role.PARENT)
                                .build();

                when(identityService.registerParent(any(CreateParentRequest.class))).thenReturn(parent);

                // When & Then
                mockMvc.perform(
                                post("/api/v1/auth/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value("parent-id"))
                                .andExpect(jsonPath("$.name").value("John Doe"))
                                .andExpect(jsonPath("$.email").value("john@example.com"))
                                .andExpect(jsonPath("$.role").value("PARENT"));
        }

        @Test
        void shouldReturnBadRequestWhenRegisterParentFails() throws Exception {
                // Given
                CreateParentRequest request = new CreateParentRequest();
                request.setName("John Doe");
                request.setEmail("invalid-email");

                when(identityService.registerParent(any(CreateParentRequest.class)))
                                .thenThrow(new IllegalArgumentException("Invalid email format"));

                // When & Then
                mockMvc.perform(
                                post("/api/v1/auth/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Invalid email format"));
        }

        @Test
        void shouldCreateChildSuccessfully() throws Exception {
                // Given
                CreateChildRequest request = new CreateChildRequest();
                request.setName("Jane Doe");
                request.setParentId("parent-id");
                request.setPhoneNumber("+1234567890");

                User child = User.builder()
                                .id("child-id")
                                .name("Jane Doe")
                                .parentId("parent-id")
                                .phoneNumber("+1234567890")
                                .role(User.Role.CHILD)
                                .build();

                when(identityService.createChild(any(CreateChildRequest.class))).thenReturn(child);

                // When & Then
                mockMvc.perform(
                                post("/api/v1/children")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value("child-id"))
                                .andExpect(jsonPath("$.name").value("Jane Doe"))
                                .andExpect(jsonPath("$.parentId").value("parent-id"))
                                .andExpect(jsonPath("$.role").value("CHILD"));
        }

        @Test
        void shouldReturnBadRequestWhenParentNotFound() throws Exception {
                // Given
                CreateChildRequest request = new CreateChildRequest();
                request.setName("Jane Doe");
                request.setParentId("non-existent-parent");

                when(identityService.createChild(any(CreateChildRequest.class)))
                                .thenThrow(
                                                new IllegalArgumentException(
                                                                "Parent with ID non-existent-parent not found."));

                // When & Then
                mockMvc.perform(
                                post("/api/v1/children")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(
                                                jsonPath("$.message")
                                                                .value("Parent with ID non-existent-parent not found."));
        }

        @Test
        void shouldGenerateOnboardingCodeSuccessfully() throws Exception {
                // Given
                String childId = "child-id";
                String onboardingCode = "ABC123";

                when(identityService.generateOnboardingCode(childId)).thenReturn(onboardingCode);

                // When & Then
                mockMvc.perform(post("/api/v1/children/{childId}/onboarding-code", childId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(onboardingCode));
        }

        @Test
        void shouldUpdateChildAllowanceSuccessfully() throws Exception {
                // Given
                String childId = "child-id";
                java.math.BigDecimal allowance = new java.math.BigDecimal("50.00");
                java.util.Map<String, java.math.BigDecimal> request = java.util.Map.of("allowance", allowance);

                User updatedChild = User.builder()
                                .id(childId)
                                .name("Child")
                                .monthlyAllowance(allowance)
                                .build();

                when(identityService.updateChildAllowance(childId, allowance)).thenReturn(updatedChild);

                // When & Then
                mockMvc.perform(
                                post("/api/v1/children/{childId}/allowance", childId)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.monthlyAllowance").value(50.00));
        }

        @Test
        void shouldGetChildSuccessfully() throws Exception {
                // Given
                String childId = "child-id";
                User child = User.builder()
                                .id(childId)
                                .name("Jane Doe")
                                .role(User.Role.CHILD)
                                .build();

                when(identityService.getChild(childId, "parent-id")).thenReturn(child);

                // When & Then
                mockMvc.perform(get("/api/v1/children/{childId}", childId)
                                .param("parent_id", "parent-id"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(childId))
                                .andExpect(jsonPath("$.name").value("Jane Doe"));
        }
}
