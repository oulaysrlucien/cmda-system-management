package org.cmda.management.dtos.member;

import java.time.LocalDate;

public record MemberJourneyTransitionRequest(
        Long journeyStageId,
        LocalDate startDate
) {
}
