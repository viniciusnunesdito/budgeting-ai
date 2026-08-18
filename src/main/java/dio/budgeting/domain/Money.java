package dio.budgeting.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Immutable value object representing a strictly positive monetary amount in BRL.
 * <p>
 * A {@link Transaction} always stores "how much money changed hands" as a
 * positive {@code Money}; whether it increases or decreases the balance is
 * decided by its {@link TransactionType}, not by the sign of the amount.
 * Centralizing the scale/sign rule here means every layer (domain, REST
 * DTOs, AI tools) shares the same validation instead of re-checking amounts
 * ad hoc — one of the validation gaps the base project leaves open.
 */
public record Money(BigDecimal amount) {

    public Money {
        if (amount == null) {
            throw new InvalidTransactionException("Amount must not be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Amount must be greater than zero");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    public static Money of(double amount) {
        return new Money(BigDecimal.valueOf(amount));
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }
}
