package org.cmda.management.dtos.member;

import java.time.LocalDate;

public record MemberJourneyCorrectionRequest(
        LocalDate startDate,
        LocalDate endDate
) {
}
