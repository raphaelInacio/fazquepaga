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
    private final com.fazquepaga.taskandpay.identity.IdentityService identityService;

    public AllowanceController(
            AllowanceService allowanceService,
            LedgerService ledgerService,
            WithdrawalService withdrawalService,
            com.fazquepaga.taskandpay.identity.IdentityService identityService) {
        this.allowanceService = allowanceService;
        this.ledgerService = ledgerService;
        this.withdrawalService = withdrawalService;
        this.identityService = identityService;
    }

    private com.fazquepaga.taskandpay.identity.User getAuthenticatedUser() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof com.fazquepaga.taskandpay.identity.User) {
            return (com.fazquepaga.taskandpay.identity.User) auth.getPrincipal();
        }
        return null;
    }

    @GetMapping("/predicted")
    public ResponseEntity<Map<String, BigDecimal>> getPredictedAllowance(
            @RequestParam("child_id") String childId)
            throws ExecutionException, InterruptedException {
        com.fazquepaga.taskandpay.identity.User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        // Security check
        if (user.getRole() == com.fazquepaga.taskandpay.identity.User.Role.PARENT) {
            identityService.getChild(childId, user.getId());
        } else if (!user.getId().equals(childId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }

        BigDecimal predictedAllowance = allowanceService.calculatePredictedAllowance(childId);
        return ResponseEntity.ok(Map.of("predicted_allowance", predictedAllowance));
    }

    @GetMapping("/children/{childId}/ledger")
    public ResponseEntity<LedgerResponse> getLedger(
            @PathVariable String childId)
            throws ExecutionException, InterruptedException {
        com.fazquepaga.taskandpay.identity.User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        String parentId;
        if (user.getRole() == com.fazquepaga.taskandpay.identity.User.Role.PARENT) {
            parentId = user.getId();
            identityService.getChild(childId, parentId); // Validate ownership
        } else {
            if (!user.getId().equals(childId)) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
            }
            parentId = user.getParentId();
        }

        LedgerResponse response = ledgerService.getTransactions(childId, parentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/children/{childId}/ledger/insights")
    public ResponseEntity<Map<String, String>> getLedgerInsights(
            @PathVariable String childId)
            throws ExecutionException, InterruptedException {
        com.fazquepaga.taskandpay.identity.User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        String parentId;
        if (user.getRole() == com.fazquepaga.taskandpay.identity.User.Role.PARENT) {
            parentId = user.getId();
            identityService.getChild(childId, parentId);
        } else {
            if (!user.getId().equals(childId)) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
            }
            parentId = user.getParentId();
        }

        // Validate that the child belongs to the parent (redundant but safe)
        ledgerService.getTransactions(childId, parentId); 
        String insights = ledgerService.getInsights(childId);
        return ResponseEntity.ok(Map.of("insight", insights));
    }

    @org.springframework.web.bind.annotation.PostMapping("/children/{childId}/withdraw")
    public ResponseEntity<Transaction> requestWithdrawal(
            @PathVariable String childId,
            @org.springframework.web.bind.annotation.RequestBody Map<String, BigDecimal> body)
            throws ExecutionException, InterruptedException {
        com.fazquepaga.taskandpay.identity.User user = getAuthenticatedUser();
        if (user == null || user.getRole() != com.fazquepaga.taskandpay.identity.User.Role.CHILD || !user.getId().equals(childId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }

        BigDecimal amount = body.get("amount");
        Transaction transaction = withdrawalService.requestWithdrawal(childId, amount);
        return ResponseEntity.ok(transaction);
    }

    @org.springframework.web.bind.annotation.PostMapping("/withdrawals/{id}/approve")
    public ResponseEntity<Transaction> approveWithdrawal(
            @PathVariable String id,
            @org.springframework.web.bind.annotation.RequestBody Map<String, String> body)
            throws ExecutionException, InterruptedException {
        com.fazquepaga.taskandpay.identity.User parent = getAuthenticatedUser();
        if (parent == null || parent.getRole() != com.fazquepaga.taskandpay.identity.User.Role.PARENT) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        String proof = body.get("proof");
        Transaction transaction = withdrawalService.approveWithdrawal(parent.getId(), id, proof);
        return ResponseEntity.ok(transaction);
    }

    @org.springframework.web.bind.annotation.PostMapping("/withdrawals/{id}/reject")
    public ResponseEntity<Transaction> rejectWithdrawal(
            @PathVariable String id,
            @org.springframework.web.bind.annotation.RequestBody Map<String, String> body)
            throws ExecutionException, InterruptedException {
        com.fazquepaga.taskandpay.identity.User parent = getAuthenticatedUser();
        if (parent == null || parent.getRole() != com.fazquepaga.taskandpay.identity.User.Role.PARENT) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        String reason = body.get("reason");
        Transaction transaction = withdrawalService.rejectWithdrawal(parent.getId(), id, reason);
        return ResponseEntity.ok(transaction);
    }
}
