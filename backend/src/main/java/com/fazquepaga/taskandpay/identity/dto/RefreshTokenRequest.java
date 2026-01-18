package com.fazquepaga.taskandpay.identity.dto;

import lombok.Data;

/**
 * Request DTO for refresh token endpoint.
 */
@Data
public class RefreshTokenRequest {
    private String refreshToken;
}
