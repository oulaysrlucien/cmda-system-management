package org.cmda.management.dtos.member;

import java.time.LocalDate;

public record MemberAffiliationDTO(
        Long id,
        ReferenceValueDTO reference,
        LocalDate startDate,
        LocalDate endDate
) {
}
