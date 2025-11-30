package com.fazquepaga.taskandpay.allowance;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionRepository {

    private final Firestore firestore;

    public TransactionRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public void save(Transaction transaction) {
        firestore.collection("transactions").document(transaction.getId()).set(transaction);
    }

    public QuerySnapshot findByChildId(String childId)
            throws ExecutionException, InterruptedException {
        CollectionReference transactions = firestore.collection("transactions");
        Query query =
                transactions
                        .whereEqualTo("childId", childId)
                        .orderBy("date", Query.Direction.DESCENDING);
        return query.get().get();
    }
}
