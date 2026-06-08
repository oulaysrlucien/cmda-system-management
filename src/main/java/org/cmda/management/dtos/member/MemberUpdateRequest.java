package org.cmda.management.dtos.member;

import java.time.LocalDate;

public record MemberUpdateRequest(
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
        LocalDate communityEntryDate,
        LocalDate definitiveCommitmentDate,
        String addressLine1,
        String addressLine2,
        String postalCode,
        String city,
        String administrativeArea,
        String countryCode,
        String internalNotes
) {
}
