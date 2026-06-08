package org.cmda.management.dtos.member;

import java.time.LocalDate;

public record MemberResponsibilityRequest(
        String title,
        String contextLabel,
        String description,
        LocalDate startDate
) {
}
