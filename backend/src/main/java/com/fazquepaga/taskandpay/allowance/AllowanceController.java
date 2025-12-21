package com.fazquepaga.taskandpay.allowance;

import java.math.BigDecimal;
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
    private final WithdrawalService withdrawalService;

    public AllowanceController(AllowanceService allowanceService, LedgerService ledgerService,
            WithdrawalService withdrawalService) {
        this.allowanceService = allowanceService;
        this.ledgerService = ledgerService;
        this.withdrawalService = withdrawalService;
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

    @GetMapping("/children/{childId}/ledger/insights")
    public ResponseEntity<Map<String, String>> getLedgerInsights(
            @PathVariable String childId, @RequestParam("parent_id") String parentId)
            throws ExecutionException, InterruptedException {
        // Validate that the child belongs to the parent
        ledgerService.getTransactions(childId, parentId); // This will throw if unauthorized
        String insights = ledgerService.getInsights(childId);
        return ResponseEntity.ok(Map.of("insight", insights));
    }

    @org.springframework.web.bind.annotation.PostMapping("/children/{childId}/withdraw")
    public ResponseEntity<Transaction> requestWithdrawal(
            @PathVariable String childId,
            @org.springframework.web.bind.annotation.RequestBody Map<String, BigDecimal> body)
            throws ExecutionException, InterruptedException {
        BigDecimal amount = body.get("amount");
        Transaction transaction = withdrawalService.requestWithdrawal(childId, amount);
        return ResponseEntity.ok(transaction);
    }

    @org.springframework.web.bind.annotation.PostMapping("/withdrawals/{id}/approve")
    public ResponseEntity<Transaction> approveWithdrawal(
            @PathVariable String id,
            @RequestParam("parent_id") String parentId,
            @org.springframework.web.bind.annotation.RequestBody Map<String, String> body)
            throws ExecutionException, InterruptedException {
        String proof = body.get("proof");
        Transaction transaction = withdrawalService.approveWithdrawal(parentId, id, proof);
        return ResponseEntity.ok(transaction);
    }

    @org.springframework.web.bind.annotation.PostMapping("/withdrawals/{id}/reject")
    public ResponseEntity<Transaction> rejectWithdrawal(
            @PathVariable String id,
            @RequestParam("parent_id") String parentId,
            @org.springframework.web.bind.annotation.RequestBody Map<String, String> body)
            throws ExecutionException, InterruptedException {
        String reason = body.get("reason");
        Transaction transaction = withdrawalService.rejectWithdrawal(parentId, id, reason);
        return ResponseEntity.ok(transaction);
    }
}
