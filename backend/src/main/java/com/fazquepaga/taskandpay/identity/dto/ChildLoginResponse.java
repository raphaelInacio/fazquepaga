package com.fazquepaga.taskandpay.identity.dto;

import com.fazquepaga.taskandpay.identity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChildLoginResponse {
    private User child;
    private String token;
    private String refreshToken;
    private String message;
}
