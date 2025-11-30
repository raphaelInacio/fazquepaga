package com.fazquepaga.taskandpay.ai;

import com.fazquepaga.taskandpay.ai.dto.AdventureTask;
import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.fazquepaga.taskandpay.tasks.Task;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

@Service
public class AiSuggestionService {

    private final ChatModel chatModel;
    private final UserRepository userRepository;

    public AiSuggestionService(ChatModel chatModel, UserRepository userRepository) {
        this.chatModel = chatModel;
        this.userRepository = userRepository;
    }

    public List<String> getSuggestions(int age) {
        PromptTemplate promptTemplate =
                new PromptTemplate(
                        """
                        Suggest a list of 5 simple and motivating household tasks for a {age}-year-old child.
                        Return the answer as a comma-separated list. For example:
                        task 1, task 2, task 3, task 4, task 5
                        """);
        Prompt prompt = promptTemplate.create(Map.of("age", age));
        ChatResponse response = chatModel.call(prompt);

        String content = response.getResult().getOutput().getText();
        return List.of(content.split(","));
    }

    public String generateGoalPlan(String childId, String goalDescription, BigDecimal targetAmount)
            throws ExecutionException, InterruptedException {

        // Get child info
        User child = userRepository.findByIdSync(childId);
        if (child == null) {
            throw new IllegalArgumentException("Child not found");
        }

        BigDecimal currentBalance =
                child.getBalance() != null ? child.getBalance() : BigDecimal.ZERO;
        BigDecimal monthlyAllowance =
                child.getMonthlyAllowance() != null ? child.getMonthlyAllowance() : BigDecimal.ZERO;

        PromptTemplate promptTemplate =
                new PromptTemplate(
                        """
                        You are a friendly financial coach for children. A child named {childName} wants to save for: {goalDescription}.

                        Goal amount: R$ {targetAmount}
                        Current balance: R$ {currentBalance}
                        Monthly allowance from tasks: R$ {monthlyAllowance}

                        Create a short, motivating plan (2-3 sentences) that:
                        1. Celebrates their goal
                        2. Estimates how long it will take
                        3. Gives one practical tip to reach the goal faster

                        Use simple, encouraging language appropriate for a child. Be enthusiastic and positive!
                        """);

        Map<String, Object> params =
                Map.of(
                        "childName", child.getName(),
                        "goalDescription", goalDescription,
                        "targetAmount", targetAmount.toString(),
                        "currentBalance", currentBalance.toString(),
                        "monthlyAllowance", monthlyAllowance.toString());

        Prompt prompt = promptTemplate.create(params);
        ChatResponse response = chatModel.call(prompt);

        return response.getResult().getOutput().getText();
    }

    public List<AdventureTask> generateAdventureTasks(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return new ArrayList<>();
        }

        // Build a prompt with all task descriptions
        StringBuilder taskList = new StringBuilder();
        for (int i = 0; i < tasks.size(); i++) {
            taskList.append((i + 1))
                    .append(". ")
                    .append(tasks.get(i).getDescription())
                    .append("\n");
        }

        PromptTemplate promptTemplate =
                new PromptTemplate(
                        """
                        Transform these household tasks into fun adventure quests for a child!
                        Make them exciting and game-like, but keep them short (max 5 words each).

                        Tasks:
                        {taskList}

                        Return ONLY the adventure versions, one per line, in the same order.
                        Examples:
                        - "Clean your room" -> "Conquer the Chaos Cave!"
                        - "Do homework" -> "Complete the Knowledge Quest!"
                        - "Wash dishes" -> "Defeat the Dirty Dishes Dragon!"
                        """);

        Prompt prompt = promptTemplate.create(Map.of("taskList", taskList.toString()));
        ChatResponse response = chatModel.call(prompt);

        String content = response.getResult().getOutput().getText();
        String[] adventureDescriptions = content.split("\n");

        List<AdventureTask> adventureTasks = new ArrayList<>();
        for (int i = 0; i < tasks.size() && i < adventureDescriptions.length; i++) {
            Task task = tasks.get(i);
            String adventureDesc = adventureDescriptions[i].trim();
            // Remove numbering if present (e.g., "1. " or "- ")
            adventureDesc = adventureDesc.replaceAll("^[0-9]+\\.\\s*", "").replaceAll("^-\\s*", "");

            adventureTasks.add(
                    AdventureTask.builder()
                            .id(task.getId())
                            .originalDescription(task.getDescription())
                            .adventureDescription(adventureDesc)
                            .value(task.getValue())
                            .status(task.getStatus().toString())
                            .build());
        }

        return adventureTasks;
    }
}
