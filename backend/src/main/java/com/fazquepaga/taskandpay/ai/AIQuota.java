package com.fazquepaga.taskandpay.ai;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents daily AI usage quota for a user.
 * Stored in Firestore at: users/{userId}/quotas/ai
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIQuota {
    private String userId;
    private int usedToday;
    private LocalDate lastResetDate;
    private int dailyLimit; // 5 (Free) or 10 (Premium)
}
