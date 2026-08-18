package dio.budgeting.infrastructure.http;

import dio.budgeting.infrastructure.ai.VoiceAssistantResult;
import dio.budgeting.infrastructure.ai.VoiceAssistantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Entry point for the AI voice assistant: receives an audio command,
 * transcribes it, lets the model act on it through {@code @Tool}-exposed
 * use cases, and returns both the textual and the spoken reply.
 */
@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final VoiceAssistantService voiceAssistantService;

    public AssistantController(VoiceAssistantService voiceAssistantService) {
        this.voiceAssistantService = voiceAssistantService;
    }

    @PostMapping(path = "/voice", consumes = "multipart/form-data")
    public VoiceAssistantResponse handleVoiceCommand(@RequestParam("audio") MultipartFile audio) {
        VoiceAssistantResult result = voiceAssistantService.handleVoiceCommand(audio);
        return VoiceAssistantResponse.from(result);
    }

    @PostMapping("/text")
    public TextAssistantResponse handleTextCommand(@Valid @RequestBody TextAssistantRequest request) {
        String reply = voiceAssistantService.handleTextCommand(request.message());
        return new TextAssistantResponse(reply);
    }
}
