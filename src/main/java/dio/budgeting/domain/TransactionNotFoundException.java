package dio.budgeting.domain;

/**
 * Raised when a {@link Transaction} cannot be found by its {@link TransactionId}.
 */
public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(TransactionId id) {
        super("Transaction not found: " + id);
    }
}
