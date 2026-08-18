package dio.budgeting.infrastructure.http;

import dio.budgeting.infrastructure.ai.VoiceAssistantResult;

import java.util.Base64;

/**
 * HTTP response for the voice assistant flow. The synthesized reply is
 * returned as a base64 string inside JSON (rather than as a raw
 * {@code audio/mpeg} body) so the transcript and text reply can travel
 * alongside it in a single, easy-to-inspect response — handy for testing
 * with curl/Postman without a binary-aware client.
 */
public record VoiceAssistantResponse(String transcript, String reply, String audioBase64, String audioMimeType) {

    public static VoiceAssistantResponse from(VoiceAssistantResult result) {
        String audioBase64 = Base64.getEncoder().encodeToString(result.audioReply());
        return new VoiceAssistantResponse(result.transcript(), result.reply(), audioBase64, "audio/mpeg");
    }
}
