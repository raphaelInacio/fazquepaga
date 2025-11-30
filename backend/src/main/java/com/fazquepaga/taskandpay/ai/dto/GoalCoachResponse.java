package com.fazquepaga.taskandpay.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalCoachResponse {
    private String plan;
    private String imageUrl; // Optional for MVP
}
