package org.cmda.management.dtos.dashboard;

import java.util.List;

public record DashboardSummaryDTO(
        String role,
        String scopeLevel,
        String scopeLabel,
        List<DashboardMetricDTO> metrics,
        List<DashboardAlertDTO> alerts,
        List<DashboardDistributionDTO> distributions,
        List<DashboardRecentItemDTO> recentMembers,
        List<DashboardRecentItemDTO> recentUsers
) {
}
