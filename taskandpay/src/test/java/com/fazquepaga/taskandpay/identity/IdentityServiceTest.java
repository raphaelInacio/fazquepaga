package com.fazquepaga.taskandpay.identity;

import com.fazquepaga.taskandpay.identity.dto.CreateChildRequest;

import com.fazquepaga.taskandpay.identity.dto.CreateParentRequest;

import com.google.api.core.ApiFutures;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;

import org.mockito.Mock;

import org.mockito.MockitoAnnotations;



import java.util.concurrent.ExecutionException;



import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.when;



class IdentityServiceTest {



    @Mock

    private UserRepository userRepository;



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

        User parent = User.builder()

                .id(parentId)

                .role(User.Role.PARENT)

                .build();



        CreateChildRequest request = new CreateChildRequest();

        request.setName("Test Child");

        request.setPhoneNumber("123456789");

        request.setParentId(parentId);



        when(userRepository.findByIdSync(parentId)).thenReturn(parent);

        when(userRepository.save(any(User.class))).thenReturn(ApiFutures.immediateFuture(null));



        // When

        User result = identityService.createChild(request);



        // Then

        assertNotNull(result);

        assertEquals(User.Role.CHILD, result.getRole());

        assertEquals(parentId, result.getParentId());

        assertEquals("Test Child", result.getName());

    }



    @Test

    void shouldThrowExceptionWhenParentNotFoundForChildCreation() throws ExecutionException, InterruptedException {

        // Given

        String parentId = "non-existent-parent-id";

        CreateChildRequest request = new CreateChildRequest();

        request.setName("Test Child");

        request.setParentId(parentId);



        when(userRepository.findByIdSync(parentId)).thenReturn(null);



        // When & Then

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {

            identityService.createChild(request);

        });

        assertEquals("Parent with ID " + parentId + " not found.", exception.getMessage());

    }

}
