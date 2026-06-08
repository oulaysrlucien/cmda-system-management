package org.cmda.management.dtos.dashboard;

import java.util.List;

public record DashboardDistributionDTO(
        String code,
        String label,
        List<DashboardDistributionItemDTO> items
) {
}
