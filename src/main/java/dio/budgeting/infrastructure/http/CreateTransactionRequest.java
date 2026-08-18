package dio.budgeting.infrastructure.http;

import dio.budgeting.application.CreateTransactionCommand;
import dio.budgeting.domain.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * REST request body for creating a transaction. Bean Validation annotations
 * give a fast, framework-level 400 before the request ever reaches the use
 * case; {@link dio.budgeting.domain.Transaction#create} still re-checks the
 * same invariants, so the AI tool-calling path (which does not go through
 * this DTO) is equally protected.
 */
public record CreateTransactionRequest(
        @NotBlank(message = "description must not be blank")
        String description,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "type is required")
        TransactionType type,

        @NotBlank(message = "category must not be blank")
        String category,

        @PastOrPresent(message = "occurredAt must not be in the future")
        LocalDate occurredAt
) {

    public CreateTransactionCommand toCommand() {
        return new CreateTransactionCommand(description, amount, type, category, occurredAt);
    }
}
