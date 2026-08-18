package dio.budgeting.infrastructure.ai;

import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Orchestrates the full voice-command flow described in the challenge:
 * <ol>
 *   <li>transcribe the uploaded audio to text ({@link TranscriptionModel});</li>
 *   <li>let the {@link ChatClient} understand the intent and call the right
 *       {@code @Tool} from {@link TransactionTools} to touch real data;</li>
 *   <li>convert the model's final answer back to speech ({@link TextToSpeechModel}).</li>
 * </ol>
 * This orchestration is integration glue over external AI models, so — per
 * the project's layering — it lives in infrastructure rather than in the
 * application layer; the actual business logic stays in the use cases.
 */
@Service
public class VoiceAssistantService {

    private static final long MAX_AUDIO_SIZE_BYTES = 10L * 1024 * 1024; // 10MB, matches server upload limit

    private final ChatClient chatClient;
    private final TranscriptionModel transcriptionModel;
    private final TextToSpeechModel textToSpeechModel;

    public VoiceAssistantService(ChatClient chatClient, TranscriptionModel transcriptionModel,
                                  TextToSpeechModel textToSpeechModel) {
        this.chatClient = chatClient;
        this.transcriptionModel = transcriptionModel;
        this.textToSpeechModel = textToSpeechModel;
    }

    /**
     * Full voice flow: audio in, transcript + text reply + spoken (mp3) reply out.
     */
    public VoiceAssistantResult handleVoiceCommand(MultipartFile audio) {
        validate(audio);
        String transcript = transcriptionModel.transcribe(audio.getResource());
        String reply = ask(transcript);
        byte[] audioReply = textToSpeechModel.call(reply);
        return new VoiceAssistantResult(transcript, reply, audioReply);
    }

    /**
     * Text-only shortcut for the same assistant: reuses the exact same
     * {@link ChatClient}/tools pipeline, skipping transcription and speech
     * synthesis. Useful to test and demo the tool-calling flow without
     * having to record audio for every request.
     */
    public String handleTextCommand(String message) {
        return ask(message);
    }

    private String ask(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }

    private void validate(MultipartFile audio) {
        if (audio == null || audio.isEmpty()) {
            throw new InvalidAudioException("Audio file must not be empty");
        }
        if (audio.getSize() > MAX_AUDIO_SIZE_BYTES) {
            throw new InvalidAudioException("Audio file exceeds the 10MB limit");
        }
    }
}
