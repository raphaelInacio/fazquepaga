package com.fazquepaga.taskandpay.ai;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import java.util.concurrent.ExecutionException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

@ExtendWith(MockitoExtension.class)
class AiSuggestionServiceTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AiSuggestionService aiSuggestionService;

    @Test
    void shouldIncludeChildContextInPrompt() throws ExecutionException, InterruptedException {
        // Given
        int age = 8;
        String language = "en";
        String childId = "child-123";
        String aiContext = "Loves dinosaurs.";

        User child = User.builder().id(childId).aiContext(aiContext).build();
        when(userRepository.findByIdSync(childId)).thenReturn(child);

        ChatResponse mockResponse = new ChatResponse(
                List.of(new Generation(new AssistantMessage("Task 1, Task 2, Task 3, Task 4, Task 5"))));
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // When
        aiSuggestionService.getSuggestions(age, language, childId);

        // Then
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());

        String promptText = promptCaptor.getValue().getContents();
        assertTrue(promptText.contains("Context about the child: " + aiContext));
    }

    @Test
    void shouldHandleMissingContextGracefully() throws ExecutionException, InterruptedException {
        // Given
        int age = 8;
        String language = "en";
        String childId = "child-123";

        User child = User.builder().id(childId).aiContext(null).build();
        when(userRepository.findByIdSync(childId)).thenReturn(child);

        ChatResponse mockResponse = new ChatResponse(
                List.of(new Generation(new AssistantMessage("Task 1, Task 2, Task 3, Task 4, Task 5"))));
        when(chatModel.call(any(Prompt.class))).thenReturn(mockResponse);

        // When
        aiSuggestionService.getSuggestions(age, language, childId);

        // Then
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());

        String promptText = promptCaptor.getValue().getContents();
        // Should NOT contain the context label if context is missing
        assertTrue(!promptText.contains("Context about the child:"));
    }
}
