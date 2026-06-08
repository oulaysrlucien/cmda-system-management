package org.cmda.management.repositories;

import org.cmda.management.entities.JourneyStage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JourneyStageRepository extends JpaRepository<JourneyStage, Long> {
    List<JourneyStage> findByActiveTrueOrderByDisplayOrderAsc();
    Optional<JourneyStage> findByIdAndActiveTrue(Long id);
}
