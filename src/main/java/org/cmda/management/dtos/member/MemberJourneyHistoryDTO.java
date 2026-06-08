package org.cmda.management.dtos.member;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MemberJourneyHistoryDTO(
        Long id,
        ReferenceValueDTO journeyStage,
        LocalDate startDate,
        LocalDate endDate,
        LocalDateTime createdAt,
        Long createdByUserId,
        LocalDateTime updatedAt,
        Long updatedByUserId
) {
}
