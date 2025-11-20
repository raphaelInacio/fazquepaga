package com.fazquepaga.taskandpay.whatsapp.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProofSubmittedEvent {
    private String childId;
    private String taskId;
    private String imageUrl;
}
