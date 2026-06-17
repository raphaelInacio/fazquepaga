package com.fazquepaga.taskandpay.giftcard;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.fazquepaga.taskandpay.subscription.SubscriptionService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = GiftCardController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestPropertySource(properties = "asaas.api-key=dummy-test-key")
class GiftCardControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private GiftCardService giftCardService;
    @MockitoBean private SubscriptionService subscriptionService;
    @MockitoBean private UserRepository userRepository;

    @MockitoBean private com.fazquepaga.taskandpay.security.JwtService jwtService;
    @MockitoBean private com.fazquepaga.taskandpay.security.RateLimitService rateLimitService;
    @MockitoBean private com.fazquepaga.taskandpay.security.RateLimitConfig rateLimitConfig;

    @org.junit.jupiter.api.BeforeEach
    void setup() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    private void setAuthentication(User user) {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
        );
    }

    @Test
    void shouldGetAvailableGiftCardsWhenUserIsPremium() throws Exception {
        // Given
        String userId = "parent-123";
        User parent =
                User.builder().id(userId).role(User.Role.PARENT).email("parent@test.com").build();

        when(userRepository.findByIdSync(userId)).thenReturn(parent);
        when(subscriptionService.canAccessGiftCardStore(parent)).thenReturn(true);

        setAuthentication(parent);

        // When & Then
        mockMvc.perform(get("/api/v1/giftcards/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].brand").value("Roblox"))
                .andExpect(jsonPath("$[0].value").value(50.00))
                .andExpect(jsonPath("$[1].brand").value("iFood"))
                .andExpect(jsonPath("$[1].value").value(30.00));
    }

    @Test
    void shouldFailToGetCatalogWhenUserIsNotPremium() throws Exception {
        // Given
        String userId = "parent-123";
        User parent =
                User.builder().id(userId).role(User.Role.PARENT).email("parent@test.com").build();

        when(userRepository.findByIdSync(userId)).thenReturn(parent);
        when(subscriptionService.canAccessGiftCardStore(parent)).thenReturn(false);

        setAuthentication(parent);

        // When & Then
        mockMvc.perform(get("/api/v1/giftcards/catalog"))
                .andExpect(status().isPaymentRequired());
    }

    @Test
    void shouldCreateGiftCardRequestSuccessfully() throws Exception {
        // Given
        String childId = "child-123";
        String parentId = "parent-456";
        User child =
                User.builder()
                        .id(childId)
                        .role(User.Role.CHILD)
                        .parentId(parentId)
                        .balance(BigDecimal.valueOf(100.00))
                        .build();

        GiftCardController.CreateGiftCardRequest request =
                new GiftCardController.CreateGiftCardRequest("prod-1", BigDecimal.valueOf(50.00));

        GiftCardTransaction tx =
                GiftCardTransaction.builder()
                        .id("tx-123")
                        .childId(childId)
                        .parentId(parentId)
                        .productId("prod-1")
                        .amount(BigDecimal.valueOf(50.00))
                        .status(GiftCardTransaction.Status.PENDING)
                        .createdAt(Instant.now())
                        .build();

        when(userRepository.findByIdSync(childId)).thenReturn(child);
        when(giftCardService.requestGiftCard(
                        eq(childId), eq(parentId), eq("prod-1"), eq(BigDecimal.valueOf(50.00))))
                .thenReturn(tx);

        setAuthentication(child);

        // When & Then
        mockMvc.perform(
                        post("/api/v1/giftcards/requests")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("tx-123"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldListRequestsForChild() throws Exception {
        // Given
        String childId = "child-123";
        User child = User.builder().id(childId).role(User.Role.CHILD).build();

        GiftCardTransaction tx =
                GiftCardTransaction.builder()
                        .id("tx-123")
                        .childId(childId)
                        .productId("prod-1")
                        .amount(BigDecimal.valueOf(50.00))
                        .status(GiftCardTransaction.Status.PENDING)
                        .build();

        when(userRepository.findByIdSync(childId)).thenReturn(child);
        when(giftCardService.getTransactionsByChildId(childId)).thenReturn(List.of(tx));

        setAuthentication(child);

        // When & Then
        mockMvc.perform(get("/api/v1/giftcards/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("tx-123"))
                .andExpect(jsonPath("$[0].amount").value(50.00));
    }

    @Test
    void shouldApproveRequestSuccessfully() throws Exception {
        // Given
        String parentId = "parent-456";
        String txId = "tx-123";
        User parent = User.builder().id(parentId).role(User.Role.PARENT).build();

        GiftCardTransaction tx =
                GiftCardTransaction.builder()
                        .id(txId)
                        .parentId(parentId)
                        .productId("prod-1")
                        .amount(BigDecimal.valueOf(50.00))
                        .status(GiftCardTransaction.Status.COMPLETED)
                        .pinCode("PIN-ABCDE")
                        .build();

        when(userRepository.findByIdSync(parentId)).thenReturn(parent);
        when(giftCardService.approveGiftCard(parentId, txId)).thenReturn(tx);

        setAuthentication(parent);

        // When & Then
        mockMvc.perform(
                        post("/api/v1/giftcards/requests/{id}/approve", txId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(txId))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.pinCode").value("PIN-ABCDE"));
    }
}
