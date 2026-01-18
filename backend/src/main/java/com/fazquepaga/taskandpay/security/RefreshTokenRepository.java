package com.fazquepaga.taskandpay.security;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * Repository for refresh token operations in Firestore.
 * Collection: refreshTokens/{tokenId}
 */
@Repository
public class RefreshTokenRepository {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenRepository.class);
    private static final String COLLECTION_NAME = "refreshTokens";

    private final Firestore firestore;

    public RefreshTokenRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference getCollection() {
        return firestore.collection(COLLECTION_NAME);
    }

    /**
     * Save a new refresh token.
     */
    public RefreshToken save(RefreshToken refreshToken) throws ExecutionException, InterruptedException {
        DocumentReference docRef;
        if (refreshToken.getId() == null) {
            docRef = getCollection().document();
            refreshToken.setId(docRef.getId());
        } else {
            docRef = getCollection().document(refreshToken.getId());
        }

        docRef.set(refreshToken).get();
        log.debug("Saved refresh token with id: {}", refreshToken.getId());
        return refreshToken;
    }

    /**
     * Find a refresh token by its hash.
     */
    public Optional<RefreshToken> findByTokenHash(String tokenHash)
            throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> query = getCollection().whereEqualTo("tokenHash", tokenHash).limit(1).get();

        List<QueryDocumentSnapshot> documents = query.get().getDocuments();
        if (documents.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(documents.get(0).toObject(RefreshToken.class));
    }

    /**
     * Revoke all refresh tokens for a user.
     * Uses batch write for efficiency.
     */
    public void revokeAllForUser(String userId) throws ExecutionException, InterruptedException {
        Query query = getCollection().whereEqualTo("userId", userId).whereEqualTo("revoked", false);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

        if (documents.isEmpty()) {
            log.debug("No active refresh tokens found for user: {}", userId);
            return;
        }

        WriteBatch batch = firestore.batch();
        for (QueryDocumentSnapshot doc : documents) {
            batch.update(doc.getReference(), "revoked", true);
        }

        batch.commit().get();
        log.info("Revoked {} refresh tokens for user: {}", documents.size(), userId);
    }

    /**
     * Delete expired tokens (cleanup job).
     */
    public int deleteExpiredTokens() throws ExecutionException, InterruptedException {
        Query query = getCollection()
                .whereLessThan("expiresAt", java.time.Instant.now())
                .limit(500); // Process in batches

        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

        if (documents.isEmpty()) {
            return 0;
        }

        WriteBatch batch = firestore.batch();
        for (QueryDocumentSnapshot doc : documents) {
            batch.delete(doc.getReference());
        }

        batch.commit().get();
        log.info("Deleted {} expired refresh tokens", documents.size());
        return documents.size();
    }
}
