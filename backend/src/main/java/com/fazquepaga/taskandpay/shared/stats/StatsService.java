package com.fazquepaga.taskandpay.shared.stats;

import java.util.concurrent.CompletableFuture;

/**
 * Serviço de contadores analíticos consolidados no Firestore. Usa FieldValue.increment() para
 * garantir escritas atômicas sem race conditions.
 */
public interface StatsService {

    /**
     * Incrementa atomicamente uma estatística de uso ou de negócio de uma família. O documento alvo
     * é `/families/{familyId}/metadata/stats`.
     *
     * @param familyId ID da família (parentId)
     * @param field nome do campo a incrementar (ex: "totalTasksCreated")
     * @param amount valor a adicionar (pode ser negativo para decrementar)
     */
    CompletableFuture<Void> incrementFamilyStat(String familyId, String field, double amount);

    /**
     * Incrementa um contador global do sistema. O documento alvo é `/global/stats`.
     *
     * @param field nome do campo a incrementar (ex: "totalAiPrompts")
     * @param amount valor a adicionar
     */
    CompletableFuture<Void> incrementGlobalStat(String field, double amount);

    /**
     * Busca as estatísticas consolidadas de uma família. O documento alvo é
     * `/families/{familyId}/metadata/stats`.
     *
     * @param familyId ID da família (parentId)
     * @return um mapa contendo os campos do documento de estatísticas
     */
    CompletableFuture<java.util.Map<String, Object>> getFamilyStats(String familyId);

    /**
     * Recalcula e sincroniza as estatísticas analíticas de uma família a partir das tarefas
     * reais salvas no banco.
     *
     * @param familyId ID da família (parentId)
     */
    CompletableFuture<Void> recalculateFamilyStats(String familyId);
}
