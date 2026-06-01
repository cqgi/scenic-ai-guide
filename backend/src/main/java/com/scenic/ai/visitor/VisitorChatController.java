package com.scenic.ai.visitor;

import com.scenic.ai.ai.VisitorChatService;
import com.scenic.ai.ai.VisitorVoiceChatService;
import com.scenic.ai.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/visitor/chat")
public class VisitorChatController {

    private final VisitorChatService visitorChatService;
    private final VisitorVoiceChatService visitorVoiceChatService;

    public VisitorChatController(VisitorChatService visitorChatService, VisitorVoiceChatService visitorVoiceChatService) {
        this.visitorChatService = visitorChatService;
        this.visitorVoiceChatService = visitorVoiceChatService;
    }

    @PostMapping("/text")
    public ApiResponse<VisitorChatService.ChatResponse> chatText(@Valid @RequestBody TextChatRequest request) {
        return ApiResponse.ok(visitorChatService.chatText(new VisitorChatService.ChatRequest(
                request.sessionToken(),
                request.query(),
                request.interests()
        )));
    }

    @PostMapping("/voice")
    public ApiResponse<VisitorChatService.ChatResponse> chatVoice(
            @RequestParam @NotBlank String sessionToken,
            @RequestParam(required = false) List<String> interests,
            @RequestPart MultipartFile audio
    ) {
        return ApiResponse.ok(visitorVoiceChatService.chatVoice(sessionToken, audio, interests));
    }

    public record TextChatRequest(@NotBlank String sessionToken, @NotBlank String query, List<String> interests) {
    }
}
