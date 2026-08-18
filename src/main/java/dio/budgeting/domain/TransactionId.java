package dio.budgeting.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Strong typed identifier for {@link Transaction}.
 * <p>
 * Wrapping the raw {@link UUID} avoids mixing up identifiers from different
 * aggregates at compile time and keeps id-parsing/validation logic in one place.
 */
public record TransactionId(UUID value) {

    public TransactionId {
        Objects.requireNonNull(value, "TransactionId value must not be null");
    }

    public static TransactionId generate() {
        return new TransactionId(UUID.randomUUID());
    }

    public static TransactionId of(String rawValue) {
        Objects.requireNonNull(rawValue, "rawValue must not be null");
        try {
            return new TransactionId(UUID.fromString(rawValue));
        } catch (IllegalArgumentException ex) {
            throw new InvalidTransactionException("Invalid transaction id: " + rawValue);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
