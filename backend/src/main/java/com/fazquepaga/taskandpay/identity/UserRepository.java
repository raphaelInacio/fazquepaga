package com.fazquepaga.taskandpay.identity;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private static final String COLLECTION_NAME = "users";
    private final CollectionReference usersCollection;

    public UserRepository(Firestore firestore) {
        this.usersCollection = firestore.collection(COLLECTION_NAME);
    }

    public ApiFuture<WriteResult> save(User user) {
        if (user.getId() == null || user.getId().isEmpty()) {
            // Create new user with a generated ID
            DocumentReference docRef = usersCollection.document();
            user.setId(docRef.getId());
            return docRef.set(user);
        } else {
            // Update existing user
            return usersCollection.document(user.getId()).set(user);
        }
    }

    public ApiFuture<DocumentSnapshot> findById(String userId) {
        return usersCollection.document(userId).get();
    }

    public User findByIdSync(String userId) throws ExecutionException, InterruptedException {

        DocumentSnapshot documentSnapshot = findById(userId).get();

        if (documentSnapshot.exists()) {

            return documentSnapshot.toObject(User.class);
        }

        return null;
    }

    public User findByPhoneNumber(String phoneNumber)
            throws ExecutionException, InterruptedException {

        // This query requires a custom index on the 'phoneNumber' field in Firestore.

        // Without it, the query will fail.

        ApiFuture<com.google.cloud.firestore.QuerySnapshot> future = usersCollection
                .whereEqualTo("phoneNumber", phoneNumber).limit(1).get();

        List<com.google.cloud.firestore.QueryDocumentSnapshot> documents = future.get().getDocuments();

        if (!documents.isEmpty()) {

            return documents.get(0).toObject(User.class);
        }

        return null;
    }

    public User findByEmail(String email) throws ExecutionException, InterruptedException {
        ApiFuture<com.google.cloud.firestore.QuerySnapshot> future = usersCollection.whereEqualTo("email", email)
                .limit(1).get();
        List<com.google.cloud.firestore.QueryDocumentSnapshot> documents = future.get().getDocuments();
        if (!documents.isEmpty()) {
            return documents.get(0).toObject(User.class);
        }
        return null;
    }

    public java.util.Optional<User> findByAccessCode(String accessCode)
            throws ExecutionException, InterruptedException {
        ApiFuture<com.google.cloud.firestore.QuerySnapshot> future = usersCollection
                .whereEqualTo("accessCode", accessCode).limit(1).get();
        List<com.google.cloud.firestore.QueryDocumentSnapshot> documents = future.get().getDocuments();
        if (!documents.isEmpty()) {
            return java.util.Optional.of(documents.get(0).toObject(User.class));
        }
        return java.util.Optional.empty();
    }

    public ApiFuture<com.google.cloud.firestore.QuerySnapshot> findByParentId(String parentId) {
        return usersCollection
                .whereEqualTo("parentId", parentId)
                .whereEqualTo("role", User.Role.CHILD.name())
                .get();
    }

    public ApiFuture<WriteResult> delete(String userId) {
        return usersCollection.document(userId).delete();
    }

    public User findByAsaasCustomerId(String asaasCustomerId)
            throws ExecutionException, InterruptedException {
        ApiFuture<com.google.cloud.firestore.QuerySnapshot> future = usersCollection
                .whereEqualTo("asaasCustomerId", asaasCustomerId).limit(1).get();
        List<com.google.cloud.firestore.QueryDocumentSnapshot> documents = future.get().getDocuments();
        if (!documents.isEmpty()) {
            return documents.get(0).toObject(User.class);
        }
        return null;
    }

    public User findByLastCheckoutSessionId(String checkoutSessionId)
            throws ExecutionException, InterruptedException {
        ApiFuture<com.google.cloud.firestore.QuerySnapshot> future = usersCollection
                .whereEqualTo("lastCheckoutSessionId", checkoutSessionId).limit(1).get();
        List<com.google.cloud.firestore.QueryDocumentSnapshot> documents = future.get().getDocuments();
        if (!documents.isEmpty()) {
            return documents.get(0).toObject(User.class);
        }
        return null;
    }
}
