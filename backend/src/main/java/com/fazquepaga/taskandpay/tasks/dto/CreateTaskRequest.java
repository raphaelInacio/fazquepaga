package com.fazquepaga.taskandpay.tasks.dto;

import com.fazquepaga.taskandpay.tasks.Task;
import lombok.Data;

@Data
public class CreateTaskRequest {
    private String description;
    private Task.TaskType type;
    private Task.TaskWeight weight;
    private boolean requiresProof;
    private Integer dayOfWeek;
    private java.time.Instant scheduledDate;
}
