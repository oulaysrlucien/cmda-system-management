package org.cmda.management.repositories;

import org.cmda.management.entities.ArchiveReason;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ArchiveReasonRepository extends JpaRepository<ArchiveReason, Long> {
    List<ArchiveReason> findByActiveTrueOrderByDisplayOrderAsc();
    Optional<ArchiveReason> findByIdAndActiveTrue(Long id);
}
