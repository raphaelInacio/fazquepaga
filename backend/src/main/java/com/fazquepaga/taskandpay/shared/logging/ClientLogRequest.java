package com.fazquepaga.taskandpay.shared.logging;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record ClientLogRequest(
        @NotBlank String message,
        String stack,
        @NotBlank String component,
        @NotBlank String requestUri,
        Map<String, Object> metadata) {}
