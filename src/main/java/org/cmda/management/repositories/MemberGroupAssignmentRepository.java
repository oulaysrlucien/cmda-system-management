package org.cmda.management.repositories;

import org.cmda.management.entities.MemberGroupAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MemberGroupAssignmentRepository extends JpaRepository<MemberGroupAssignment, Long> {
    List<MemberGroupAssignment> findByMemberIdOrderByStartDateAsc(Long memberId);
    Optional<MemberGroupAssignment> findByIdAndMemberId(Long id, Long memberId);
    boolean existsByMemberIdAndGroupIdAndEndDateIsNull(Long memberId, Long groupId);
}
