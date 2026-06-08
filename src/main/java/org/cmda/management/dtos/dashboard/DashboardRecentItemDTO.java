package org.cmda.management.dtos.dashboard;

import java.time.LocalDateTime;
import java.util.Map;

public record DashboardRecentItemDTO(
        Long id,
        String type,
        String label,
        String subtitle,
        LocalDateTime date,
        String route,
        Map<String, String> queryParams
) {
}
