package dio.budgeting.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Aggregate root of the budgeting domain: a single financial movement
 * (an income or an expense) reported by the user, either through the REST
 * API or through the voice assistant.
 * <p>
 * Modeled as a class (not a record) because it has identity ({@link TransactionId})
 * and its validation rules may evolve independently of its field list.
 */
public class Transaction {

    private static final int MAX_DESCRIPTION_LENGTH = 140;
    private static final int MAX_CATEGORY_LENGTH = 60;

    private final TransactionId id;
    private final String description;
    private final Money amount;
    private final TransactionType type;
    private final String category;
    private final LocalDate occurredAt;
    private final Instant createdAt;

    private Transaction(TransactionId id, String description, Money amount, TransactionType type,
                         String category, LocalDate occurredAt, Instant createdAt) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.occurredAt = occurredAt;
        this.createdAt = createdAt;
    }

    /**
     * Creates a brand-new transaction, applying every domain validation rule.
     * This is the single entry point used by both the REST controller and the
     * AI tool-calling layer, so a voice command can never bypass a rule that a
     * regular HTTP request would have to follow.
     */
    public static Transaction create(String description, Money amount, TransactionType type,
                                      String category, LocalDate occurredAt) {
        String cleanDescription = requireText(description, "Description", MAX_DESCRIPTION_LENGTH);
        String cleanCategory = requireText(category, "Category", MAX_CATEGORY_LENGTH);
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(type, "Type must not be null");
        LocalDate date = occurredAt == null ? LocalDate.now() : occurredAt;
        if (date.isAfter(LocalDate.now())) {
            throw new InvalidTransactionException("occurredAt must not be in the future: " + date);
        }
        return new Transaction(TransactionId.generate(), cleanDescription, amount, type, cleanCategory, date,
                Instant.now());
    }

    /**
     * Rebuilds a transaction already known to be valid (e.g. read back from the
     * database). Used by the persistence adapter instead of {@link #create}.
     */
    public static Transaction restore(TransactionId id, String description, Money amount, TransactionType type,
                                       String category, LocalDate occurredAt, Instant createdAt) {
        Objects.requireNonNull(id, "Id must not be null");
        return new Transaction(id, description, amount, type, category, occurredAt, createdAt);
    }

    private static String requireText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new InvalidTransactionException(field + " must not be blank");
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new InvalidTransactionException(field + " must not exceed " + maxLength + " characters");
        }
        return trimmed;
    }

    public TransactionId id() {
        return id;
    }

    public String description() {
        return description;
    }

    public Money amount() {
        return amount;
    }

    public TransactionType type() {
        return type;
    }

    public String category() {
        return category;
    }

    public LocalDate occurredAt() {
        return occurredAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    /** Signed amount used when computing balances: positive for income, negative for expense. */
    public java.math.BigDecimal signedAmount() {
        return type == TransactionType.EXPENSE ? amount.amount().negate() : amount.amount();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
