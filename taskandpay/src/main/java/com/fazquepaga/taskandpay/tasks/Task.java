package com.fazquepaga.taskandpay.tasks;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @DocumentId
    private String id;

    private String description;
    private TaskType type;
    private TaskWeight weight;
    private TaskStatus status;
    private boolean requiresProof;
    private Instant createdAt;
    private Integer dayOfWeek; // 1 (Mon) to 7 (Sun) for WEEKLY
    private Instant scheduledDate; // For ONE_TIME

    public enum TaskType {
        DAILY,
        WEEKLY,
        ONE_TIME
    }

    public enum TaskWeight {
        LOW,
        MEDIUM,
        HIGH
    }

    public enum TaskStatus {
        PENDING,
        COMPLETED,
        APPROVED
    }
}