package com.scenic.ai.scenic;

import com.scenic.ai.common.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scenic-areas")
public class ScenicAreaController {

    private final ScenicAreaRepository scenicAreaRepository;
    private final ScenicSpotRepository scenicSpotRepository;

    public ScenicAreaController(ScenicAreaRepository scenicAreaRepository, ScenicSpotRepository scenicSpotRepository) {
        this.scenicAreaRepository = scenicAreaRepository;
        this.scenicSpotRepository = scenicSpotRepository;
    }

    @GetMapping("/default")
    public ApiResponse<ScenicAreaResponse> defaultArea() {
        ScenicArea area = scenicAreaRepository.findFirstByEnabledTrueOrderByIdAsc()
                .orElseThrow(() -> new IllegalArgumentException("未配置默认景区"));
        List<ScenicSpotDto> spots = scenicSpotRepository.findByScenicAreaIdAndEnabledTrueOrderBySortOrderAsc(area.getId())
                .stream()
                .map(ScenicSpotDto::from)
                .toList();
        return ApiResponse.ok(ScenicAreaResponse.from(area, spots));
    }

    public record ScenicAreaResponse(
            Long id,
            String name,
            String summary,
            String location,
            String openingHours,
            String contactPhone,
            List<ScenicSpotDto> spots
    ) {
        static ScenicAreaResponse from(ScenicArea area, List<ScenicSpotDto> spots) {
            return new ScenicAreaResponse(
                    area.getId(),
                    area.getName(),
                    area.getSummary(),
                    area.getLocation(),
                    area.getOpeningHours(),
                    area.getContactPhone(),
                    spots
            );
        }
    }

    public record ScenicSpotDto(Long id, String name, String alias, String summary, String tags, Integer recommendedMinutes) {
        static ScenicSpotDto from(ScenicSpot spot) {
            return new ScenicSpotDto(
                    spot.getId(),
                    spot.getName(),
                    spot.getAlias(),
                    spot.getSummary(),
                    spot.getTags(),
                    spot.getRecommendedMinutes()
            );
        }
    }
}
