package com.scenic.ai.scenic;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScenicAreaRepository extends JpaRepository<ScenicArea, Long> {
    Optional<ScenicArea> findFirstByEnabledTrueOrderByIdAsc();
}
