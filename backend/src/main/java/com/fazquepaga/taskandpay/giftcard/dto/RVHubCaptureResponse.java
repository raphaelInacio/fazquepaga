package com.fazquepaga.taskandpay.giftcard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RVHubCaptureResponse {
    private String id;
    private String status;
    private PinInfo pin;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PinInfo {
        private String code;

        @JsonProperty("pin_code")
        private String pinCode;

        private String serial;
        private String instructions;
    }
}
