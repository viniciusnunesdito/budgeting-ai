package dio.budgeting.infrastructure.http;

import jakarta.validation.constraints.NotBlank;

/**
 * Text-only entry point to the assistant. This is one of the project's
 * evolutions: it lets anyone exercise the exact same intent-understanding
 * and tool-calling pipeline as the voice flow, without needing to record or
 * upload an audio file — handy for quick manual tests and for automated
 * integration tests.
 */
public record TextAssistantRequest(
        @NotBlank(message = "message must not be blank") String message
) {
}
