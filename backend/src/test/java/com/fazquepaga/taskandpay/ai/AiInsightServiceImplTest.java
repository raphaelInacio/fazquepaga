package com.fazquepaga.taskandpay.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fazquepaga.taskandpay.allowance.Transaction;
import com.fazquepaga.taskandpay.allowance.TransactionRepository;
import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

class AiInsightServiceImplTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private ChatModel chatModel;
    @Mock private QuerySnapshot querySnapshot;
    @Mock private QueryDocumentSnapshot queryDocumentSnapshot;

    private AiInsightServiceImpl aiInsightService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        aiInsightService =
                new AiInsightServiceImpl(transactionRepository, userRepository, chatModel);
    }

    @Test
    void shouldReturnMotivationalMessageWhenNoTransactions()
            throws ExecutionException, InterruptedException {
        // Arrange
        String childId = "child123";
        User child = User.builder().id(childId).name("Maria").balance(BigDecimal.ZERO).build();

        when(userRepository.findByIdSync(childId)).thenReturn(child);
        when(transactionRepository.findByChildId(childId)).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(new ArrayList<>());

        // Act
        String insight = aiInsightService.getInsights(childId);

        // Assert
        assertNotNull(insight);
        assertEquals(
                "Comece a completar tarefas para ganhar sua mesada e acompanhar seu progresso"
                        + " financeiro!",
                insight);
    }

    @Test
    void shouldGenerateAiInsightWithTransactions() throws ExecutionException, InterruptedException {
        // Arrange
        String childId = "child123";
        User child =
                User.builder().id(childId).name("Maria").balance(new BigDecimal("70.00")).build();

        Transaction transaction1 =
                Transaction.builder()
                        .id("tx1")
                        .childId(childId)
                        .amount(new BigDecimal("50.00"))
                        .description("Limpou o quarto")
                        .date(Instant.now())
                        .type(Transaction.TransactionType.CREDIT)
                        .build();

        Transaction transaction2 =
                Transaction.builder()
                        .id("tx2")
                        .childId(childId)
                        .amount(new BigDecimal("30.00"))
                        .description("Comprou um brinquedo")
                        .date(Instant.now())
                        .type(Transaction.TransactionType.DEBIT)
                        .build();

        when(userRepository.findByIdSync(childId)).thenReturn(child);
        when(transactionRepository.findByChildId(childId)).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments())
                .thenReturn(List.of(queryDocumentSnapshot, queryDocumentSnapshot));
        when(queryDocumentSnapshot.toObject(Transaction.class))
                .thenReturn(transaction1)
                .thenReturn(transaction2);

        String aiResponse = "Parabéns Maria! Você economizou 70% do que ganhou. Continue assim! 🎉";
        Generation generation = new Generation(new AssistantMessage(aiResponse));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        // Act
        String insight = aiInsightService.getInsights(childId);

        // Assert
        assertNotNull(insight);
        assertEquals(aiResponse, insight);
    }

    @Test
    void shouldReturnErrorMessageWhenChildNotFound()
            throws ExecutionException, InterruptedException {
        // Arrange
        String childId = "nonexistent";
        when(userRepository.findByIdSync(anyString())).thenReturn(null);

        // Act
        String insight = aiInsightService.getInsights(childId);

        // Assert
        assertEquals("Não foi possível gerar insights no momento.", insight);
    }

    @Test
    void shouldReturnErrorMessageOnException() throws ExecutionException, InterruptedException {
        // Arrange
        String childId = "child123";
        when(userRepository.findByIdSync(childId))
                .thenThrow(new ExecutionException("Firestore error", new RuntimeException()));

        // Act
        String insight = aiInsightService.getInsights(childId);

        // Assert
        assertEquals(
                "Não foi possível gerar insights no momento. Tente novamente mais tarde.", insight);
    }
}
