package com.fazquepaga.taskandpay.ai;

public interface AiValidator {
    boolean validateTaskCompletionImage(byte[] image, String taskDescription);
}
