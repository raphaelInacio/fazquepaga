package com.fazquepaga.taskandpay.shared.stats;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementação do StatsService utilizando o SDK Admin do Firestore. Todas as escritas são atômicas
 * via FieldValue.increment() para evitar race conditions.
 */
@Service
public class FirestoreStatsService implements StatsService {

    private static final Logger log = LoggerFactory.getLogger(FirestoreStatsService.class);

    private static final String FAMILIES_COLLECTION = "families";
    private static final String METADATA_COLLECTION = "metadata";
    private static final String STATS_DOCUMENT = "stats";
    private static final String GLOBAL_COLLECTION = "global";

    private final Firestore firestore;

    public FirestoreStatsService(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public CompletableFuture<Void> incrementFamilyStat(
            String familyId, String field, double amount) {
        if (familyId == null || familyId.isBlank()) {
            log.warn("incrementFamilyStat called with null/blank familyId for field={}", field);
            return CompletableFuture.completedFuture(null);
        }

        DocumentReference statsRef =
                firestore
                        .collection(FAMILIES_COLLECTION)
                        .document(familyId)
                        .collection(METADATA_COLLECTION)
                        .document(STATS_DOCUMENT);

        Map<String, Object> updates = new HashMap<>();
        updates.put(field, FieldValue.increment(amount));
        updates.put("lastActivityTimestamp", FieldValue.serverTimestamp());

        ApiFuture<WriteResult> future =
                statsRef.set(updates, com.google.cloud.firestore.SetOptions.merge());

        return toCompletableFuture(future, familyId, field);
    }

    @Override
    public CompletableFuture<Void> incrementGlobalStat(String field, double amount) {
        DocumentReference globalStatsRef =
                firestore.collection(GLOBAL_COLLECTION).document(STATS_DOCUMENT);

        Map<String, Object> updates = new HashMap<>();
        updates.put(field, FieldValue.increment(amount));
        updates.put("lastActivityTimestamp", FieldValue.serverTimestamp());

        ApiFuture<WriteResult> future =
                globalStatsRef.set(updates, com.google.cloud.firestore.SetOptions.merge());

        return toCompletableFuture(future, "global", field);
    }

    private CompletableFuture<Void> toCompletableFuture(
            ApiFuture<WriteResult> apiFuture, String context, String field) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        com.google.api.core.ApiFutures.addCallback(
                apiFuture,
                new com.google.api.core.ApiFutureCallback<WriteResult>() {
                    @Override
                    public void onSuccess(WriteResult result) {
                        log.debug(
                                "Stat incremented successfully: context={}, field={},"
                                        + " updateTime={}",
                                context,
                                field,
                                result.getUpdateTime());
                        completableFuture.complete(null);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        log.error(
                                "Failed to increment stat: context={}, field={}, error={}",
                                context,
                                field,
                                t.getMessage(),
                                t);
                        // Não propaga exceção para não bloquear fluxos principais de negócio
                        completableFuture.complete(null);
                    }
                },
                com.google.common.util.concurrent.MoreExecutors.directExecutor());

        return completableFuture;
    }

    @Override
    public CompletableFuture<Map<String, Object>> getFamilyStats(String familyId) {
        if (familyId == null || familyId.isBlank()) {
            log.warn("getFamilyStats called with null/blank familyId");
            return CompletableFuture.completedFuture(Map.of());
        }

        DocumentReference statsRef =
                firestore
                        .collection(FAMILIES_COLLECTION)
                        .document(familyId)
                        .collection(METADATA_COLLECTION)
                        .document(STATS_DOCUMENT);

        CompletableFuture<Map<String, Object>> completableFuture = new CompletableFuture<>();

        com.google.api.core.ApiFutures.addCallback(
                statsRef.get(),
                new com.google.api.core.ApiFutureCallback<
                        com.google.cloud.firestore.DocumentSnapshot>() {
                    @Override
                    public void onSuccess(com.google.cloud.firestore.DocumentSnapshot result) {
                        if (result.exists()) {
                            completableFuture.complete(result.getData());
                        } else {
                            Map<String, Object> defaultStats = new HashMap<>();
                            defaultStats.put("totalTasksCreated", 0L);
                            defaultStats.put("totalTasksCompleted", 0L);
                            defaultStats.put("totalTasksApproved", 0L);
                            defaultStats.put("totalAllowancePaid", 0.0);
                            defaultStats.put("aiSuggestionsUsed", 0L);
                            completableFuture.complete(defaultStats);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        log.error(
                                "Failed to fetch family stats for familyId={}: {}",
                                familyId,
                                t.getMessage());
                        completableFuture.completeExceptionally(t);
                    }
                },
                com.google.common.util.concurrent.MoreExecutors.directExecutor());

        return completableFuture;
    }

    @Override
    public CompletableFuture<Void> recalculateFamilyStats(String familyId) {
        if (familyId == null || familyId.isBlank()) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            try {
                // 1. Buscar os filhos da família na coleção "users"
                var childrenDocs = firestore.collection("users")
                        .whereEqualTo("parentId", familyId)
                        .whereEqualTo("role", "CHILD")
                        .get()
                        .get()
                        .getDocuments();

                long totalCreated = 0;
                long totalCompleted = 0;
                long totalApproved = 0;
                double totalAllowance = 0.0;

                for (var childDoc : childrenDocs) {
                    String childId = childDoc.getId();
                    // 2. Buscar tarefas do filho na subcoleção "tasks"
                    var tasksDocs = firestore.collection("users")
                            .document(childId)
                            .collection("tasks")
                            .get()
                            .get()
                            .getDocuments();

                    for (var taskDoc : tasksDocs) {
                        Boolean archived = taskDoc.getBoolean("archived");
                        if (Boolean.TRUE.equals(archived)) {
                            continue;
                        }

                        totalCreated++;
                        String statusStr = taskDoc.getString("status");
                        if (statusStr != null) {
                            if (statusStr.equals("APPROVED")) {
                                totalApproved++;
                                totalCompleted++;
                                Double val = taskDoc.getDouble("value");
                                if (val != null) {
                                    totalAllowance += val;
                                }
                            } else if (statusStr.equals("COMPLETED") || statusStr.equals("PENDING_APPROVAL")) {
                                totalCompleted++;
                            }
                        }
                    }
                }

                // 3. Salvar de volta no documento de estatísticas da família
                DocumentReference statsRef = firestore
                        .collection(FAMILIES_COLLECTION)
                        .document(familyId)
                        .collection(METADATA_COLLECTION)
                        .document(STATS_DOCUMENT);

                Map<String, Object> updates = new HashMap<>();
                updates.put("totalTasksCreated", totalCreated);
                updates.put("totalTasksCompleted", totalCompleted);
                updates.put("totalTasksApproved", totalApproved);
                updates.put("totalAllowancePaid", totalAllowance);
                updates.put("lastActivityTimestamp", FieldValue.serverTimestamp());

                statsRef.set(updates, com.google.cloud.firestore.SetOptions.merge()).get();
                completableFuture.complete(null);

            } catch (Exception e) {
                log.error("Failed to recalculate family stats for familyId={}", familyId, e);
                completableFuture.completeExceptionally(e);
            }
        });

        return completableFuture;
    }
}
