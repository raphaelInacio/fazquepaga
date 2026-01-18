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

    /**
     * Creates an IdentityController configured with the required service dependencies.
     *
     * Initializes the controller with the identity management, JWT, refresh-token, and reCAPTCHA services.
     */
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

    /**
     * Register a new parent user after verifying the provided reCAPTCHA token.
     *
     * @param request the registration payload containing parent details and the reCAPTCHA token
     * @return the created User wrapped in a ResponseEntity with HTTP 201 Created
     * @throws RecaptchaException   if reCAPTCHA verification fails
     * @throws ExecutionException   if an error occurs during asynchronous reCAPTCHA verification
     * @throws InterruptedException if the reCAPTCHA verification thread is interrupted
     */
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

    /**
     * Authenticate a parent using credentials, verify reCAPTCHA, and issue an access token and refresh token.
     *
     * @param request the login request containing the parent's email, password, and reCAPTCHA token
     * @return a LoginResponse containing the JWT access token, a refresh token, and the authenticated User
     * @throws RecaptchaException   if reCAPTCHA verification fails
     * @throws ExecutionException   if an asynchronous operation involved in authentication or token creation fails
     * @throws InterruptedException if an asynchronous operation is interrupted
     */
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

    /**
     * Authenticate a child by onboarding code, verify reCAPTCHA, and return session tokens.
     *
     * @param request contains the child's onboarding code and the reCAPTCHA token
     * @return a ChildLoginResponse containing the authenticated child, a JWT access token,
     *         a refresh token, and a success message
     * @throws ExecutionException if an error occurs during reCAPTCHA verification or related async processing
     * @throws InterruptedException if reCAPTCHA verification or related async processing is interrupted
     */
    @PostMapping("/children/login")
    public ResponseEntity<ChildLoginResponse> childLogin(@RequestBody ChildLoginRequest request)
            throws ExecutionException, InterruptedException {
        // Verify reCAPTCHA token
        if (!recaptchaService.verify(request.getRecaptchaToken(), "child_login")) {
            throw new RecaptchaException("reCAPTCHA verification failed");
        }
        User child = identityService.authenticateChildByCode(request.getCode());
        String token = jwtService.generateToken(
                child.getId(), child.getId(), "CHILD"); // Simplified token for child
        String refreshToken = refreshTokenService.createRefreshToken(child.getId());
        ChildLoginResponse response = ChildLoginResponse.builder()
                .child(child)
                .token(token)
                .refreshToken(refreshToken)
                .message("Login successful")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Exchange a refresh token for a new access token.
     *
     * @param request contains the refresh token to validate and exchange
     * @return a map with key `token` containing the new access token on success (HTTP 200),
     *         or key `error` with an error message on failure (HTTP 401)
     */
    @PostMapping("/auth/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(
            @RequestBody RefreshTokenRequest request) {
        Optional<String> newToken = refreshTokenService.validateAndRefresh(request.getRefreshToken());
        if (newToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired refresh token"));
        }
        return ResponseEntity.ok(Map.of("token", newToken.get()));
    }

    /**
     * Log out all refresh-token sessions for the currently authenticated user.
     *
     * Returns HTTP 200 with a map containing "message" on successful logout, or HTTP 401 with a map containing "error" when no authenticated principal is present.
     *
     * @param principal the authenticated principal (may be null for unauthenticated requests)
     * @return a map with a "message" key on success or an "error" key on unauthorized requests
     */
    @PostMapping("/auth/logout-all")
    public ResponseEntity<Map<String, String>> logoutAll(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }
        refreshTokenService.revokeAllTokens(principal.getName());
        return ResponseEntity.ok(Map.of("message", "All sessions logged out"));
    }

    /**
     * Create a child user associated with the specified parent.
     *
     * @param request  the child's creation data; the child will be associated with the provided parentId
     * @param parentId the identifier of the parent who will own the new child
     * @return the created child User
     * @throws ExecutionException   if an error occurs during asynchronous processing
     * @throws InterruptedException if the operation is interrupted
     */
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
            @PathVariable String childId, @RequestParam("parent_id") String parentId)
            throws ExecutionException, InterruptedException {
        identityService.deleteChild(childId, parentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update a child's AI context after verifying the provided parent owns the child.
     *
     * @param childId  the ID of the child whose AI context will be updated
     * @param request  request containing the new AI context
     * @param parentId the ID of the parent used to verify ownership before applying the update
     * @return the updated child User
     * @throws ExecutionException   if the asynchronous update operation fails
     * @throws InterruptedException if the update operation is interrupted
     */
    @PatchMapping("/children/{childId}/context")
    public ResponseEntity<User> updateAiContext(
            @PathVariable String childId,
            @RequestBody com.fazquepaga.taskandpay.identity.dto.UpdateAiContextRequest request,
            @RequestParam("parent_id") String parentId)
            throws ExecutionException, InterruptedException {
        User updatedChild = identityService.updateAiContext(childId, request.getContext(), parentId);
        return ResponseEntity.ok(updatedChild);
    }
}