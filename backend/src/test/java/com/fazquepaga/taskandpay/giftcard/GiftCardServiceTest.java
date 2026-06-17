package com.fazquepaga.taskandpay.giftcard;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fazquepaga.taskandpay.giftcard.dto.RVHubCaptureResponse;
import com.fazquepaga.taskandpay.giftcard.dto.RVHubTransactionResponse;
import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.fazquepaga.taskandpay.payment.AsaasService;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Transaction;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class GiftCardServiceTest {

    @Mock private GiftCardTransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private AsaasService asaasService;
    @Mock private RVHubClient rvHubClient;
    @Mock private Firestore firestore;

    private GiftCardService service;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        service =
                new GiftCardService(
                        transactionRepository,
                        userRepository,
                        asaasService,
                        rvHubClient,
                        firestore);

        // Mock firestore collection and document calls to return valid mocks
        com.google.cloud.firestore.CollectionReference mockCollection =
                mock(com.google.cloud.firestore.CollectionReference.class);
        com.google.cloud.firestore.DocumentReference mockDocRef =
                mock(com.google.cloud.firestore.DocumentReference.class);
        when(firestore.collection(anyString())).thenReturn(mockCollection);
        when(mockCollection.document(anyString())).thenReturn(mockDocRef);

        // Mock generic firestore runTransaction behavior to run the functional interface callback
        when(firestore.runTransaction(any()))
                .thenAnswer(
                        invocation -> {
                            @SuppressWarnings("unchecked")
                            Transaction.Function<Void> callback = invocation.getArgument(0);
                            Transaction mockTx = mock(Transaction.class);

                            DocumentSnapshot childSnapshot = mock(DocumentSnapshot.class);
                            // Dynamically fetch whatever child user is mock-configured in
                            // userRepository for child-123
                            User currentChild = userRepository.findByIdSync("child-123");
                            when(childSnapshot.toObject(User.class)).thenReturn(currentChild);
                            doReturn(com.google.api.core.ApiFutures.immediateFuture(childSnapshot))
                                    .when(mockTx)
                                    .get(any(com.google.cloud.firestore.DocumentReference.class));

                            callback.updateCallback(mockTx);
                            return com.google.api.core.ApiFutures.immediateFuture(null);
                        });
    }

    @Test
    void shouldRequestGiftCardSuccessfully() throws ExecutionException, InterruptedException {
        // Given
        String childId = "child-123";
        String parentId = "parent-456";
        String productId = "prod-robux";
        BigDecimal amount = BigDecimal.valueOf(30.00);

        User child =
                User.builder()
                        .id(childId)
                        .parentId(parentId)
                        .balance(BigDecimal.valueOf(50.00))
                        .build();

        when(userRepository.findByIdSync(childId)).thenReturn(child);

        // When
        GiftCardTransaction tx = service.requestGiftCard(childId, parentId, productId, amount);

        // Then
        assertNotNull(tx);
        assertEquals(childId, tx.getChildId());
        assertEquals(parentId, tx.getParentId());
        assertEquals(productId, tx.getProductId());
        assertEquals(amount, tx.getAmount());
        assertEquals(GiftCardTransaction.Status.PENDING, tx.getStatus());
        assertNotNull(tx.getIdempotencyKey());
        verify(transactionRepository).save(tx);
    }

    @Test
    void shouldFailRequestWhenChildNotFound() throws ExecutionException, InterruptedException {
        // Given
        when(userRepository.findByIdSync("nonexistent")).thenReturn(null);

        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> service.requestGiftCard("nonexistent", "parent", "prod", BigDecimal.TEN));
    }

    @Test
    void shouldFailRequestWhenChildDoesNotBelongToParent()
            throws ExecutionException, InterruptedException {
        // Given
        User child =
                User.builder()
                        .id("child-123")
                        .parentId("another-parent")
                        .balance(BigDecimal.valueOf(50.00))
                        .build();

        when(userRepository.findByIdSync("child-123")).thenReturn(child);

        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> service.requestGiftCard("child-123", "parent-456", "prod", BigDecimal.TEN));
    }

    @Test
    void shouldFailRequestWhenInsufficientBalance()
            throws ExecutionException, InterruptedException {
        // Given
        User child =
                User.builder()
                        .id("child-123")
                        .parentId("parent-456")
                        .balance(BigDecimal.valueOf(5.00))
                        .build();

        when(userRepository.findByIdSync("child-123")).thenReturn(child);

        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.requestGiftCard(
                                "child-123", "parent-456", "prod", BigDecimal.valueOf(50.00)));
    }

    @Test
    void shouldApproveGiftCardSuccessfully() throws ExecutionException, InterruptedException {
        // Given
        String txId = "tx-789";
        String parentId = "parent-456";
        String childId = "child-123";

        GiftCardTransaction tx =
                GiftCardTransaction.builder()
                        .id(txId)
                        .childId(childId)
                        .parentId(parentId)
                        .productId("prod-robux")
                        .amount(BigDecimal.valueOf(25.00))
                        .status(GiftCardTransaction.Status.PENDING)
                        .idempotencyKey("idemp-key-1")
                        .build();

        User parent = User.builder().id(parentId).asaasCustomerId("cus-parent").build();
        User child = User.builder().id(childId).balance(BigDecimal.valueOf(30.00)).build();

        when(transactionRepository.findById(txId)).thenReturn(tx);
        when(userRepository.findByIdSync(parentId)).thenReturn(parent);
        when(userRepository.findByIdSync(childId)).thenReturn(child);

        // Mock Asaas
        when(asaasService.createAdHocCharge(parent, tx.getAmount(), txId))
                .thenReturn("asaas-pay-1");

        // Mock RV Hub
        RVHubTransactionResponse rvhubTx = new RVHubTransactionResponse();
        rvhubTx.setId("rv-tx-111");
        when(rvHubClient.requestPinTopup("prod-robux", tx.getAmount(), "idemp-key-1"))
                .thenReturn(rvhubTx);

        RVHubCaptureResponse rvhubCapture = new RVHubCaptureResponse();
        RVHubCaptureResponse.PinInfo pinInfo = new RVHubCaptureResponse.PinInfo();
        pinInfo.setCode("PINCODE-12345");
        rvhubCapture.setPin(pinInfo);
        when(rvHubClient.capturePinTopup("rv-tx-111")).thenReturn(rvhubCapture);

        // When
        GiftCardTransaction result = service.approveGiftCard(parentId, txId);

        // Then
        assertNotNull(result);
        assertEquals(GiftCardTransaction.Status.COMPLETED, result.getStatus());
        assertEquals("asaas-pay-1", result.getAsaasPaymentId());
        assertEquals("rv-tx-111", result.getRvhubTransactionId());
        assertEquals("PINCODE-12345", result.getPinCode());

        // Verifica dedução de saldo fictício (30.00 - 25.00 = 5.00)
        assertEquals(BigDecimal.valueOf(5.00), child.getBalance());
        verify(userRepository, never()).save(any());
        verify(transactionRepository, times(1))
                .save(tx); // Uma vez ao salvar ID Asaas, a consolidação ocorre via Firestore
        // transaction
    }

    @Test
    void shouldRefundAndFailWhenRVHubFails() throws ExecutionException, InterruptedException {
        // Given
        String txId = "tx-789";
        String parentId = "parent-456";
        String childId = "child-123";

        GiftCardTransaction tx =
                GiftCardTransaction.builder()
                        .id(txId)
                        .childId(childId)
                        .parentId(parentId)
                        .productId("prod-robux")
                        .amount(BigDecimal.valueOf(25.00))
                        .status(GiftCardTransaction.Status.PENDING)
                        .idempotencyKey("idemp-key-1")
                        .build();

        User parent = User.builder().id(parentId).asaasCustomerId("cus-parent").build();
        User child = User.builder().id(childId).balance(BigDecimal.valueOf(30.00)).build();

        when(transactionRepository.findById(txId)).thenReturn(tx);
        when(userRepository.findByIdSync(parentId)).thenReturn(parent);
        when(userRepository.findByIdSync(childId)).thenReturn(child);

        // Mock Asaas
        when(asaasService.createAdHocCharge(parent, tx.getAmount(), txId))
                .thenReturn("asaas-pay-1");

        // Mock RV Hub falhando
        when(rvHubClient.requestPinTopup(any(), any(), any()))
                .thenThrow(new RuntimeException("RV Hub service unavailable"));

        // When & Then
        assertThrows(RuntimeException.class, () -> service.approveGiftCard(parentId, txId));

        // Verifica que o estorno foi acionado
        verify(asaasService).refundCharge("asaas-pay-1");

        // Verifica que a transação foi marcada como FAILED e o saldo fictício NÃO foi deduzido
        assertEquals(GiftCardTransaction.Status.FAILED, tx.getStatus());
        assertEquals(BigDecimal.valueOf(30.00), child.getBalance()); // intocado
        verify(transactionRepository, times(2)).save(tx); // Uma vez Asaas id, outra vez falhado
        verify(userRepository, never()).save(child);
    }
}
