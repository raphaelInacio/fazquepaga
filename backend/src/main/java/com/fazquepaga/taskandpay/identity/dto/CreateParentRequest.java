package com.fazquepaga.taskandpay.identity.dto;

import lombok.Data;

@Data
public class CreateParentRequest {
    private String name;
    private String email;
    private String phoneNumber;
    private String password;
    private String recaptchaToken;
}
