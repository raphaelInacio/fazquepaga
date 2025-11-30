package com.fazquepaga.taskandpay.ai;

import com.fazquepaga.taskandpay.allowance.Transaction;
import com.fazquepaga.taskandpay.allowance.TransactionRepository;
import com.fazquepaga.taskandpay.identity.User;
import com.fazquepaga.taskandpay.identity.UserRepository;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

@Service
public class AiInsightServiceImpl implements AiInsightService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ChatModel chatModel;

    public AiInsightServiceImpl(
            TransactionRepository transactionRepository,
            UserRepository userRepository,
            ChatModel chatModel) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.chatModel = chatModel;
    }

    @Override
    public String getInsights(String childId) {
        try {
            // Fetch child and transactions
            User child = userRepository.findByIdSync(childId);
            if (child == null) {
                return "Não foi possível gerar insights no momento.";
            }

            List<QueryDocumentSnapshot> documents =
                    transactionRepository.findByChildId(childId).getDocuments();
            List<Transaction> transactions =
                    documents.stream()
                            .map(doc -> doc.toObject(Transaction.class))
                            .collect(Collectors.toList());

            // If no transactions, return a motivational message
            if (transactions.isEmpty()) {
                return "Comece a completar tarefas para ganhar sua mesada e acompanhar seu"
                        + " progresso financeiro!";
            }

            // Calculate financial statistics
            BigDecimal totalCredits =
                    transactions.stream()
                            .filter(t -> t.getType() == Transaction.TransactionType.CREDIT)
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalDebits =
                    transactions.stream()
                            .filter(t -> t.getType() == Transaction.TransactionType.DEBIT)
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal balance = child.getBalance() != null ? child.getBalance() : BigDecimal.ZERO;

            // Calculate savings rate
            int savingsRate = 0;
            if (totalCredits.compareTo(BigDecimal.ZERO) > 0) {
                savingsRate =
                        balance.divide(totalCredits, 2, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .intValue();
            }

            // Generate AI insight using Gemini
            PromptTemplate promptTemplate =
                    new PromptTemplate(
                            """
                            Você é um coach financeiro amigável para crianças. Baseado nos dados financeiros abaixo, gere uma mensagem curta (máximo 2 frases) em português, encorajadora e educativa:

                            - Nome da criança: {childName}
                            - Total ganho: R$ {totalCredits}
                            - Total gasto: R$ {totalDebits}
                            - Saldo atual: R$ {balance}
                            - Taxa de economia: {savingsRate}%
                            - Número de transações: {transactionCount}

                            A mensagem deve ser positiva, motivadora e dar uma dica prática. Use emojis se apropriado.
                            """);

            Prompt prompt =
                    promptTemplate.create(
                            Map.of(
                                    "childName", child.getName(),
                                    "totalCredits",
                                            totalCredits
                                                    .setScale(2, RoundingMode.HALF_UP)
                                                    .toString(),
                                    "totalDebits",
                                            totalDebits
                                                    .setScale(2, RoundingMode.HALF_UP)
                                                    .toString(),
                                    "balance", balance.setScale(2, RoundingMode.HALF_UP).toString(),
                                    "savingsRate", savingsRate,
                                    "transactionCount", transactions.size()));

            ChatResponse response = chatModel.call(prompt);
            return response.getResult().getOutput().getText().trim();

        } catch (ExecutionException | InterruptedException e) {
            return "Não foi possível gerar insights no momento. Tente novamente mais tarde.";
        }
    }
}
