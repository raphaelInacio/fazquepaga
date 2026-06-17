package com.fazquepaga.taskandpay.identity;

import com.fazquepaga.taskandpay.identity.dto.ChildLoginRequest;
import com.fazquepaga.taskandpay.identity.dto.ChildLoginResponse;
import com.fazquepaga.taskandpay.identity.dto.CreateChildRequest;
import com.fazquepaga.taskandpay.identity.dto.CreateParentRequest;
import com.fazquepaga.taskandpay.identity.dto.RefreshTokenRequest;
import com.fazquepaga.taskandpay.identity.dto.UpdateChildRequest;
import com.fazquepaga.taskandpay.security.RecaptchaException;
import com.fazquepaga.taskandpay.security.RecaptchaService;
import com.fazquepaga.taskandpay.security.RefreshTokenService;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class IdentityController {

    private final IdentityService identityService;
    private final com.fazquepaga.taskandpay.security.JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RecaptchaService recaptchaService;

    public IdentityController(
            IdentityService identityService,
            com.fazquepaga.taskandpay.security.JwtService jwtService,
            RefreshTokenService refreshTokenService,
            RecaptchaService recaptchaService) {
        this.identityService = identityService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.recaptchaService = recaptchaService;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<User> registerParent(@RequestBody CreateParentRequest request)
            throws ExecutionException, InterruptedException {
        // Verify reCAPTCHA token
        if (!recaptchaService.verify(request.getRecaptchaToken(), "register")) {
            throw new RecaptchaException("reCAPTCHA verification failed");
        }
        User parent = identityService.registerParent(request);
        // We could return a token here too, but for now let's stick to returning User
        // and requiring login.
        return ResponseEntity.status(HttpStatus.CREATED).body(parent);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<com.fazquepaga.taskandpay.identity.dto.LoginResponse> login(
            @RequestBody com.fazquepaga.taskandpay.identity.dto.LoginRequest request)
            throws ExecutionException, InterruptedException {
        // Verify reCAPTCHA token
        if (!recaptchaService.verify(request.getRecaptchaToken(), "login")) {
            throw new RecaptchaException("reCAPTCHA verification failed");
        }
        User user = identityService.authenticateParent(request.getEmail(), request.getPassword());
        String token = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getId());
        return ResponseEntity.ok(
                com.fazquepaga.taskandpay.identity.dto.LoginResponse.builder()
                        .token(token)
                        .refreshToken(refreshToken)
                        .user(user)
                        .build());
    }

    @PostMapping("/children/login")
    public ResponseEntity<ChildLoginResponse> childLogin(@RequestBody ChildLoginRequest request)
            throws ExecutionException, InterruptedException {
        // Verify reCAPTCHA token
        if (!recaptchaService.verify(request.getRecaptchaToken(), "child_login")) {
            throw new RecaptchaException("reCAPTCHA verification failed");
        }
        User child = identityService.authenticateChildByCode(request.getCode());
        String token =
                jwtService.generateToken(
                        child.getId(), child.getId(), "CHILD"); // Simplified token for child
        String refreshToken = refreshTokenService.createRefreshToken(child.getId());
        ChildLoginResponse response =
                ChildLoginResponse.builder()
                        .child(child)
                        .token(token)
                        .refreshToken(refreshToken)
                        .message("Login successful")
                        .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(
            @RequestBody RefreshTokenRequest request) {
        Optional<String> newToken =
                refreshTokenService.validateAndRefresh(request.getRefreshToken());
        if (newToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired refresh token"));
        }
        return ResponseEntity.ok(Map.of("token", newToken.get()));
    }

    @PostMapping("/auth/logout-all")
    public ResponseEntity<Map<String, String>> logoutAll(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }
        refreshTokenService.revokeAllTokens(principal.getName());
        return ResponseEntity.ok(Map.of("message", "All sessions logged out"));
    }

    private User getAuthenticatedUser() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        return null;
    }

    @PostMapping("/children")
    public ResponseEntity<User> createChild(
            @RequestBody CreateChildRequest request)
            throws ExecutionException, InterruptedException {
        User parent = getAuthenticatedUser();
        if (parent == null || parent.getRole() != User.Role.PARENT) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Enforce that the child is created for the authenticated parent
        request.setParentId(parent.getId());
        User child = identityService.createChild(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(child);
    }

    @GetMapping("/children")
    public ResponseEntity<List<User>> getChildren()
            throws ExecutionException, InterruptedException {
        User parent = getAuthenticatedUser();
        if (parent == null || parent.getRole() != User.Role.PARENT) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<User> children = identityService.getChildren(parent.getId());
        return ResponseEntity.ok(children);
    }

    @GetMapping("/children/{childId}")
    public ResponseEntity<User> getChild(
            @PathVariable String childId)
            throws ExecutionException, InterruptedException {
        User parent = getAuthenticatedUser();
        if (parent == null || parent.getRole() != User.Role.PARENT) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User child = identityService.getChild(childId, parent.getId());
        return ResponseEntity.ok(child);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUser(@PathVariable String userId)
            throws ExecutionException, InterruptedException {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Security check: only allow if it's the user themselves OR their child
        if (!currentUser.getId().equals(userId)) {
            try {
                identityService.getChild(userId, currentUser.getId());
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        User user = identityService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/children/{childId}/onboarding-code")
    public ResponseEntity<Map<String, String>> generateOnboardingCode(
            @PathVariable String childId)
            throws ExecutionException, InterruptedException {
        User parent = getAuthenticatedUser();
        if (parent == null || parent.getRole() != User.Role.PARENT) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Validate ownership
        identityService.getChild(childId, parent.getId());

        String code = identityService.generateOnboardingCode(childId);
        return ResponseEntity.ok(Map.of("code", code));
    }

    @PostMapping("/children/{childId}/allowance")
    public ResponseEntity<User> updateChildAllowance(
            @PathVariable String childId,
            @RequestBody Map<String, java.math.BigDecimal> request)
            throws ExecutionException, InterruptedException {
        User parent = getAuthenticatedUser();
        if (parent == null || parent.getRole() != User.Role.PARENT) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Validate ownership
        identityService.getChild(childId, parent.getId());

        User updatedChild = identityService.updateChildAllowance(childId, request.get("allowance"));
        return ResponseEntity.ok(updatedChild);
    }

    @PutMapping("/children/{childId}")
    public ResponseEntity<User> updateChild(
            @PathVariable String childId,
            @RequestBody UpdateChildRequest request)
            throws ExecutionException, InterruptedException {
        User parent = getAuthenticatedUser();
        if (parent == null || parent.getRole() != User.Role.PARENT) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User updatedChild = identityService.updateChild(childId, request, parent.getId());
        return ResponseEntity.ok(updatedChild);
    }

    @DeleteMapping("/children/{childId}")
    public ResponseEntity<Void> deleteChild(
            @PathVariable String childId)
            throws ExecutionException, InterruptedException {
        User parent = getAuthenticatedUser();
        if (parent == null || parent.getRole() != User.Role.PARENT) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        identityService.deleteChild(childId, parent.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/children/{childId}/context")
    public ResponseEntity<User> updateAiContext(
            @PathVariable String childId,
            @RequestBody com.fazquepaga.taskandpay.identity.dto.UpdateAiContextRequest request)
            throws ExecutionException, InterruptedException {
        User parent = getAuthenticatedUser();
        if (parent == null || parent.getRole() != User.Role.PARENT) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User updatedChild =
                identityService.updateAiContext(childId, request.getContext(), parent.getId());
        return ResponseEntity.ok(updatedChild);
    }
}
