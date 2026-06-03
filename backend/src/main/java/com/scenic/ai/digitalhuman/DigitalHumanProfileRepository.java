package com.scenic.ai.digitalhuman;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DigitalHumanProfileRepository extends JpaRepository<DigitalHumanProfile, Long> {

    @EntityGraph(attributePaths = "scenicArea")
    Optional<DigitalHumanProfile> findFirstByEnabledTrueOrderByIdAsc();

    @EntityGraph(attributePaths = "scenicArea")
    Optional<DigitalHumanProfile> findFirstByScenicAreaIdOrderByIdAsc(Long scenicAreaId);
}
