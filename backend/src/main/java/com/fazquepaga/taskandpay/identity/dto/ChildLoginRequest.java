package com.fazquepaga.taskandpay.identity.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChildLoginRequest {
    private String code;
    private String recaptchaToken;
}
