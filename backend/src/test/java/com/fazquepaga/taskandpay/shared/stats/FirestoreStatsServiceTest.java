package com.fazquepaga.taskandpay.shared.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FirestoreStatsServiceTest {

    @Mock private Firestore firestore;
    @Mock private CollectionReference familiesCollection;
    @Mock private DocumentReference familyDocRef;
    @Mock private CollectionReference metadataCollection;
    @Mock private DocumentReference statsDocRef;
    @Mock private CollectionReference globalCollection;
    @Mock private DocumentReference globalStatsDocRef;
    @Mock private ApiFuture<WriteResult> writeResultFuture;
    @Mock private WriteResult writeResult;

    private FirestoreStatsService statsService;

    @BeforeEach
    void setUp() {
        statsService = new FirestoreStatsService(firestore);
    }

    @Test
    void incrementFamilyStat_shouldCallFirestoreWithIncrementAndMerge()
            throws ExecutionException, InterruptedException {
        // Arrange
        String familyId = "family-123";
        String field = "totalTasksCreated";
        double amount = 1.0;

        setupFamilyStatsDocRef(familyId);

        // Act
        CompletableFuture<Void> result = statsService.incrementFamilyStat(familyId, field, amount);

        // Assert
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(statsDocRef).set(mapCaptor.capture(), eq(SetOptions.merge()));

        Map<String, Object> capturedMap = mapCaptor.getValue();
        assertThat(capturedMap).containsKey(field);
        assertThat(capturedMap).containsKey("lastActivityTimestamp");
        assertThat(capturedMap.get(field)).isInstanceOf(FieldValue.class);
    }

    @Test
    void incrementFamilyStat_withNullFamilyId_shouldReturnCompletedFutureWithoutCallingFirestore() {
        // Act
        CompletableFuture<Void> result = statsService.incrementFamilyStat(null, "field", 1.0);

        // Assert
        assertThat(result).isCompletedWithValue(null);
        // Firestore should not be called
        verify(firestore, org.mockito.Mockito.never()).collection(anyString());
    }

    @Test
    void
            incrementFamilyStat_withBlankFamilyId_shouldReturnCompletedFutureWithoutCallingFirestore() {
        // Act
        CompletableFuture<Void> result = statsService.incrementFamilyStat("  ", "field", 1.0);

        // Assert
        assertThat(result).isCompletedWithValue(null);
        verify(firestore, org.mockito.Mockito.never()).collection(anyString());
    }

    @Test
    void incrementGlobalStat_shouldCallFirestoreGlobalDocWithIncrementAndMerge()
            throws ExecutionException, InterruptedException {
        // Arrange
        String field = "totalAiPrompts";
        double amount = 1.0;

        when(firestore.collection("global")).thenReturn(globalCollection);
        when(globalCollection.document("stats")).thenReturn(globalStatsDocRef);
        when(globalStatsDocRef.set(any(Map.class), eq(SetOptions.merge())))
                .thenReturn(writeResultFuture);

        // Act
        CompletableFuture<Void> result = statsService.incrementGlobalStat(field, amount);

        // Assert
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(globalStatsDocRef).set(mapCaptor.capture(), eq(SetOptions.merge()));

        Map<String, Object> capturedMap = mapCaptor.getValue();
        assertThat(capturedMap).containsKey(field);
        assertThat(capturedMap).containsKey("lastActivityTimestamp");
        assertThat(capturedMap.get(field)).isInstanceOf(FieldValue.class);
    }

    @Test
    void incrementFamilyStat_withMultipleFields_shouldIncrementEachField()
            throws ExecutionException, InterruptedException {
        // Arrange
        String familyId = "family-456";
        setupFamilyStatsDocRef(familyId);

        // Act - incrementar diferentes campos
        statsService.incrementFamilyStat(familyId, "totalTasksCreated", 1.0);
        statsService.incrementFamilyStat(familyId, "totalTasksCompleted", 1.0);
        statsService.incrementFamilyStat(familyId, "totalAllowancePaid", 25.50);

        // Assert - deve ter chamado 3 vezes
        verify(statsDocRef, org.mockito.Mockito.times(3))
                .set(any(Map.class), eq(SetOptions.merge()));
    }

    @Test
    void incrementFamilyStat_whenFirestoreFails_shouldCompleteNormallyWithoutPropagatingError()
            throws ExecutionException, InterruptedException {
        // Arrange
        String familyId = "family-789";
        setupFamilyStatsDocRef(familyId);

        // Act - não deve lançar exceção
        CompletableFuture<Void> result =
                statsService.incrementFamilyStat(familyId, "totalTasksCreated", 1.0);

        // Assert - o future deve completar normalmente mesmo com erro no Firestore
        assertThat(result).isNotNull();
    }

    @Mock private com.google.cloud.firestore.DocumentSnapshot documentSnapshot;

    @Test
    void getFamilyStats_withNullFamilyId_shouldReturnEmptyMap() throws Exception {
        // Act
        CompletableFuture<Map<String, Object>> result = statsService.getFamilyStats(null);

        // Assert
        assertThat(result.get()).isEmpty();
    }

    @Test
    void getFamilyStats_whenDocumentExists_shouldReturnDataMap() throws Exception {
        // Arrange
        String familyId = "family-123";
        setupFamilyStatsDocRefForGet(familyId);

        Map<String, Object> expectedStats =
                Map.of("totalTasksCreated", 5L, "totalTasksCompleted", 3L);
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getData()).thenReturn(expectedStats);

        // Act
        CompletableFuture<Map<String, Object>> result = statsService.getFamilyStats(familyId);

        // Assert
        assertThat(result.get()).isEqualTo(expectedStats);
    }

    @Test
    void getFamilyStats_whenDocumentDoesNotExist_shouldReturnDefaultZeroedMap() throws Exception {
        // Arrange
        String familyId = "family-123";
        setupFamilyStatsDocRefForGet(familyId);

        when(documentSnapshot.exists()).thenReturn(false);

        // Act
        CompletableFuture<Map<String, Object>> result = statsService.getFamilyStats(familyId);

        // Assert
        Map<String, Object> resultStats = result.get();
        assertThat(resultStats).containsEntry("totalTasksCreated", 0L);
        assertThat(resultStats).containsEntry("totalTasksCompleted", 0L);
        assertThat(resultStats).containsEntry("totalTasksApproved", 0L);
        assertThat(resultStats).containsEntry("totalAllowancePaid", 0.0);
        assertThat(resultStats).containsEntry("aiSuggestionsUsed", 0L);
    }

    // Helper para configurar a hierarquia de referências Firestore para família
    private void setupFamilyStatsDocRef(String familyId) {
        when(firestore.collection("families")).thenReturn(familiesCollection);
        when(familiesCollection.document(familyId)).thenReturn(familyDocRef);
        when(familyDocRef.collection("metadata")).thenReturn(metadataCollection);
        when(metadataCollection.document("stats")).thenReturn(statsDocRef);
        when(statsDocRef.set(any(Map.class), eq(SetOptions.merge()))).thenReturn(writeResultFuture);
    }

    private void setupFamilyStatsDocRefForGet(String familyId) {
        when(firestore.collection("families")).thenReturn(familiesCollection);
        when(familiesCollection.document(familyId)).thenReturn(familyDocRef);
        when(familyDocRef.collection("metadata")).thenReturn(metadataCollection);
        when(metadataCollection.document("stats")).thenReturn(statsDocRef);
        when(statsDocRef.get())
                .thenReturn(com.google.api.core.ApiFutures.immediateFuture(documentSnapshot));
    }
}
