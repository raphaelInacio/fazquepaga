package com.fazquepaga.taskandpay.payment.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AsaasCustomerRequest {
    private String name;
    private String cpfCnpj;
    private String email;
    private String mobilePhone;
    private String externalReference;
    private boolean notificationDisabled;
}
