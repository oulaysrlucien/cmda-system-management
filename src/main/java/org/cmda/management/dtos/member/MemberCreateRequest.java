package org.cmda.management.dtos.member;

import java.time.LocalDate;

public record MemberCreateRequest(
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        LocalDate birthday,
        LocalDate baptismDate,
        String profession,
        String talentsAndSkills,
        Long fraternityId,
        Long lifeStateId,
        LocalDate communityEntryDate
) {
}
