package dio.budgeting.application;

import dio.budgeting.domain.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Read model returned by every use case that exposes a transaction, shared by
 * the REST controller and the AI tool layer so both present identical data.
 */
public record TransactionView(
        String id,
        String description,
        BigDecimal amount,
        String type,
        String category,
        LocalDate occurredAt
) {

    public static TransactionView from(Transaction transaction) {
        return new TransactionView(
                transaction.id().toString(),
                transaction.description(),
                transaction.amount().amount(),
                transaction.type().name(),
                transaction.category(),
                transaction.occurredAt()
        );
    }
}
