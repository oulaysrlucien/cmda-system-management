package org.cmda.management.repositories;

import org.cmda.management.entities.CommunityGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CommunityGroupRepository extends JpaRepository<CommunityGroup, Long> {
    List<CommunityGroup> findByActiveTrueOrderByDisplayOrderAsc();
    Optional<CommunityGroup> findByIdAndActiveTrue(Long id);
}
