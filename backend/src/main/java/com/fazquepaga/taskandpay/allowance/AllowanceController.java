package com.fazquepaga.taskandpay.allowance;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/allowance")
public class AllowanceController {

    private final AllowanceService allowanceService;
    private final LedgerService ledgerService;

    public AllowanceController(AllowanceService allowanceService, LedgerService ledgerService) {
        this.allowanceService = allowanceService;
        this.ledgerService = ledgerService;
    }

    @GetMapping("/predicted")
    public ResponseEntity<Map<String, BigDecimal>> getPredictedAllowance(
            @RequestParam("child_id") String childId)
            throws ExecutionException, InterruptedException {

        BigDecimal predictedAllowance = allowanceService.calculatePredictedAllowance(childId);

        return ResponseEntity.ok(Map.of("predicted_allowance", predictedAllowance));
    }

    @GetMapping("/children/{childId}/ledger")
    public ResponseEntity<LedgerResponse> getLedger(
            @PathVariable String childId, @RequestParam("parent_id") String parentId)
            throws ExecutionException, InterruptedException {
        LedgerResponse response = ledgerService.getTransactions(childId, parentId);
        return ResponseEntity.ok(response);
    }
}
