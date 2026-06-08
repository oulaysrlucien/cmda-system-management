package org.cmda.management.repositories;

import org.cmda.management.entities.CommunityService;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CommunityServiceRepository extends JpaRepository<CommunityService, Long> {
    List<CommunityService> findByActiveTrueOrderByDisplayOrderAsc();
    Optional<CommunityService> findByIdAndActiveTrue(Long id);
}
