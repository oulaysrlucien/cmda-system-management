package org.cmda.management.repositories;

import org.cmda.management.entities.MemberJourneyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MemberJourneyHistoryRepository extends JpaRepository<MemberJourneyHistory, Long> {
    List<MemberJourneyHistory> findByMemberIdOrderByStartDateAsc(Long memberId);
    Optional<MemberJourneyHistory> findByMemberIdAndEndDateIsNull(Long memberId);
    Optional<MemberJourneyHistory> findByIdAndMemberId(Long id, Long memberId);
}
