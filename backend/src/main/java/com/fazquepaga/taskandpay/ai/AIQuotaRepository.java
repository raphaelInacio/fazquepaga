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

    /**
     * Creates an AIQuotaRepository using the provided Firestore instance.
     *
     * @param firestore the Firestore client used to access users/{userId}/quotas/ai
     */
    public AIQuotaRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Builds a DocumentReference for the AI quota document of the specified user.
     *
     * @param userId the user's identifier
     * @return the DocumentReference for users/{userId}/quotas/ai
     */
    private DocumentReference getQuotaDocument(String userId) {
        return firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(QUOTAS_SUBCOLLECTION)
                .document(AI_DOCUMENT);
    }

    /**
     * Retrieve the AI quota for the given user.
     *
     * @param userId the identifier of the user whose quota to fetch
     * @return the AIQuota for the specified user, or `null` if no quota document exists
     * @throws ExecutionException   if an error occurs while fetching the document
     * @throws InterruptedException if the thread is interrupted while waiting for the fetch
     */
    public AIQuota findByUserId(String userId) throws ExecutionException, InterruptedException {
        ApiFuture<DocumentSnapshot> future = getQuotaDocument(userId).get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            return document.toObject(AIQuota.class);
        }
        return null;
    }

    /**
     * Persist the AI quota for the given user to Firestore.
     *
     * @param userId the ID of the user whose quota document will be written
     * @param quota  the AIQuota to persist; its `userId` field will be set to `userId`
     * @return       the write result for the quota document
     */
    public ApiFuture<WriteResult> save(String userId, AIQuota quota) {
        quota.setUserId(userId);
        return getQuotaDocument(userId).set(quota);
    }
}