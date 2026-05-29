package com.fazquepaga.taskandpay.subscription;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.subscription.dto.CancelSubscriptionRequest;
import com.fazquepaga.taskandpay.subscription.dto.CancelSubscriptionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class SubscriptionControllerTest {

    @Mock private SubscriptionService subscriptionService;

    @InjectMocks private SubscriptionController subscriptionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCancelSubscription_Success() throws Exception {
        User parent = User.builder().id("parent1").role(User.Role.PARENT).build();
        CancelSubscriptionRequest request =
                new CancelSubscriptionRequest(CancellationReason.TOO_EXPENSIVE, null);

        CancelSubscriptionResponse response =
                CancelSubscriptionResponse.builder()
                        .status(User.SubscriptionStatus.PENDING_CANCELLATION)
                        .message("Assinatura cancelada com sucesso.")
                        .build();

        when(subscriptionService.cancelSubscription(
                        eq("parent1"), any(CancelSubscriptionRequest.class)))
                .thenReturn(response);

        ResponseEntity<CancelSubscriptionResponse> result =
                subscriptionController.cancelSubscription(parent, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("PENDING_CANCELLATION", result.getBody().getStatus().name());
    }

    @Test
    void testCancelSubscription_ForbiddenForChild() throws Exception {
        User child = User.builder().id("child1").role(User.Role.CHILD).build();
        CancelSubscriptionRequest request =
                new CancelSubscriptionRequest(CancellationReason.TOO_EXPENSIVE, null);

        ResponseEntity<CancelSubscriptionResponse> result =
                subscriptionController.cancelSubscription(child, request);

        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
    }
}
