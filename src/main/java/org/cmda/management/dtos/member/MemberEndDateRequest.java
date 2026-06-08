package org.cmda.management.dtos.member;

import java.time.LocalDate;

public record MemberEndDateRequest(
        LocalDate endDate
) {
}
