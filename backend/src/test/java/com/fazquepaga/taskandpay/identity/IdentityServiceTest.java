package com.fazquepaga.taskandpay.identity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fazquepaga.taskandpay.identity.dto.CreateChildRequest;
import com.fazquepaga.taskandpay.identity.dto.CreateParentRequest;
import com.google.api.core.ApiFutures;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class IdentityServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    private IdentityService identityService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldRegisterParent() throws ExecutionException, InterruptedException {

        // Given

        CreateParentRequest request = new CreateParentRequest();

        request.setName("Test Parent");

        request.setEmail("parent@test.com");

        // Mock the save operation to do nothing and return a completed future
        when(userRepository.save(any(User.class))).thenReturn(ApiFutures.immediateFuture(null));
        when(passwordEncoder.encode(any())).thenReturn("hashed_password");

        // When

        User result = identityService.registerParent(request);

        // Then

        assertNotNull(result);

        assertEquals(User.Role.PARENT, result.getRole());

        assertEquals("Test Parent", result.getName());
    }

    @Test
    void shouldCreateChild() throws ExecutionException, InterruptedException {

        // Given

        String parentId = "parent-id";

        User parent = User.builder().id(parentId).role(User.Role.PARENT).build();

        CreateChildRequest request = new CreateChildRequest();

        request.setName("Test Child");

        request.setPhoneNumber("123456789");

        request.setParentId(parentId);

        when(userRepository.findByIdSync(parentId)).thenReturn(parent);

        when(userRepository.save(any(User.class))).thenReturn(ApiFutures.immediateFuture(null));
        when(userRepository.findByAccessCode(any())).thenReturn(java.util.Optional.empty());

        // When

        User result = identityService.createChild(request);

        // Then

        assertNotNull(result);

        assertEquals(User.Role.CHILD, result.getRole());

        assertEquals(parentId, result.getParentId());

        assertEquals("Test Child", result.getName());
    }

    @Test
    void shouldThrowExceptionWhenParentNotFoundForChildCreation()
            throws ExecutionException, InterruptedException {

        // Given

        String parentId = "non-existent-parent-id";

        CreateChildRequest request = new CreateChildRequest();

        request.setName("Test Child");

        request.setParentId(parentId);

        when(userRepository.findByIdSync(parentId)).thenReturn(null);

        // When & Then

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> {
                    identityService.createChild(request);
                });

        assertEquals("Parent with ID " + parentId + " not found.", exception.getMessage());
    }

    @Test
    void shouldGenerateOnboardingCode() throws ExecutionException, InterruptedException {
        // Given
        String childId = "child-123";
        String accessCode = "ABC123";
        User child = User.builder().id(childId).accessCode(accessCode).build(); // Mock child with access code

        when(userRepository.findByIdSync(childId)).thenReturn(child);

        // When
        String code = identityService.generateOnboardingCode(childId);

        // Then
        assertNotNull(code);
        assertEquals(accessCode, code);
    }

    @Test
    void shouldCompleteOnboardingSuccessfully() throws ExecutionException, InterruptedException {
        // Given
        String childId = "child-123";
        String accessCode = "ABC123";
        String phoneNumber = "+1234567890";

        User child = User.builder().id(childId).name("Test Child").role(User.Role.CHILD).accessCode(accessCode).build();

        // Mock findByAccessCode instead of map lookup
        when(userRepository.findByAccessCode(accessCode)).thenReturn(java.util.Optional.of(child));
        when(userRepository.save(any(User.class))).thenReturn(ApiFutures.immediateFuture(null));

        // When
        User result = identityService.completeOnboarding(accessCode, phoneNumber);

        // Then
        assertNotNull(result);
        assertEquals(phoneNumber, result.getPhoneNumber());
        assertEquals(childId, result.getId());
    }

    @Test
    void shouldThrowExceptionForInvalidOnboardingCode() throws ExecutionException, InterruptedException {
        // Given
        String invalidCode = "INVALID";
        String phoneNumber = "+1234567890";

        when(userRepository.findByAccessCode(invalidCode)).thenReturn(java.util.Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> identityService.completeOnboarding(invalidCode, phoneNumber));

        assertEquals("Invalid onboarding code.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenParentIdIsNull() {
        // Given
        CreateChildRequest request = new CreateChildRequest();
        request.setName("Test Child");
        request.setParentId(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> identityService.createChild(request));

        assertEquals("Parent ID is required to create a child.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenParentIdIsEmpty() {
        // Given
        CreateChildRequest request = new CreateChildRequest();
        request.setName("Test Child");
        request.setParentId("");

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> identityService.createChild(request));

        assertEquals("Parent ID is required to create a child.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenParentRoleIsNotParent()
            throws ExecutionException, InterruptedException {
        // Given
        String parentId = "parent-id";
        User notAParent = User.builder()
                .id(parentId)
                .role(User.Role.CHILD) // Wrong role
                .build();

        CreateChildRequest request = new CreateChildRequest();
        request.setName("Test Child");
        request.setParentId(parentId);

        when(userRepository.findByIdSync(parentId)).thenReturn(notAParent);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> identityService.createChild(request));

        assertEquals("Parent with ID " + parentId + " not found.", exception.getMessage());
    }

    @Test
    void shouldUpdateChildAllowance() throws ExecutionException, InterruptedException {
        // Given
        String childId = "child-id";
        java.math.BigDecimal allowance = new java.math.BigDecimal("50.00");
        User child = User.builder().id(childId).build();

        when(userRepository.findByIdSync(childId)).thenReturn(child);
        when(userRepository.save(any(User.class))).thenReturn(ApiFutures.immediateFuture(null));

        // When
        User result = identityService.updateChildAllowance(childId, allowance);

        // Then
        assertNotNull(result);
        assertEquals(allowance, result.getMonthlyAllowance());
    }

    @Test
    void shouldUpdateChild() throws ExecutionException, InterruptedException {
        // Given
        String childId = "child-id";
        String parentId = "parent-id";
        User child = User.builder()
                .id(childId)
                .parentId(parentId)
                .name("Old Name")
                .age(8)
                .phoneNumber("111111111")
                .role(User.Role.CHILD)
                .build();

        com.fazquepaga.taskandpay.identity.dto.UpdateChildRequest request = new com.fazquepaga.taskandpay.identity.dto.UpdateChildRequest();
        request.setName("New Name");
        request.setAge(9);
        request.setPhoneNumber("222222222");

        when(userRepository.findByIdSync(childId)).thenReturn(child);
        when(userRepository.save(any(User.class))).thenReturn(ApiFutures.immediateFuture(null));

        // When
        User result = identityService.updateChild(childId, request, parentId);

        // Then
        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals(9, result.getAge());
        assertEquals("222222222", result.getPhoneNumber());
    }

    @Test
    void shouldUpdateChildPartially() throws ExecutionException, InterruptedException {
        // Given
        String childId = "child-id";
        String parentId = "parent-id";
        User child = User.builder()
                .id(childId)
                .parentId(parentId)
                .name("Old Name")
                .age(8)
                .phoneNumber("111111111")
                .role(User.Role.CHILD)
                .build();

        com.fazquepaga.taskandpay.identity.dto.UpdateChildRequest request = new com.fazquepaga.taskandpay.identity.dto.UpdateChildRequest();
        request.setName("New Name"); // Only update name

        when(userRepository.findByIdSync(childId)).thenReturn(child);
        when(userRepository.save(any(User.class))).thenReturn(ApiFutures.immediateFuture(null));

        // When
        User result = identityService.updateChild(childId, request, parentId);

        // Then
        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals(8, result.getAge()); // Unchanged
        assertEquals("111111111", result.getPhoneNumber()); // Unchanged
    }

    @Test
    void shouldThrowExceptionWhenUpdatingChildOfDifferentParent() throws ExecutionException, InterruptedException {
        // Given
        String childId = "child-id";
        String parentId = "parent-id";
        String wrongParentId = "wrong-parent-id";
        User child = User.builder()
                .id(childId)
                .parentId(wrongParentId) // Different parent
                .role(User.Role.CHILD)
                .build();

        com.fazquepaga.taskandpay.identity.dto.UpdateChildRequest request = new com.fazquepaga.taskandpay.identity.dto.UpdateChildRequest();
        request.setName("New Name");

        when(userRepository.findByIdSync(childId)).thenReturn(child);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> identityService.updateChild(childId, request, parentId));

        assertEquals("Child does not belong to this parent", exception.getMessage());
    }

    @Test
    void shouldDeleteChild() throws ExecutionException, InterruptedException {
        // Given
        String childId = "child-id";
        String parentId = "parent-id";
        User child = User.builder()
                .id(childId)
                .parentId(parentId)
                .role(User.Role.CHILD)
                .build();

        when(userRepository.findByIdSync(childId)).thenReturn(child);
        when(userRepository.delete(childId)).thenReturn(ApiFutures.immediateFuture(null));

        // When & Then (should not throw)
        assertDoesNotThrow(() -> identityService.deleteChild(childId, parentId));
    }

    @Test
    void shouldThrowExceptionWhenDeletingChildOfDifferentParent() throws ExecutionException, InterruptedException {
        // Given
        String childId = "child-id";
        String parentId = "parent-id";
        String wrongParentId = "wrong-parent-id";
        User child = User.builder()
                .id(childId)
                .parentId(wrongParentId) // Different parent
                .role(User.Role.CHILD)
                .build();

        when(userRepository.findByIdSync(childId)).thenReturn(child);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> identityService.deleteChild(childId, parentId));

        assertEquals("Child does not belong to this parent", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentChild() throws ExecutionException, InterruptedException {
        // Given
        String childId = "non-existent-child";
        String parentId = "parent-id";

        when(userRepository.findByIdSync(childId)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> identityService.deleteChild(childId, parentId));

        assertEquals("Child not found", exception.getMessage());
    }
}
