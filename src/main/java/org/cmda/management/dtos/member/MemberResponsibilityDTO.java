package org.cmda.management.dtos.member;

import java.time.LocalDate;

public record MemberResponsibilityDTO(
        Long id,
        String title,
        String contextLabel,
        String description,
        LocalDate startDate,
        LocalDate endDate
) {
}
