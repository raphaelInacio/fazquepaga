package com.fazquepaga.taskandpay.ai;

import java.util.Map;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

@Service
public class AiValidatorImpl implements AiValidator {

    private final ChatModel chatModel;

    public AiValidatorImpl(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public boolean validateTaskCompletionImage(byte[] image, String taskDescription) {
        // This is a simplified implementation. A real one would need to handle image
        // bytes.
        // Spring AI support for multimodal input is still evolving.
        // For now, we will just simulate the call.

        PromptTemplate promptTemplate =
                new PromptTemplate(
                        """
                This image is a proof of completion for the task: '{taskDescription}'.
                Does the image contain a representation of this task being completed?
                Answer only with 'yes' or 'no'.
                """);
        Prompt prompt = promptTemplate.create(Map.of("taskDescription", taskDescription));
        ChatResponse response = chatModel.call(prompt);

        String content = response.getResult().getOutput().getText();
        return "yes".equalsIgnoreCase(content.trim());
    }
}
