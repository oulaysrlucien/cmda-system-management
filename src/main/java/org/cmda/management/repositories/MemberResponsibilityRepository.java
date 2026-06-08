package org.cmda.management.repositories;

import org.cmda.management.entities.MemberResponsibility;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MemberResponsibilityRepository extends JpaRepository<MemberResponsibility, Long> {
    List<MemberResponsibility> findByMemberIdOrderByStartDateAsc(Long memberId);
    Optional<MemberResponsibility> findByIdAndMemberId(Long id, Long memberId);
}
