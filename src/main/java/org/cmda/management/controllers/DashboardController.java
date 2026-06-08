package org.cmda.management.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.cmda.management.dtos.dashboard.DashboardSummaryDTO;
import org.cmda.management.services.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "08 - DASHBOARD", description = "Indicateurs MVP par role et par perimetre.")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROVINCIAL', 'REGIONAL', 'BERGER')")
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getCurrentUserDashboard() {
        return ResponseEntity.ok(dashboardService.getCurrentUserDashboard());
    }
}
