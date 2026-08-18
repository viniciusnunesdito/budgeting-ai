package dio.budgeting.domain;

/**
 * Raised when a transaction (or one of its value objects) violates a domain
 * invariant, e.g. a non-positive amount or a blank description.
 */
public class InvalidTransactionException extends RuntimeException {

    public InvalidTransactionException(String message) {
        super(message);
    }
}
