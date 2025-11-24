package com.fazquepaga.taskandpay.allowance;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AllowanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AllowanceService allowanceService;

    @Test
    @WithMockUser(username = "parent@example.com", roles = "PARENT")
    void shouldGetPredictedAllowance() throws Exception {
        // Given
        String childId = "child-id";
        BigDecimal predictedAllowance = BigDecimal.valueOf(100.0);

        when(allowanceService.calculatePredictedAllowance(childId)).thenReturn(predictedAllowance);

        // When & Then
        mockMvc.perform(get("/api/v1/allowance/predicted").param("child_id", childId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.predicted_allowance").value(100.0));
    }
}