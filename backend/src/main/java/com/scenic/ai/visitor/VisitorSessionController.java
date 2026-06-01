package com.scenic.ai.visitor;

import com.scenic.ai.common.ApiResponse;
import com.scenic.ai.scenic.ScenicArea;
import com.scenic.ai.scenic.ScenicAreaRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/visitor/sessions")
public class VisitorSessionController {

    private final ScenicAreaRepository scenicAreaRepository;
    private final VisitorSessionRepository visitorSessionRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public VisitorSessionController(ScenicAreaRepository scenicAreaRepository, VisitorSessionRepository visitorSessionRepository) {
        this.scenicAreaRepository = scenicAreaRepository;
        this.visitorSessionRepository = visitorSessionRepository;
    }

    @PostMapping
    public ApiResponse<VisitorSessionResponse> create(@Valid @RequestBody CreateVisitorSessionRequest request) {
        ScenicArea area = scenicAreaRepository.findById(request.scenicAreaId())
                .filter(ScenicArea::isEnabled)
                .orElseThrow(() -> new IllegalArgumentException("景区不存在或已停用"));
        String interests = request.interests() == null ? "" : String.join(",", request.interests());
        VisitorSession session = visitorSessionRepository.save(new VisitorSession(
                area,
                newSessionToken(),
                interests,
                request.deviceType()
        ));
        return ApiResponse.ok(new VisitorSessionResponse(session.getId(), session.getSessionToken(), session.getInterests()));
    }

    private String newSessionToken() {
        byte[] bytes = new byte[24];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record CreateVisitorSessionRequest(@NotNull Long scenicAreaId, List<String> interests, String deviceType) {
    }

    public record VisitorSessionResponse(Long sessionId, String sessionToken, String interests) {
    }
}
