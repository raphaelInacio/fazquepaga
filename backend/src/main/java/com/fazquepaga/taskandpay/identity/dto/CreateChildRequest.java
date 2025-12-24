package com.fazquepaga.taskandpay.identity.dto;

import lombok.Data;

@Data
public class CreateChildRequest {
    private String name;
    private String parentId;
    private String phoneNumber;
    private Integer age;
    private String aiContext;
}
