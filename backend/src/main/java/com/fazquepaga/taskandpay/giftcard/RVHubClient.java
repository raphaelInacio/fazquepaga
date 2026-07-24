package com.fazquepaga.taskandpay.giftcard;

import com.fazquepaga.taskandpay.giftcard.dto.RVHubCaptureResponse;
import com.fazquepaga.taskandpay.giftcard.dto.RVHubProductResponse;
import com.fazquepaga.taskandpay.giftcard.dto.RVHubTransactionResponse;
import java.math.BigDecimal;
import java.util.List;

public interface RVHubClient {
    // Autentica via client_credentials e retorna o token Bearer JWT
    String authenticate();

    // Busca o portfólio de produtos disponíveis
    List<RVHubProductResponse> getPortfolio(String kind);

    // Solicita a recarga (Passo 1 do RV Hub) com Idempotency Key
    RVHubTransactionResponse requestPinTopup(
            String productId, BigDecimal amount, String idempotencyKey);

    // Captura a recarga (Passo 2 do RV Hub)
    RVHubCaptureResponse capturePinTopup(String transactionId);
}
