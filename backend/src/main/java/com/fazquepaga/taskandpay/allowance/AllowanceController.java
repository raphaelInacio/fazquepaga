package com.fazquepaga.taskandpay.allowance;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/allowance")
public class AllowanceController {

    private final AllowanceService allowanceService;

    public AllowanceController(AllowanceService allowanceService) {
        this.allowanceService = allowanceService;
    }

    @GetMapping("/predicted")
    public ResponseEntity<Map<String, BigDecimal>> getPredictedAllowance(
            @RequestParam("child_id") String childId)
            throws ExecutionException, InterruptedException {

        BigDecimal predictedAllowance = allowanceService.calculatePredictedAllowance(childId);

        return ResponseEntity.ok(Map.of("predicted_allowance", predictedAllowance));
    }
}
