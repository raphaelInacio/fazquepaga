package com.fazquepaga.taskandpay.ai;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AiSuggestionService {

    private final ChatClient chatClient;

    public AiSuggestionService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public List<String> getSuggestions(int age) {
        PromptTemplate promptTemplate = new PromptTemplate("""
                Suggest a list of 5 simple and motivating household tasks for a {age}-year-old child.
                Return the answer as a comma-separated list. For example:
                task 1, task 2, task 3, task 4, task 5
                """);
        Prompt prompt = promptTemplate.create(Map.of("age", age));
        ChatResponse response = chatClient.call(prompt);

        String content = response.getResult().getOutput().getContent();
        return List.of(content.split(","));
    }
}
