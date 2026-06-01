package com.scenic.ai.ai;

import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VisitorVoiceChatService {

    private final SpeechToTextClient speechToTextClient;
    private final VisitorChatService visitorChatService;

    public VisitorVoiceChatService(SpeechToTextClient speechToTextClient, VisitorChatService visitorChatService) {
        this.speechToTextClient = speechToTextClient;
        this.visitorChatService = visitorChatService;
    }

    public VisitorChatService.ChatResponse chatVoice(String sessionToken, MultipartFile audio, List<String> interests) {
        if (audio == null || audio.isEmpty()) {
            throw new IllegalArgumentException("语音文件不能为空");
        }
        try {
            SpeechToTextClient.TranscriptionResult result = speechToTextClient.transcribe(
                    audio.getBytes(),
                    audio.getOriginalFilename(),
                    audio.getContentType()
            );
            if (!result.success()) {
                throw new IllegalArgumentException("语音识别失败: " + result.errorMessage());
            }
            return visitorChatService.chatVoice(new VisitorChatService.ChatRequest(sessionToken, result.text(), interests), result.text());
        } catch (IOException exception) {
            throw new IllegalArgumentException("语音文件读取失败");
        }
    }
}
