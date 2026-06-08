package org.cmda.management.dtos.dashboard;

import java.util.Map;

public record DashboardMetricDTO(
        String code,
        String label,
        long value,
        String icon,
        String route,
        Map<String, String> queryParams,
        String hint
) {
}
