package com.fazquepaga.taskandpay.tasks;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import org.springframework.stereotype.Repository;

@Repository
public class TaskRepository {

    private static final String USERS_COLLECTION = "users";
    private static final String TASKS_SUBCOLLECTION = "tasks";
    private final Firestore firestore;

    public TaskRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference getTasksCollection(String userId) {
        return firestore.collection(USERS_COLLECTION).document(userId).collection(TASKS_SUBCOLLECTION);
    }

    public ApiFuture<WriteResult> save(String userId, Task task) {
        CollectionReference tasksCollection = getTasksCollection(userId);
        if (task.getId() == null || task.getId().isEmpty()) {
            DocumentReference docRef = tasksCollection.document();
            task.setId(docRef.getId());
            return docRef.set(task);
        } else {
            return tasksCollection.document(task.getId()).set(task);
        }
    }

        public ApiFuture<QuerySnapshot> findTasksByUserId(String userId) {

            return getTasksCollection(userId).get();

        }

    

        public ApiFuture<com.google.cloud.firestore.DocumentSnapshot> findById(String userId, String taskId) {

            return getTasksCollection(userId).document(taskId).get();

        }

    }

    