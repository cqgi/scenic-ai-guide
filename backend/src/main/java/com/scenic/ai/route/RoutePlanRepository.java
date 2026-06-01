package com.scenic.ai.route;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoutePlanRepository extends JpaRepository<RoutePlan, Long> {
    List<RoutePlan> findByScenicAreaIdAndEnabledTrue(Long scenicAreaId);
}
