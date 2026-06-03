package com.scenic.ai.digitalhuman;

import com.scenic.ai.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DigitalHumanController {

    private final DigitalHumanService digitalHumanService;

    public DigitalHumanController(DigitalHumanService digitalHumanService) {
        this.digitalHumanService = digitalHumanService;
    }

    @GetMapping("/digital-human/default")
    public ApiResponse<DigitalHumanService.DigitalHumanProfileResponse> publicDefaultProfile() {
        return ApiResponse.ok(digitalHumanService.defaultProfile());
    }

    @GetMapping("/admin/digital-human/default")
    public ApiResponse<DigitalHumanService.DigitalHumanProfileResponse> adminDefaultProfile() {
        return ApiResponse.ok(digitalHumanService.defaultProfile());
    }

    @PutMapping("/admin/digital-human/default")
    public ApiResponse<DigitalHumanService.DigitalHumanProfileResponse> updateDefaultProfile(
            @Valid @RequestBody UpdateDigitalHumanProfilePayload payload
    ) {
        return ApiResponse.ok(digitalHumanService.updateDefault(new DigitalHumanService.UpdateDigitalHumanProfileRequest(
                payload.scenicAreaId(),
                payload.name(),
                payload.avatarUrl(),
                payload.modelUrl(),
                payload.voiceCode(),
                payload.clothingStyle(),
                payload.welcomeText(),
                payload.personaPrompt(),
                payload.enabled()
        )));
    }

    public record UpdateDigitalHumanProfilePayload(
            @NotNull Long scenicAreaId,
            @NotBlank String name,
            String avatarUrl,
            String modelUrl,
            String voiceCode,
            String clothingStyle,
            @NotBlank String welcomeText,
            @NotBlank String personaPrompt,
            boolean enabled
    ) {
    }
}
