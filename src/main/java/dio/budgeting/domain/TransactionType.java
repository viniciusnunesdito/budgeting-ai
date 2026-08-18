package dio.budgeting.domain;

/**
 * A financial transaction is either money coming in ({@link #INCOME}) or
 * money going out ({@link #EXPENSE}). The amount stored on {@link Transaction}
 * is always non-negative; the type defines its sign when computing balances.
 */
public enum TransactionType {
    INCOME,
    EXPENSE;

    public static TransactionType parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new InvalidTransactionException("Transaction type must not be blank");
        }
        String normalized = raw.trim().toUpperCase();
        // Accept common Portuguese synonyms so the AI tool layer can pass through
        // whatever word the language model picks without a translation table.
        return switch (normalized) {
            case "INCOME", "RECEITA", "ENTRADA", "GANHO" -> INCOME;
            case "EXPENSE", "DESPESA", "SAIDA", "SAÍDA", "GASTO" -> EXPENSE;
            default -> throw new InvalidTransactionException(
                    "Unknown transaction type: " + raw + " (expected INCOME or EXPENSE)");
        };
    }
}
