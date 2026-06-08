package org.cmda.management.repositories;

import org.cmda.management.entities.LifeState;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LifeStateRepository extends JpaRepository<LifeState, Long> {
    List<LifeState> findByActiveTrueOrderByDisplayOrderAsc();
    Optional<LifeState> findByIdAndActiveTrue(Long id);
}
