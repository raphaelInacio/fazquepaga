package com.fazquepaga.taskandpay.ai;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Repository;

/**
 * Repository for AI quota operations in Firestore.
 * Path: users/{userId}/quotas/ai
 */
@Repository
public class AIQuotaRepository {

    private static final String USERS_COLLECTION = "users";
    private static final String QUOTAS_SUBCOLLECTION = "quotas";
    private static final String AI_DOCUMENT = "ai";

    private final Firestore firestore;

    public AIQuotaRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private DocumentReference getQuotaDocument(String userId) {
        return firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(QUOTAS_SUBCOLLECTION)
                .document(AI_DOCUMENT);
    }

    public AIQuota findByUserId(String userId) throws ExecutionException, InterruptedException {
        ApiFuture<DocumentSnapshot> future = getQuotaDocument(userId).get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            return document.toObject(AIQuota.class);
        }
        return null;
    }

    public ApiFuture<WriteResult> save(String userId, AIQuota quota) {
        quota.setUserId(userId);
        return getQuotaDocument(userId).set(quota);
    }
}
