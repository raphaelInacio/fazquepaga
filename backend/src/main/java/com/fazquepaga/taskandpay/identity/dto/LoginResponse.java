package com.fazquepaga.taskandpay.identity.dto;

import com.fazquepaga.taskandpay.identity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String token;
    private User user;
}
