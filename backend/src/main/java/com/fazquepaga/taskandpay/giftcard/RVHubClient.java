package com.fazquepaga.taskandpay.giftcard;

import com.fazquepaga.taskandpay.giftcard.dto.RVHubCaptureResponse;
import com.fazquepaga.taskandpay.giftcard.dto.RVHubTransactionResponse;
import java.math.BigDecimal;

public interface RVHubClient {
    // Autentica via client_credentials e retorna o token Bearer JWT
    String authenticate();

    // Solicita a recarga (Passo 1 do RV Hub) com Idempotency Key
    RVHubTransactionResponse requestPinTopup(
            String productId, BigDecimal amount, String idempotencyKey);

    // Captura a recarga (Passo 2 do RV Hub)
    RVHubCaptureResponse capturePinTopup(String transactionId);
}
