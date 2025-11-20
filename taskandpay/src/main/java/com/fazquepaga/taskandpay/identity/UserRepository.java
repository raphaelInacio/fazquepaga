package com.fazquepaga.taskandpay.identity;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ExecutionException;

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

    

        public User findByPhoneNumber(String phoneNumber) throws ExecutionException, InterruptedException {

            // This is not an efficient query and will not scale.

            // A real implementation would need a secondary index on the phoneNumber field.

            for (DocumentSnapshot doc : usersCollection.get().get().getDocuments()) {

                User user = doc.toObject(User.class);

                if (user != null && phoneNumber.equals(user.getPhoneNumber())) {

                    return user;

                }

            }

            return null;

        }

    }

    