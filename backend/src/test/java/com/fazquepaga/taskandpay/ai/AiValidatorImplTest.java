package com.fazquepaga.taskandpay.ai;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.AssistantMessage;

class AiValidatorImplTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private ChatResponse chatResponse;

    @Mock
    private Generation generation;

    private AiValidatorImpl aiValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        aiValidator = new AiValidatorImpl(chatModel);
    }

    @Test
    void shouldReturnTrueWhenImageMatchesTask() {
        // Given
        byte[] image = new byte[] { 1, 2, 3 };
        String taskDescription = "Clean your room";

        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(new AssistantMessage("yes"));

        // When
        boolean result = aiValidator.validateTaskCompletionImage(image, taskDescription);

        // Then
        assertTrue(result);
        verify(chatModel).call(any(Prompt.class));
    }

    @Test
    void shouldReturnFalseWhenImageDoesNotMatchTask() {
        // Given
        byte[] image = new byte[] { 1, 2, 3 };
        String taskDescription = "Do homework";

        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(new AssistantMessage("no"));

        // When
        boolean result = aiValidator.validateTaskCompletionImage(image, taskDescription);

        // Then
        assertFalse(result);
    }

    @Test
    void shouldHandleYesWithWhitespace() {
        // Given
        byte[] image = new byte[] { 1, 2, 3 };
        String taskDescription = "Make bed";

        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(new AssistantMessage("  YES  "));

        // When
        boolean result = aiValidator.validateTaskCompletionImage(image, taskDescription);

        // Then
        assertTrue(result);
    }

    @Test
    void shouldHandleMixedCaseResponse() {
        // Given
        byte[] image = new byte[] { 1, 2, 3 };
        String taskDescription = "Wash dishes";

        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(new AssistantMessage("Yes"));

        // When
        boolean result = aiValidator.validateTaskCompletionImage(image, taskDescription);

        // Then
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseForUnexpectedResponse() {
        // Given
        byte[] image = new byte[] { 1, 2, 3 };
        String taskDescription = "Take out trash";

        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(new AssistantMessage("maybe"));

        // When
        boolean result = aiValidator.validateTaskCompletionImage(image, taskDescription);

        // Then
        assertFalse(result);
    }

    @Test
    void shouldHandleEmptyImage() {
        // Given
        byte[] image = new byte[] {};
        String taskDescription = "Feed pet";

        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(new AssistantMessage("no"));

        // When
        boolean result = aiValidator.validateTaskCompletionImage(image, taskDescription);

        // Then
        assertFalse(result);
    }
}
