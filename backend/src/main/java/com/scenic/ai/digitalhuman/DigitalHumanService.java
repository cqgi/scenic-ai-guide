package com.scenic.ai.digitalhuman;

import com.scenic.ai.scenic.ScenicArea;
import com.scenic.ai.scenic.ScenicAreaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class DigitalHumanService {

    private final DigitalHumanProfileRepository profileRepository;
    private final ScenicAreaRepository scenicAreaRepository;

    public DigitalHumanService(DigitalHumanProfileRepository profileRepository, ScenicAreaRepository scenicAreaRepository) {
        this.profileRepository = profileRepository;
        this.scenicAreaRepository = scenicAreaRepository;
    }

    @Transactional
    public DigitalHumanProfileResponse defaultProfile() {
        DigitalHumanProfile profile = profileRepository.findFirstByEnabledTrueOrderByIdAsc()
                .or(() -> profileRepository.findAll().stream().findFirst())
                .orElseGet(this::createDefaultProfile);
        return DigitalHumanProfileResponse.from(profile);
    }

    @Transactional
    public DigitalHumanProfileResponse updateDefault(UpdateDigitalHumanProfileRequest request) {
        DigitalHumanProfile profile = profileRepository.findFirstByScenicAreaIdOrderByIdAsc(request.scenicAreaId())
                .orElseGet(() -> createProfileForArea(request.scenicAreaId()));
        profile.update(
                request.name().trim(),
                request.avatarUrl(),
                request.modelUrl(),
                request.voiceCode(),
                request.clothingStyle(),
                request.welcomeText().trim(),
                request.personaPrompt().trim(),
                request.enabled()
        );
        return DigitalHumanProfileResponse.from(profileRepository.save(profile));
    }

    private DigitalHumanProfile createDefaultProfile() {
        ScenicArea area = scenicAreaRepository.findFirstByEnabledTrueOrderByIdAsc()
                .orElseThrow(() -> new IllegalArgumentException("未配置默认景区"));
        DigitalHumanProfile profile = new DigitalHumanProfile(
                area,
                "小栖",
                "你好，我是你的景区 AI 导游小栖。可以问我景点讲解、拍照建议，也可以让我推荐路线。",
                "你是专业、亲切、可信的景区 AI 导游。"
        );
        return profileRepository.save(profile);
    }

    private DigitalHumanProfile createProfileForArea(Long scenicAreaId) {
        ScenicArea area = scenicAreaRepository.findById(scenicAreaId)
                .filter(ScenicArea::isEnabled)
                .orElseThrow(() -> new IllegalArgumentException("景区不存在或已停用"));
        return new DigitalHumanProfile(
                area,
                "小栖",
                "你好，我是你的景区 AI 导游小栖。可以问我景点讲解、拍照建议，也可以让我推荐路线。",
                "你是专业、亲切、可信的景区 AI 导游。"
        );
    }

    public record UpdateDigitalHumanProfileRequest(
            Long scenicAreaId,
            String name,
            String avatarUrl,
            String modelUrl,
            String voiceCode,
            String clothingStyle,
            String welcomeText,
            String personaPrompt,
            boolean enabled
    ) {
        public UpdateDigitalHumanProfileRequest {
            if (scenicAreaId == null) {
                throw new IllegalArgumentException("景区 ID 不能为空");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("数字人名称不能为空");
            }
            if (welcomeText == null || welcomeText.isBlank()) {
                throw new IllegalArgumentException("欢迎语不能为空");
            }
            if (personaPrompt == null || personaPrompt.isBlank()) {
                throw new IllegalArgumentException("人设 Prompt 不能为空");
            }
        }
    }

    public record DigitalHumanProfileResponse(
            Long id,
            Long scenicAreaId,
            String scenicAreaName,
            String name,
            String avatarUrl,
            String modelUrl,
            String voiceCode,
            String clothingStyle,
            String welcomeText,
            String personaPrompt,
            boolean enabled
    ) {
        static DigitalHumanProfileResponse from(DigitalHumanProfile profile) {
            return new DigitalHumanProfileResponse(
                    profile.getId(),
                    profile.getScenicArea().getId(),
                    profile.getScenicArea().getName(),
                    profile.getName(),
                    profile.getAvatarUrl(),
                    profile.getModelUrl(),
                    profile.getVoiceCode(),
                    profile.getClothingStyle(),
                    profile.getWelcomeText(),
                    profile.getPersonaPrompt(),
                    profile.isEnabled()
            );
        }
    }
}
