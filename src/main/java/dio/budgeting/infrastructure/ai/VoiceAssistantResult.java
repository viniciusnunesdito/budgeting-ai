package dio.budgeting.infrastructure.ai;

/** Outcome of a full voice-command round trip: what was heard, what was decided, and how it sounds. */
public record VoiceAssistantResult(String transcript, String reply, byte[] audioReply) {
}
