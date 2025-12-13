package com.fazquepaga.taskandpay.identity;

import com.fazquepaga.taskandpay.identity.dto.ChildLoginRequest;
import com.fazquepaga.taskandpay.identity.dto.ChildLoginResponse;
import com.fazquepaga.taskandpay.identity.dto.CreateChildRequest;
import com.fazquepaga.taskandpay.identity.dto.CreateParentRequest;
import com.fazquepaga.taskandpay.identity.dto.UpdateChildRequest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        User parent = identityService.registerParent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(parent);
    }

    @PostMapping("/children/login")
    public ResponseEntity<ChildLoginResponse> childLogin(@RequestBody ChildLoginRequest request)
            throws ExecutionException, InterruptedException {
        User child = identityService.authenticateChildByCode(request.getCode());
        ChildLoginResponse response = ChildLoginResponse.builder().child(child).message("Login successful").build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/children")
    public ResponseEntity<User> createChild(
            @RequestBody CreateChildRequest request, @RequestParam("parent_id") String parentId)
            throws ExecutionException, InterruptedException {
        // Enforce that the child is created for the authenticated parent
        request.setParentId(parentId);
        User child = identityService.createChild(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(child);
    }

    @GetMapping("/children")
    public ResponseEntity<List<User>> getChildren(@RequestParam("parent_id") String parentId)
            throws ExecutionException, InterruptedException {
        List<User> children = identityService.getChildren(parentId);
        return ResponseEntity.ok(children);
    }

    @GetMapping("/children/{childId}")
    public ResponseEntity<User> getChild(
            @PathVariable String childId, @RequestParam("parent_id") String parentId)
            throws ExecutionException, InterruptedException {
        User child = identityService.getChild(childId, parentId);
        return ResponseEntity.ok(child);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUser(@PathVariable String userId)
            throws ExecutionException, InterruptedException {
        User user = identityService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/children/{childId}/onboarding-code")
    public ResponseEntity<Map<String, String>> generateOnboardingCode(
            @PathVariable String childId, @RequestParam("parent_id") String parentId)
            throws ExecutionException, InterruptedException {
        // Validate ownership
        identityService.getChild(childId, parentId);

        String code = identityService.generateOnboardingCode(childId);
        return ResponseEntity.ok(Map.of("code", code));
    }

    @PostMapping("/children/{childId}/allowance")
    public ResponseEntity<User> updateChildAllowance(
            @PathVariable String childId,
            @RequestBody Map<String, java.math.BigDecimal> request,
            @RequestParam("parent_id") String parentId)
            throws ExecutionException, InterruptedException {
        // Validate ownership
        identityService.getChild(childId, parentId);

        User updatedChild = identityService.updateChildAllowance(childId, request.get("allowance"));
        return ResponseEntity.ok(updatedChild);
    }

    @PutMapping("/children/{childId}")
    public ResponseEntity<User> updateChild(
            @PathVariable String childId,
            @RequestBody UpdateChildRequest request,
            @RequestParam("parent_id") String parentId)
            throws ExecutionException, InterruptedException {
        User updatedChild = identityService.updateChild(childId, request, parentId);
        return ResponseEntity.ok(updatedChild);
    }

    @DeleteMapping("/children/{childId}")
    public ResponseEntity<Void> deleteChild(
            @PathVariable String childId,
            @RequestParam("parent_id") String parentId)
            throws ExecutionException, InterruptedException {
        identityService.deleteChild(childId, parentId);
        return ResponseEntity.noContent().build();
    }
}
