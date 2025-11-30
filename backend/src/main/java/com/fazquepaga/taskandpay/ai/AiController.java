package com.fazquepaga.taskandpay.ai;

import com.fazquepaga.taskandpay.ai.dto.AdventureModeRequest;
import com.fazquepaga.taskandpay.ai.dto.AdventureModeResponse;
import com.fazquepaga.taskandpay.ai.dto.AdventureTask;
import com.fazquepaga.taskandpay.ai.dto.GoalCoachRequest;
import com.fazquepaga.taskandpay.ai.dto.GoalCoachResponse;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiSuggestionService suggestionService;

    public AiController(AiSuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    @GetMapping("/tasks/suggestions")
    public List<String> getTaskSuggestions(@RequestParam int age) {
        return suggestionService.getSuggestions(age);
    }

    @PostMapping("/goal-coach")
    public GoalCoachResponse getGoalCoachPlan(@RequestBody GoalCoachRequest request)
            throws ExecutionException, InterruptedException {
        String plan =
                suggestionService.generateGoalPlan(
                        request.getChildId(),
                        request.getGoalDescription(),
                        request.getTargetAmount());

        return GoalCoachResponse.builder()
                .plan(plan)
                .imageUrl(null) // TODO: Implement AI image generation in future iteration
                .build();
    }

    @PostMapping("/adventure-mode/tasks")
    public AdventureModeResponse getAdventureTasks(@RequestBody AdventureModeRequest request) {
        List<AdventureTask> adventureTasks =
                suggestionService.generateAdventureTasks(request.getTasks());

        return AdventureModeResponse.builder().tasks(adventureTasks).build();
    }
}
