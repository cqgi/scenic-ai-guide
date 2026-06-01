package com.scenic.ai.visitor;

import com.scenic.ai.common.ApiResponse;
import com.scenic.ai.interaction.FeedbackService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/visitor/feedback")
public class VisitorFeedbackController {

    private final FeedbackService feedbackService;

    public VisitorFeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public ApiResponse<FeedbackService.FeedbackResponse> submit(@Valid @RequestBody FeedbackRequest request) {
        return ApiResponse.ok(feedbackService.submit(new FeedbackService.FeedbackRequest(
                request.sessionToken(),
                request.interactionId(),
                request.score(),
                request.comment()
        )));
    }

    public record FeedbackRequest(@NotBlank String sessionToken, @NotNull Long interactionId, @Min(1) @Max(5) int score, String comment) {
    }
}
