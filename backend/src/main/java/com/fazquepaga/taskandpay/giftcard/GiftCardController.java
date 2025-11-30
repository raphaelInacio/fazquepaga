package com.fazquepaga.taskandpay.giftcard;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.fazquepaga.taskandpay.subscription.SubscriptionLimitReachedException;
import com.fazquepaga.taskandpay.subscription.SubscriptionService;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Controller for Gift Card operations (Mock implementation for MVP). */
@RestController
@RequestMapping("/api/v1/giftcards")
@lombok.extern.slf4j.Slf4j
public class GiftCardController {

    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;

    public GiftCardController(
            SubscriptionService subscriptionService, UserRepository userRepository) {
        this.subscriptionService = subscriptionService;
        this.userRepository = userRepository;
    }

    /** Get available gift cards (Mock data). */
    @GetMapping
    public ResponseEntity<List<GiftCard>> getAvailableGiftCards(
            @RequestHeader("X-User-Id") String userId)
            throws ExecutionException, InterruptedException {

        User user = userRepository.findByIdSync(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Check if user can access gift card store
        if (!subscriptionService.canAccessGiftCardStore(user)) {
            throw new SubscriptionLimitReachedException(
                    "Gift Card store is only available for Premium users. Upgrade to access!");
        }

        // Mock gift cards
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

    /** Redeem a gift card (Mock - just logs the action). */
    @PostMapping("/{giftCardId}/redeem")
    public ResponseEntity<String> redeemGiftCard(
            @PathVariable String giftCardId, @RequestHeader("X-User-Id") String userId)
            throws ExecutionException, InterruptedException {

        User user = userRepository.findByIdSync(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        if (!subscriptionService.canAccessGiftCardStore(user)) {
            throw new SubscriptionLimitReachedException(
                    "Gift Card redemption is only available for Premium users.");
        }

        // Mock redemption logic
        log.info("User {} redeemed gift card {}", userId, giftCardId);

        return ResponseEntity.ok("Gift card redeemed successfully! (Mock)");
    }
}
