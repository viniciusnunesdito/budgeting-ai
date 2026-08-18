package dio.budgeting.infrastructure.ai;

/**
 * Raised when the uploaded audio file cannot be processed (missing, empty or
 * too large). Kept separate from {@link dio.budgeting.domain.InvalidTransactionException}
 * because it is an infrastructure/input concern, not a domain rule.
 */
public class InvalidAudioException extends RuntimeException {

    public InvalidAudioException(String message) {
        super(message);
    }
}
