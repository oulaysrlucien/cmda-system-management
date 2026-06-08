package org.cmda.management.dtos.dashboard;

import java.util.Map;

public record DashboardAlertDTO(
        String code,
        String label,
        long value,
        String severity,
        String route,
        Map<String, String> queryParams
) {
}
