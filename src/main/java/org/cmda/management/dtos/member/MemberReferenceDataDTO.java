package org.cmda.management.dtos.member;

import java.util.List;

public record MemberReferenceDataDTO(
        List<ReferenceValueDTO> journeyStages,
        List<ReferenceValueDTO> lifeStates,
        List<ReferenceValueDTO> archiveReasons,
        List<ReferenceValueDTO> communityGroups,
        List<ReferenceValueDTO> communityServices
) {
}
