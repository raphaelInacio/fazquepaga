package com.fazquepaga.taskandpay.subscription;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.subscription.dto.SubscriptionStatusResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, String>> subscribe(@AuthenticationPrincipal User user) {
        String checkoutUrl = subscriptionService.generateSubscribeUrl(user.getId());
        return ResponseEntity.ok(Map.of("checkoutUrl", checkoutUrl));
    }

    @GetMapping("/status")
    public ResponseEntity<SubscriptionStatusResponse> getStatus(
            @AuthenticationPrincipal User user) {
        // Fetch fresh user data from DB
        User freshUser = subscriptionService.getUser(user.getId());

        // Calculate trial state
        boolean trialExpired = subscriptionService.isTrialExpired(freshUser);
        Integer daysRemaining = subscriptionService.getTrialDaysRemaining(freshUser);
        boolean isPremium = freshUser.getSubscriptionTier() == User.SubscriptionTier.PREMIUM
                && freshUser.getSubscriptionStatus() == User.SubscriptionStatus.ACTIVE;

        return ResponseEntity.ok(
                SubscriptionStatusResponse.builder()
                        .status(freshUser.getSubscriptionStatus())
                        .tier(freshUser.getSubscriptionTier())
                        .subscriptionId(freshUser.getSubscriptionId())
                        .isTrialActive(!trialExpired && !isPremium)
                        .trialDaysRemaining(daysRemaining)
                        .build());
    }
}
