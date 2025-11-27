package com.fazquepaga.taskandpay.tasks.dto;

import com.fazquepaga.taskandpay.tasks.Task.TaskType;
import com.fazquepaga.taskandpay.tasks.Task.TaskWeight;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CreateTaskRequest {
    private String description;
    private TaskType type;
    private TaskWeight weight;
    private BigDecimal value; // Valor monetário da tarefa em R$
    private boolean requiresProof;
    private Integer dayOfWeek;
    private java.time.Instant scheduledDate;
}
