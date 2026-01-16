package com.fazquepaga.taskandpay.ai;

import com.fazquepaga.taskandpay.ai.dto.AdventureModeRequest;
import com.fazquepaga.taskandpay.ai.dto.AdventureModeResponse;
import com.fazquepaga.taskandpay.ai.dto.AdventureTask;
import com.fazquepaga.taskandpay.ai.dto.GoalCoachRequest;
import com.fazquepaga.taskandpay.ai.dto.GoalCoachResponse;
import com.fazquepaga.taskandpay.identity.User;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final AIQuotaService aiQuotaService;

    public AiController(AiSuggestionService suggestionService, AIQuotaService aiQuotaService) {
        this.suggestionService = suggestionService;
        this.aiQuotaService = aiQuotaService;
    }

    @GetMapping("/tasks/suggestions")
    public List<String> getTaskSuggestions(
            @RequestParam int age,
            @RequestParam(name = "child_id", required = false) String childId,
            @RequestHeader(value = "Accept-Language", defaultValue = "pt") String language,
            @AuthenticationPrincipal User user) {

        // Verify quota before AI call
        aiQuotaService.verifyQuotaOrThrow(user.getId());

        List<String> suggestions = suggestionService.getSuggestions(age, language, childId);

        // Record usage only after success
        aiQuotaService.recordUsage(user.getId());

        return suggestions;
    }

    @PostMapping("/goal-coach")
    public GoalCoachResponse getGoalCoachPlan(
            @RequestBody GoalCoachRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "pt") String language,
            @AuthenticationPrincipal User user)
            throws ExecutionException, InterruptedException {

        // Verify quota before AI call
        aiQuotaService.verifyQuotaOrThrow(user.getId());

        String plan = suggestionService.generateGoalPlan(
                request.getChildId(),
                request.getGoalDescription(),
                request.getTargetAmount(),
                language);

        // Record usage only after success
        aiQuotaService.recordUsage(user.getId());

        return GoalCoachResponse.builder()
                .plan(plan)
                .imageUrl(null)
                .build();
    }

    @PostMapping("/adventure-mode/tasks")
    public AdventureModeResponse getAdventureTasks(
            @RequestBody AdventureModeRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "pt") String language,
            @AuthenticationPrincipal User user) {

        // Verify quota before AI call
        aiQuotaService.verifyQuotaOrThrow(user.getId());

        List<AdventureTask> adventureTasks = suggestionService.generateAdventureTasks(request.getTasks(), language);

        // Record usage only after success
        aiQuotaService.recordUsage(user.getId());

        return AdventureModeResponse.builder().tasks(adventureTasks).build();
    }
}
