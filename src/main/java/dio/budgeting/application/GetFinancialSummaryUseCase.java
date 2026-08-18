package dio.budgeting.application;

import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.domain.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Computes a financial summary (total income, total expense and balance) for
 * an optional date range. This is the main functional evolution added on
 * top of the base project: a new kind of financial query, available through
 * {@code GET /api/transactions/summary} and through a new {@code @Tool} so
 * the voice assistant can answer "how am I doing this month?"-style
 * questions instead of only creating/listing raw transactions.
 */
@Component
public class GetFinancialSummaryUseCase {

    private final TransactionRepository transactionRepository;

    public GetFinancialSummaryUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public FinancialSummaryView execute(LocalDate start, LocalDate end) {
        List<Transaction> transactions = (start == null && end == null)
                ? transactionRepository.findAll()
                : transactionRepository.findByOccurredAtBetween(start, end);

        BigDecimal totalIncome = sum(transactions, TransactionType.INCOME);
        BigDecimal totalExpense = sum(transactions, TransactionType.EXPENSE);

        return new FinancialSummaryView(
                start,
                end,
                totalIncome,
                totalExpense,
                totalIncome.subtract(totalExpense),
                transactions.size()
        );
    }

    private BigDecimal sum(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.type() == type)
                .map(t -> t.amount().amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
