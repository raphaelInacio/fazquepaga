package com.fazquepaga.taskandpay.ai;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AiController.class)
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiSuggestionService suggestionService;

    @Test
    void shouldReturnTaskSuggestionsForAge() throws Exception {
        // Given
        int age = 10;
        List<String> suggestions = Arrays.asList(
                "Make your bed",
                "Clean your room",
                "Do homework",
                "Feed the pet",
                "Water the plants");

        when(suggestionService.getSuggestions(age)).thenReturn(suggestions);

        // When & Then
        mockMvc.perform(get("/api/v1/ai/tasks/suggestions").param("age", String.valueOf(age)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0]").value("Make your bed"))
                .andExpect(jsonPath("$[1]").value("Clean your room"))
                .andExpect(jsonPath("$[2]").value("Do homework"));
    }

    @Test
    void shouldReturnSuggestionsForYoungerChild() throws Exception {
        // Given
        int age = 5;
        List<String> suggestions = Arrays.asList("Put toys away", "Brush teeth", "Help set table");

        when(suggestionService.getSuggestions(age)).thenReturn(suggestions);

        // When & Then
        mockMvc.perform(get("/api/v1/ai/tasks/suggestions").param("age", String.valueOf(age)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void shouldReturnSuggestionsForOlderChild() throws Exception {
        // Given
        int age = 15;
        List<String> suggestions = Arrays.asList("Mow the lawn", "Wash the car", "Cook a meal", "Do laundry");

        when(suggestionService.getSuggestions(age)).thenReturn(suggestions);

        // When & Then
        mockMvc.perform(get("/api/v1/ai/tasks/suggestions").param("age", String.valueOf(age)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void shouldHandleEmptySuggestionsList() throws Exception {
        // Given
        int age = 10;
        when(suggestionService.getSuggestions(anyInt())).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/v1/ai/tasks/suggestions").param("age", String.valueOf(age)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
