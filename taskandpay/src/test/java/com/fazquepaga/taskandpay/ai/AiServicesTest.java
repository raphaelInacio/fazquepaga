package com.fazquepaga.taskandpay.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AiServicesTest {

    @Mock
    private ChatClient chatClient;

    private AiSuggestionService suggestionService;
    private AiValidatorImpl aiValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        suggestionService = new AiSuggestionService(chatClient);
        aiValidator = new AiValidatorImpl(chatClient);
    }

    @Test
    void shouldReturnSuggestions() {
        String suggestionText = "task 1, task 2, task 3";
        Generation generation = new Generation(suggestionText);
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(chatClient.call(any(Prompt.class))).thenReturn(chatResponse);

        List<String> suggestions = suggestionService.getSuggestions(10);

        assertEquals(3, suggestions.size());
        assertEquals(" task 2", suggestions.get(1));
    }

    @Test
    void shouldReturnTrueForValidImage() {
        Generation generation = new Generation("yes");
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(chatClient.call(any(Prompt.class))).thenReturn(chatResponse);

        boolean isValid = aiValidator.validateTaskCompletionImage(new byte[0], "a task");

        assertTrue(isValid);
    }
}
