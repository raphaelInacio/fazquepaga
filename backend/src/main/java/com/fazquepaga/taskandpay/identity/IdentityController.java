package com.fazquepaga.taskandpay.identity;

import com.fazquepaga.taskandpay.identity.dto.CreateChildRequest;
import com.fazquepaga.taskandpay.identity.dto.CreateParentRequest;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class IdentityController {

    private final IdentityService identityService;

    public IdentityController(IdentityService identityService) {

        this.identityService = identityService;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<User> registerParent(@RequestBody CreateParentRequest request)
            throws ExecutionException, InterruptedException {

        User registeredParent = identityService.registerParent(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(registeredParent);
    }

    @PostMapping("/children")
    public ResponseEntity<User> createChild(@RequestBody CreateChildRequest request)
            throws ExecutionException, InterruptedException {

        User createdChild = identityService.createChild(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdChild);
    }

    @PostMapping("/children/{childId}/onboarding-code")
    public ResponseEntity<Map<String, String>> generateOnboardingCode(
            @PathVariable String childId) {

        String code = identityService.generateOnboardingCode(childId);

        return ResponseEntity.ok(Map.of("code", code));
    }

    @PostMapping("/children/{childId}/allowance")
    public ResponseEntity<User> updateChildAllowance(
            @PathVariable String childId, @RequestBody Map<String, java.math.BigDecimal> request)
            throws ExecutionException, InterruptedException {

        java.math.BigDecimal allowance = request.get("allowance");
        User updatedChild = identityService.updateChildAllowance(childId, allowance);

        return ResponseEntity.ok(updatedChild);
    }
}
