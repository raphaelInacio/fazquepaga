package com.fazquepaga.taskandpay.ai.dto;

import com.fazquepaga.taskandpay.tasks.Task;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdventureModeRequest {
    private List<Task> tasks;
}
