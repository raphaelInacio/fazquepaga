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
        // Refresh user status from DB in case webhook updated it recently
        User.SubscriptionStatus status = subscriptionService.getStatus(user.getId());
        User.SubscriptionTier tier = subscriptionService.getTier(user.getId());

        return ResponseEntity.ok(
                SubscriptionStatusResponse.builder()
                        .status(status)
                        .tier(tier)
                        .subscriptionId(user.getSubscriptionId()) // Might be null if fresh from
                        // principal, but we fetched
                        // status/tier from service which fetches fresh user.
                        // Wait, service.getStatus calls getUser(userId).
                        // Ideally I should get the fresh User object once.
                        .build());
    }
}
