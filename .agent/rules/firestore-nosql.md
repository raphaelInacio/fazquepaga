---
trigger: model_decision
---

# Firestore NoSQL Standards

## Data Modeling

- **NoSQL First**: Use Cloud Firestore as the primary database.
- **Collection Structure**:
  - `users/{userId}`: Root document for each user (parent).
  - `users/{userId}/tasks/{taskId}`: Subcollection for tasks.
  - `users/{userId}/ledger/{transactionId}`: Subcollection for financial records.
- **Document IDs**: Use auto-generated IDs unless there's a specific natural key (e.g., `email` for user lookup before auth).

## Data Access

- **Repository Pattern**: Encapsulate Firestore logic in `@Repository` classes.
- **Asynchronous Operations**: Firestore operations return `ApiFuture`.
  - Use `listener` callbacks or `get()` (blocking) carefully.
  - In Spring Boot, blocking with `.get()` is acceptable in `@Service` methods if running on standard internal threads, but prefer non-blocking if scaling requirements increase.
- **Collection Group Queries**: Use `firestore.collectionGroup("tasks")` for querying across all users (e.g., admin reporting or global schedulers).

## Best Practices

- **Transactional Writes**: Use `firestore.runTransaction()` for operations involving multiple documents (e.g., deducting allowance and creating a ledger entry).
- **Security Rules**: (Though managed effectively by backend logic here) maintain awareness that Firestore rules enforce frontend direct access if used.
- **Date Handling**: Store dates as `Timestamp` (Firestore native) or handle conversion from `LocalDateTime` in the Mapper.

## Common Patterns

```java
// Saving a document
DocumentReference docRef = collection.document();
task.setId(docRef.getId());
return docRef.set(task);

// Querying
ApiFuture<QuerySnapshot> query = collection.whereEqualTo("status", "PENDING").get();
```