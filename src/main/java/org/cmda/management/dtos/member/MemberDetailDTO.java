package org.cmda.management.dtos.member;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MemberDetailDTO(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        LocalDate birthday,
        LocalDate baptismDate,
        String profession,
        String talentsAndSkills,
        String status,
        Long fraternityId,
        String fraternityName,
        Long regionId,
        String regionName,
        Long provinceId,
        String provinceName,
        ReferenceValueDTO currentJourneyStage,
        LocalDate journeyStageSince,
        ReferenceValueDTO lifeState,
        LocalDate communityEntryDate,
        LocalDate definitiveCommitmentDate,
        String photoReference,
        String city,
        String addressLine1,
        String addressLine2,
        String postalCode,
        String administrativeArea,
        String countryCode,
        String internalNotes,
        ReferenceValueDTO archiveReason,
        String archiveComment,
        LocalDateTime archivedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<MemberJourneyHistoryDTO> journeyHistory,
        List<MemberAffiliationDTO> groupAssignments,
        List<MemberAffiliationDTO> serviceAssignments,
        List<MemberResponsibilityDTO> responsibilities
) {
}
