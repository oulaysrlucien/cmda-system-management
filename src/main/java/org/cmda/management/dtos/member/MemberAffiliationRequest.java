package org.cmda.management.dtos.member;

import java.time.LocalDate;

public record MemberAffiliationRequest(
        Long referenceId,
        LocalDate startDate
) {
}
