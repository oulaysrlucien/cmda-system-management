package org.cmda.management.repositories;

import org.cmda.management.entities.MemberServiceAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MemberServiceAssignmentRepository extends JpaRepository<MemberServiceAssignment, Long> {
    List<MemberServiceAssignment> findByMemberIdOrderByStartDateAsc(Long memberId);
    Optional<MemberServiceAssignment> findByIdAndMemberId(Long id, Long memberId);
    boolean existsByMemberIdAndServiceIdAndEndDateIsNull(Long memberId, Long serviceId);
}
