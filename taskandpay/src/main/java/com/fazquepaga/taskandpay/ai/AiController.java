package com.fazquepaga.taskandpay.ai;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
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
}
