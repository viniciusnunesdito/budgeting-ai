package dio.budgeting.application;

import dio.budgeting.domain.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Input for {@link CreateTransactionUseCase}. A plain immutable record: it is
 * a transport/DTO-style object with no identity, built the same way whether
 * it originates from the REST controller or from an AI tool call.
 */
public record CreateTransactionCommand(
        String description,
        BigDecimal amount,
        TransactionType type,
        String category,
        LocalDate occurredAt
) {
}
