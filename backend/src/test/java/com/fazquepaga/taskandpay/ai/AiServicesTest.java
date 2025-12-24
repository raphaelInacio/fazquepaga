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

    @Mock private ChatModel chatModel;
    @Mock private UserRepository userRepository;

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

        List<String> suggestions = suggestionService.getSuggestions(10, "pt", null);

        assertEquals(3, suggestions.size());
        assertEquals(" task 2", suggestions.get(1));
    }

    @Test
    void shouldReturnSuggestionsInEnglish() {
        String suggestionText = "clean room, do homework, wash dishes";
        Generation generation = new Generation(new AssistantMessage(suggestionText));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        List<String> suggestions = suggestionService.getSuggestions(10, "en", null);

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

    @Test
    void shouldIncludeAiContextInPrompt() throws Exception {
        // Given
        String childId = "child1";
        String aiContext = "loves dinosaurs";
        com.fazquepaga.taskandpay.identity.User child =
                com.fazquepaga.taskandpay.identity.User.builder()
                        .id(childId)
                        .aiContext(aiContext)
                        .build();

        when(userRepository.findByIdSync(childId)).thenReturn(child);

        String suggestionText = "task 1, task 2, task 3";
        Generation generation = new Generation(new AssistantMessage(suggestionText));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        org.mockito.ArgumentCaptor<Prompt> promptCaptor =
                org.mockito.ArgumentCaptor.forClass(Prompt.class);
        when(chatModel.call(promptCaptor.capture())).thenReturn(chatResponse);

        // When
        suggestionService.getSuggestions(10, "en", childId);

        // Then
        String promptContent = promptCaptor.getValue().getContents();
        assertTrue(promptContent.contains("Context about the child: " + aiContext));
    }
}
