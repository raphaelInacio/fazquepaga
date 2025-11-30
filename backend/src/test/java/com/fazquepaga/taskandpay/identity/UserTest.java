package com.fazquepaga.taskandpay.identity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void shouldBuildUserWithBuilder() {
        // Given & When
        User user =
                User.builder()
                        .id("user-id")
                        .name("John Doe")
                        .email("john@example.com")
                        .role(User.Role.PARENT)
                        .build();

        // Then
        assertNotNull(user);
        assertEquals("user-id", user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals(User.Role.PARENT, user.getRole());
    }

    @Test
    void shouldCreateParentUser() {
        // Given & When
        User parent =
                User.builder()
                        .id("parent-id")
                        .name("Parent Name")
                        .email("parent@example.com")
                        .role(User.Role.PARENT)
                        .build();

        // Then
        assertEquals(User.Role.PARENT, parent.getRole());
        assertNotNull(parent.getEmail());
    }

    @Test
    void shouldCreateChildUser() {
        // Given & When
        User child =
                User.builder()
                        .id("child-id")
                        .name("Child Name")
                        .phoneNumber("+1234567890")
                        .parentId("parent-id")
                        .role(User.Role.CHILD)
                        .build();

        // Then
        assertEquals(User.Role.CHILD, child.getRole());
        assertEquals("parent-id", child.getParentId());
        assertEquals("+1234567890", child.getPhoneNumber());
    }

    @Test
    void shouldSupportAllUserRoles() {
        // Then
        assertNotNull(User.Role.PARENT);
        assertNotNull(User.Role.CHILD);
        assertEquals(2, User.Role.values().length);
    }

    @Test
    void shouldCreateUserWithAllFields() {
        // Given & When
        User user =
                User.builder()
                        .id("user-id")
                        .name("Full Name")
                        .email("email@example.com")
                        .phoneNumber("+1234567890")
                        .parentId("parent-id")
                        .age(10)
                        .role(User.Role.CHILD)
                        .build();

        // Then
        assertNotNull(user.getId());
        assertNotNull(user.getName());
        assertNotNull(user.getEmail());
        assertNotNull(user.getPhoneNumber());
        assertNotNull(user.getParentId());
        assertNotNull(user.getAge());
        assertNotNull(user.getRole());
    }
}
