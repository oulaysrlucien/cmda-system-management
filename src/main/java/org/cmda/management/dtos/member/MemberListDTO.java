package org.cmda.management.dtos.member;

public record MemberListDTO(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String profession,
        String talentsAndSkills,
        String status,
        Long fraternityId,
        String fraternityName,
        Long regionId,
        String regionName,
        Long provinceId,
        String provinceName,
        String city,
        ReferenceValueDTO currentJourneyStage,
        ReferenceValueDTO lifeState
) {
}
