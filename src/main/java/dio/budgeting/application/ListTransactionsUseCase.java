package dio.budgeting.application;

import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.domain.TransactionType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Lists transactions, optionally filtered by type and/or a date range.
 * Backs both {@code GET /api/transactions} and the "list transactions"
 * AI tool.
 */
@Component
public class ListTransactionsUseCase {

    private final TransactionRepository transactionRepository;

    public ListTransactionsUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<TransactionView> execute(TransactionType type, LocalDate start, LocalDate end) {
        List<Transaction> transactions;
        if (type != null) {
            transactions = transactionRepository.findByType(type);
        } else if (start != null || end != null) {
            transactions = transactionRepository.findByOccurredAtBetween(start, end);
        } else {
            transactions = transactionRepository.findAll();
        }

        return transactions.stream()
                .filter(t -> type == null || t.type() == type)
                .filter(t -> start == null || !t.occurredAt().isBefore(start))
                .filter(t -> end == null || !t.occurredAt().isAfter(end))
                .sorted((a, b) -> b.occurredAt().compareTo(a.occurredAt()))
                .map(TransactionView::from)
                .toList();
    }
}
