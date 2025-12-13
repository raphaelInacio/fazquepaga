package com.fazquepaga.taskandpay.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fazquepaga.taskandpay.identity.UserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

class AiServicesTest {

    @Mock
    private ChatModel chatModel;
    @Mock
    private UserRepository userRepository;

    private AiSuggestionService suggestionService;
    private AiValidatorImpl aiValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        suggestionService = new AiSuggestionService(chatModel, userRepository);
        aiValidator = new AiValidatorImpl(chatModel);
    }

    @Test
    void shouldReturnSuggestions() {
        String suggestionText = "task 1, task 2, task 3";
        Generation generation = new Generation(new AssistantMessage(suggestionText));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        List<String> suggestions = suggestionService.getSuggestions(10, "pt");

        assertEquals(3, suggestions.size());
        assertEquals(" task 2", suggestions.get(1));
    }

    @Test
    void shouldReturnSuggestionsInEnglish() {
        String suggestionText = "clean room, do homework, wash dishes";
        Generation generation = new Generation(new AssistantMessage(suggestionText));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        List<String> suggestions = suggestionService.getSuggestions(10, "en");

        assertEquals(3, suggestions.size());
        assertTrue(suggestions.get(0).contains("clean"));
    }

    @Test
    void shouldReturnTrueForValidImage() {
        Generation generation = new Generation(new AssistantMessage("yes"));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        boolean isValid = aiValidator.validateTaskCompletionImage(new byte[0], "a task");

        assertTrue(isValid);
    }
}
