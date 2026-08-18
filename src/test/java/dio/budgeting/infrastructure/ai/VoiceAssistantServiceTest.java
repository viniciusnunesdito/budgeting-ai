package dio.budgeting.infrastructure.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the voice-command orchestration, with the AI models mocked
 * out so the flow can be verified without calling OpenAI over the network.
 */
class VoiceAssistantServiceTest {

    @Mock
    private TranscriptionModel transcriptionModel;

    @Mock
    private TextToSpeechModel textToSpeechModel;

    private ChatClient chatClient;
    private ChatClient.ChatClientRequestSpec requestSpec;
    private ChatClient.CallResponseSpec responseSpec;

    private VoiceAssistantService voiceAssistantService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        chatClient = mock(ChatClient.class);
        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        responseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);

        voiceAssistantService = new VoiceAssistantService(chatClient, transcriptionModel, textToSpeechModel);
    }

    @Test
    void transcribesAsksTheModelAndSynthesizesTheReply() {
        MultipartFile audio = new MockMultipartFile("audio", "comando.mp3", "audio/mpeg", new byte[]{1, 2, 3});
        when(transcriptionModel.transcribe(any(Resource.class))).thenReturn("registra uma despesa de 10 reais");
        when(responseSpec.content()).thenReturn("Despesa registrada!");
        when(textToSpeechModel.call("Despesa registrada!")).thenReturn(new byte[]{9, 9, 9});

        VoiceAssistantResult result = voiceAssistantService.handleVoiceCommand(audio);

        assertEquals("registra uma despesa de 10 reais", result.transcript());
        assertEquals("Despesa registrada!", result.reply());
        assertArrayEquals(new byte[]{9, 9, 9}, result.audioReply());
    }

    @Test
    void rejectsEmptyAudio() {
        MultipartFile empty = new MockMultipartFile("audio", "empty.mp3", "audio/mpeg", new byte[0]);
        assertThrows(InvalidAudioException.class, () -> voiceAssistantService.handleVoiceCommand(empty));
    }

    @Test
    void textCommandSkipsTranscriptionAndSpeechSynthesis() {
        when(responseSpec.content()).thenReturn("Aqui está seu resumo.");

        String reply = voiceAssistantService.handleTextCommand("como estão minhas finanças?");

        assertEquals("Aqui está seu resumo.", reply);
    }
}
