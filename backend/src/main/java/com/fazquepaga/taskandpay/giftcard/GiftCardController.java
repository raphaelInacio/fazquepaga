package com.fazquepaga.taskandpay.giftcard;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.fazquepaga.taskandpay.subscription.SubscriptionLimitReachedException;
import com.fazquepaga.taskandpay.subscription.SubscriptionService;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/giftcards")
@Slf4j
public class GiftCardController {

    private final GiftCardService giftCardService;
    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;

    public GiftCardController(
            GiftCardService giftCardService,
            SubscriptionService subscriptionService,
            UserRepository userRepository) {
        this.giftCardService = giftCardService;
        this.subscriptionService = subscriptionService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser()
            throws ExecutionException, InterruptedException {
        org.springframework.security.core.Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        return null;
    }

    @GetMapping({"", "/catalog"})
    public ResponseEntity<List<GiftCard>> getAvailableGiftCards()
            throws ExecutionException, InterruptedException {
        log.info("Fetching gift cards catalog");

        User user = getAuthenticatedUser();
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Check if user can access gift card store (must be premium)
        if (!subscriptionService.canAccessGiftCardStore(user)) {
            throw new SubscriptionLimitReachedException(
                    "Gift Card store is only available for Premium users. Upgrade to access!");
        }

        // Mock curated catalog approved for families (e.g. Roblox, iFood, PlayStation Store)
        List<GiftCard> giftCards =
                Arrays.asList(
                        GiftCard.builder()
                                .id("1")
                                .name("Roblox R$50")
                                .brand("Roblox")
                                .value(new BigDecimal("50.00"))
                                .description("50 Robux para usar no Roblox")
                                .build(),
                        GiftCard.builder()
                                .id("2")
                                .name("iFood R$30")
                                .brand("iFood")
                                .value(new BigDecimal("30.00"))
                                .description("Vale de R$30 para pedir comida")
                                .build(),
                        GiftCard.builder()
                                .id("3")
                                .name("PlayStation Store R$100")
                                .brand("PlayStation")
                                .value(new BigDecimal("100.00"))
                                .description("Crédito de R$100 para a PlayStation Store")
                                .build());

        return ResponseEntity.ok(giftCards);
    }

    @PostMapping("/requests")
    public ResponseEntity<GiftCardTransaction> requestGiftCard(
            @RequestBody CreateGiftCardRequest request)
            throws ExecutionException, InterruptedException {
        log.info("Creating gift card request");

        User child = getAuthenticatedUser();
        if (child == null) {
            throw new IllegalArgumentException("Child not found");
        }

        if (child.getRole() != User.Role.CHILD) {
            throw new IllegalArgumentException(
                    "Apenas dependentes podem solicitar resgates de Gift Cards.");
        }

        if (child.getParentId() == null || child.getParentId().isEmpty()) {
            throw new IllegalArgumentException("Dependente não possui responsável vinculado.");
        }

        GiftCardTransaction tx =
                giftCardService.requestGiftCard(
                        child.getId(),
                        child.getParentId(),
                        request.getProductId(),
                        request.getAmount());

        return ResponseEntity.status(HttpStatus.CREATED).body(tx);
    }

    @GetMapping("/requests")
    public ResponseEntity<List<GiftCardTransaction>> getGiftCardRequests()
            throws ExecutionException, InterruptedException {
        log.info("Listing gift card requests");

        User user = getAuthenticatedUser();
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        List<GiftCardTransaction> requests;
        if (user.getRole() == User.Role.CHILD) {
            requests = giftCardService.getTransactionsByChildId(user.getId());
        } else {
            requests = giftCardService.getTransactionsByParentId(user.getId());
        }

        return ResponseEntity.ok(requests);
    }

    @PostMapping("/requests/{id}/approve")
    public ResponseEntity<GiftCardTransaction> approveGiftCard(
            @PathVariable("id") String transactionId)
            throws ExecutionException, InterruptedException {
        log.info("Approving gift card request: {}", transactionId);

        User parent = getAuthenticatedUser();
        if (parent == null) {
            throw new IllegalArgumentException("Parent not found");
        }

        if (parent.getRole() != User.Role.PARENT) {
            throw new IllegalArgumentException("Apenas responsáveis podem aprovar resgates.");
        }

        GiftCardTransaction tx = giftCardService.approveGiftCard(parent.getId(), transactionId);
        return ResponseEntity.ok(tx);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateGiftCardRequest {
        private String productId;
        private BigDecimal amount;
    }
}
