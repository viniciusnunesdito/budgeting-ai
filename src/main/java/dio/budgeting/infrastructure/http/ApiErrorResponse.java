package dio.budgeting.infrastructure.http;

import java.time.Instant;
import java.util.List;

/** Uniform error body returned by {@link GlobalExceptionHandler}. */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        List<String> messages
) {

    public static ApiErrorResponse of(int status, String error, List<String> messages) {
        return new ApiErrorResponse(Instant.now(), status, error, messages);
    }
}
