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
import org.springframework.web.bind.annotation.RequestHeader;
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
    public List<String> getTaskSuggestions(
            @RequestParam int age,
            @RequestParam(name = "child_id", required = false) String childId,
            @RequestHeader(value = "Accept-Language", defaultValue = "pt") String language) {
        return suggestionService.getSuggestions(age, language, childId);
    }

    @PostMapping("/goal-coach")
    public GoalCoachResponse getGoalCoachPlan(
            @RequestBody GoalCoachRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "pt") String language)
            throws ExecutionException, InterruptedException {
        String plan =
                suggestionService.generateGoalPlan(
                        request.getChildId(),
                        request.getGoalDescription(),
                        request.getTargetAmount(),
                        language);

        return GoalCoachResponse.builder()
                .plan(plan)
                .imageUrl(null) // TODO: Implement AI image generation in future iteration
                .build();
    }

    @PostMapping("/adventure-mode/tasks")
    public AdventureModeResponse getAdventureTasks(
            @RequestBody AdventureModeRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "pt") String language) {
        List<AdventureTask> adventureTasks =
                suggestionService.generateAdventureTasks(request.getTasks(), language);

        return AdventureModeResponse.builder().tasks(adventureTasks).build();
    }
}
