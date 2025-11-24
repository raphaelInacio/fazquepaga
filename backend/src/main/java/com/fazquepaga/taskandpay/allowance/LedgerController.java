package com.fazquepaga.taskandpay.allowance;

import com.fazquepaga.taskandpay.identity.UserRepository;
import java.security.Principal;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class LedgerController {

    private final LedgerService ledgerService;
    private final UserRepository userRepository; // To verify parent-child relationship

    public LedgerController(LedgerService ledgerService, UserRepository userRepository) {
        this.ledgerService = ledgerService;
        this.userRepository = userRepository;
    }

    @GetMapping("/children/{childId}/ledger")
    public ResponseEntity<List<Transaction>> getChildLedger(
            @PathVariable String childId, Principal principal)
            throws ExecutionException, InterruptedException {

        // Validate that the authenticated parent owns the child
        if (!userRepository.findByIdSync(childId).getParentId().equals(principal.getName())) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        List<Transaction> ledger = ledgerService.getTransactions(childId, principal.getName());
        return ResponseEntity.ok(ledger);
    }
}
