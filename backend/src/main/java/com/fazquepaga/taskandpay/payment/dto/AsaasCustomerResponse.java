package com.fazquepaga.taskandpay.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsaasCustomerResponse {
    private String id;
    private String name;
    private String email;
}
