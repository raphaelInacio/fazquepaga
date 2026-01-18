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

    /**
     * Create a new AiController wiring the AI suggestion and quota services.
     *
     * @param suggestionService service that generates AI-powered suggestions and plans
     * @param aiQuotaService service responsible for verifying and recording per-user AI quota usage
     */
    public AiController(AiSuggestionService suggestionService, AIQuotaService aiQuotaService) {
        this.suggestionService = suggestionService;
        this.aiQuotaService = aiQuotaService;
    }

    /**
     * Provides a list of age-appropriate task suggestions for a child.
     *
     * @param age the child's age in years used to tailor suggestions
     * @param childId optional child identifier to customize suggestions; may be null
     * @param language requested language (Accept-Language header) used to localize suggestions
     * @param user authenticated user requesting suggestions
     * @return a list of task suggestion strings tailored to the given age and child context
     */
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

    /**
     * Generate a goal-coaching plan for a child and return it wrapped in a GoalCoachResponse.
     *
     * @param request  the goal coaching request containing `childId`, `goalDescription`, and `targetAmount`
     * @param language the language tag to use for generated content (defaults to "pt")
     * @return a GoalCoachResponse containing the generated plan; `imageUrl` is set to null
     * @throws ExecutionException   if plan generation fails during execution
     * @throws InterruptedException if plan generation is interrupted
     */
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

    /**
     * Generate adventure-mode tasks for the provided request and return them wrapped in an AdventureModeResponse.
     *
     * This endpoint enforces the calling user's AI quota before generating tasks and records quota usage after a successful generation.
     *
     * @param request the adventure mode request containing the input tasks to expand or transform
     * @param language the requested response language (e.g., "pt")
     * @param user the authenticated user making the request
     * @return an AdventureModeResponse containing the list of generated AdventureTask items
     */
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