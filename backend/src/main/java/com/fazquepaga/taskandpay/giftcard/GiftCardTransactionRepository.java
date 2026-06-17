package com.fazquepaga.taskandpay.giftcard;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Repository;

@Repository
public class GiftCardTransactionRepository {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "giftcard_transactions";

    public GiftCardTransactionRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public void save(GiftCardTransaction transaction) {
        if (transaction.getId() == null || transaction.getId().isEmpty()) {
            transaction.setId(UUID.randomUUID().toString());
        }
        firestore.collection(COLLECTION_NAME).document(transaction.getId()).set(transaction);
    }

    public GiftCardTransaction findById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore.collection(COLLECTION_NAME).document(id).get().get();
        if (doc.exists()) {
            return doc.toObject(GiftCardTransaction.class);
        }
        return null;
    }

    public List<GiftCardTransaction> findByChildId(String childId)
            throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot =
                firestore
                        .collection(COLLECTION_NAME)
                        .whereEqualTo("childId", childId)
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                        .get()
                        .get();
        return querySnapshot.toObjects(GiftCardTransaction.class);
    }

    public List<GiftCardTransaction> findByParentId(String parentId)
            throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot =
                firestore
                        .collection(COLLECTION_NAME)
                        .whereEqualTo("parentId", parentId)
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                        .get()
                        .get();
        return querySnapshot.toObjects(GiftCardTransaction.class);
    }

    public Optional<GiftCardTransaction> findByIdempotencyKey(String idempotencyKey)
            throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot =
                firestore
                        .collection(COLLECTION_NAME)
                        .whereEqualTo("idempotencyKey", idempotencyKey)
                        .limit(1)
                        .get()
                        .get();
        List<GiftCardTransaction> transactions = querySnapshot.toObjects(GiftCardTransaction.class);
        if (!transactions.isEmpty()) {
            return Optional.of(transactions.get(0));
        }
        return Optional.empty();
    }
}
