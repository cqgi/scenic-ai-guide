package com.scenic.ai.scenic;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScenicSpotRepository extends JpaRepository<ScenicSpot, Long> {
    List<ScenicSpot> findByScenicAreaIdAndEnabledTrueOrderBySortOrderAsc(Long scenicAreaId);
}
