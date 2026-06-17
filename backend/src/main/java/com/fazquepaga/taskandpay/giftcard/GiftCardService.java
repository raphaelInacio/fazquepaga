package com.fazquepaga.taskandpay.giftcard;

import com.fazquepaga.taskandpay.giftcard.dto.RVHubCaptureResponse;
import com.fazquepaga.taskandpay.giftcard.dto.RVHubTransactionResponse;
import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.fazquepaga.taskandpay.payment.AsaasService;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GiftCardService {

    private final GiftCardTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AsaasService asaasService;
    private final RVHubClient rvHubClient;
    private final Firestore firestore;

    public GiftCardTransaction requestGiftCard(
            String childId, String parentId, String productId, BigDecimal amount)
            throws ExecutionException, InterruptedException {
        log.info(
                "Processing Gift Card request. Child: {}, Parent: {}, Product: {}, Amount: {}",
                childId,
                parentId,
                productId,
                amount);

        User child = userRepository.findByIdSync(childId);
        if (child == null) {
            throw new IllegalArgumentException("Criança não encontrada");
        }

        if (child.getParentId() == null || !child.getParentId().equals(parentId)) {
            throw new IllegalArgumentException("Criança não pertence a este responsável");
        }

        BigDecimal balance = child.getBalance() != null ? child.getBalance() : BigDecimal.ZERO;
        if (balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Saldo fictício insuficiente para esta operação");
        }

        GiftCardTransaction transaction =
                GiftCardTransaction.builder()
                        .id(UUID.randomUUID().toString())
                        .childId(childId)
                        .parentId(parentId)
                        .productId(productId)
                        .amount(amount)
                        .status(GiftCardTransaction.Status.PENDING)
                        .createdAt(Instant.now())
                        .idempotencyKey(UUID.randomUUID().toString())
                        .build();

        transactionRepository.save(transaction);
        return transaction;
    }

    public GiftCardTransaction approveGiftCard(String parentId, String transactionId)
            throws ExecutionException, InterruptedException {
        log.info(
                "Processing Gift Card approval. Parent: {}, Transaction: {}",
                parentId,
                transactionId);

        GiftCardTransaction transaction = transactionRepository.findById(transactionId);
        validateApprovalRequest(parentId, transaction);

        User parent = userRepository.findByIdSync(parentId);
        User child = userRepository.findByIdSync(transaction.getChildId());
        validateUsersAndBalance(parent, child, transaction);

        String asaasPaymentId = null;
        try {
            // 1. Cobrança Real (Asaas)
            asaasPaymentId = chargeParent(parent, transaction);

            // 2. Emissão do Gift Card (RV Hub)
            RVHubTransactionResponse rvhubTx = requestRVHubTopup(transaction);
            String rvhubTransactionId = rvhubTx.getId();

            RVHubCaptureResponse rvhubCapture = rvHubClient.capturePinTopup(rvhubTransactionId);
            String pinCode = extractPinCode(rvhubCapture);

            // 3. Consolidação Final Atômica no Firestore (Transação e Saldo)
            consolidateDatabaseTransaction(transaction, rvhubTransactionId, pinCode);

            log.info(
                    "Gift Card emitido com sucesso. Transação: {}, PIN: {}",
                    transaction.getId(),
                    pinCode);
            return transactionRepository.findById(transactionId);

        } catch (Exception e) {
            handleApprovalFailure(transaction, asaasPaymentId, e);
            throw e;
        }
    }

    public List<GiftCardTransaction> getTransactionsByChildId(String childId)
            throws ExecutionException, InterruptedException {
        return transactionRepository.findByChildId(childId);
    }

    public List<GiftCardTransaction> getTransactionsByParentId(String parentId)
            throws ExecutionException, InterruptedException {
        return transactionRepository.findByParentId(parentId);
    }

    private void validateApprovalRequest(String parentId, GiftCardTransaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transação não encontrada");
        }
        if (!transaction.getParentId().equals(parentId)) {
            throw new IllegalArgumentException("Transação não pertence a este responsável");
        }
        if (transaction.getStatus() != GiftCardTransaction.Status.PENDING) {
            throw new IllegalStateException("Esta transação já foi processada.");
        }
    }

    private void validateUsersAndBalance(User parent, User child, GiftCardTransaction transaction)
            throws ExecutionException, InterruptedException {
        if (parent == null || child == null) {
            throw new IllegalArgumentException("Responsável ou dependente não encontrado");
        }
        BigDecimal balance = child.getBalance() != null ? child.getBalance() : BigDecimal.ZERO;
        if (balance.compareTo(transaction.getAmount()) < 0) {
            transaction.setStatus(GiftCardTransaction.Status.FAILED);
            transactionRepository.save(transaction);
            throw new IllegalArgumentException(
                    "Saldo fictício do dependente insuficiente para aprovação.");
        }
    }

    private String chargeParent(User parent, GiftCardTransaction transaction) {
        try {
            String paymentId =
                    asaasService.createAdHocCharge(
                            parent, transaction.getAmount(), transaction.getId());
            transaction.setAsaasPaymentId(paymentId);
            transactionRepository.save(transaction);
            return paymentId;
        } catch (Exception e) {
            log.error(
                    "Erro na cobrança Asaas para a transação: {}. Mensagem: {}",
                    transaction.getId(),
                    e.getMessage());
            transaction.setStatus(GiftCardTransaction.Status.FAILED);
            transactionRepository.save(transaction);
            throw e;
        }
    }

    private RVHubTransactionResponse requestRVHubTopup(GiftCardTransaction transaction) {
        return rvHubClient.requestPinTopup(
                transaction.getProductId(),
                transaction.getAmount(),
                transaction.getIdempotencyKey());
    }

    private String extractPinCode(RVHubCaptureResponse rvhubCapture) {
        String pinCode = null;
        if (rvhubCapture.getPin() != null) {
            pinCode = rvhubCapture.getPin().getCode();
        }
        if (pinCode == null || pinCode.isEmpty()) {
            throw new RuntimeException("Código PIN não retornado pela RV Hub");
        }
        return pinCode;
    }

    private void consolidateDatabaseTransaction(
            GiftCardTransaction transaction, String rvhubTransactionId, String pinCode)
            throws ExecutionException, InterruptedException {

        String transactionId = transaction.getId();
        String childId = transaction.getChildId();
        BigDecimal amount = transaction.getAmount();

        firestore
                .runTransaction(
                        tx -> {
                            // 1. Atualiza e salva a transação de Gift Card
                            DocumentReference txRef =
                                    firestore
                                            .collection("giftcard_transactions")
                                            .document(transactionId);
                            transaction.setRvhubTransactionId(rvhubTransactionId);
                            transaction.setPinCode(pinCode);
                            transaction.setStatus(GiftCardTransaction.Status.COMPLETED);
                            tx.set(txRef, transaction);

                            // 2. Deduz o saldo fictício do dependente de forma atômica
                            DocumentReference childRef =
                                    firestore.collection("users").document(childId);
                            com.google.cloud.firestore.DocumentSnapshot childSnapshot =
                                    tx.get(childRef).get();
                            User latestChild = childSnapshot.toObject(User.class);
                            if (latestChild == null) {
                                throw new IllegalArgumentException(
                                        "Criança não encontrada no momento de consolidar"
                                                + " transação");
                            }
                            BigDecimal balance =
                                    latestChild.getBalance() != null
                                            ? latestChild.getBalance()
                                            : BigDecimal.ZERO;
                            if (balance.compareTo(amount) < 0) {
                                throw new IllegalArgumentException(
                                        "Saldo fictício do dependente insuficiente no momento de"
                                                + " consolidar.");
                            }
                            latestChild.setBalance(balance.subtract(amount));
                            tx.set(childRef, latestChild);

                            return null;
                        })
                .get();
    }

    private void handleApprovalFailure(
            GiftCardTransaction transaction, String asaasPaymentId, Exception e) {
        log.error(
                "[GIFT_CARD_ERROR] Erro na emissão da RV Hub. Iniciando estorno automático no"
                        + " Asaas. Payment ID: {}, Transação ID: {}, Erro: {}",
                asaasPaymentId,
                transaction.getId(),
                e.getMessage(),
                e);

        if (asaasPaymentId != null) {
            try {
                asaasService.refundCharge(asaasPaymentId);
                log.info(
                        "Estorno automático no Asaas concluído com sucesso para o Payment ID: {}",
                        asaasPaymentId);
            } catch (Exception refundEx) {
                log.error(
                        "[GIFT_CARD_ERROR] Falha crítica ao tentar estornar cobrança no Asaas."
                                + " Payment ID: {}. Requer intervenção manual! Erro: {}",
                        asaasPaymentId,
                        refundEx.getMessage(),
                        refundEx);
            }
        }

        transaction.setStatus(GiftCardTransaction.Status.FAILED);
        transactionRepository.save(transaction);
    }
}
