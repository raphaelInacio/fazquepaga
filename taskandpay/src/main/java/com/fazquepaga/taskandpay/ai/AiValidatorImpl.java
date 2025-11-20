package com.fazquepaga.taskandpay.ai;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AiValidatorImpl implements AiValidator {

    private final ChatClient chatClient;

    public AiValidatorImpl(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public boolean validateTaskCompletionImage(byte[] image, String taskDescription) {
        // This is a simplified implementation. A real one would need to handle image bytes.
        // Spring AI support for multimodal input is still evolving.
        // For now, we will just simulate the call.

        PromptTemplate promptTemplate = new PromptTemplate("""
                This image is a proof of completion for the task: '{taskDescription}'.
                Does the image contain a representation of this task being completed?
                Answer only with 'yes' or 'no'.
                """);
        Prompt prompt = promptTemplate.create(Map.of("taskDescription", taskDescription));
        ChatResponse response = chatClient.call(prompt);

        String content = response.getResult().getOutput().getContent();
        return "yes".equalsIgnoreCase(content.trim());
    }
}
