package com.fazquepaga.taskandpay.tasks.dto;

import com.fazquepaga.taskandpay.tasks.Task.TaskType;
import com.fazquepaga.taskandpay.tasks.Task.TaskWeight;
import lombok.Data;

@Data
public class CreateTaskRequest {
    private String description;
    private TaskType type;
    private TaskWeight weight;
    // private BigDecimal value; ← REMOVED - will be calculated automatically
    private boolean requiresProof;
    private Integer dayOfWeek; // For WEEKLY tasks
    private java.time.Instant scheduledDate; // For ONE_TIME tasks
}
