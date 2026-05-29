package org.cmda.management.controllers;

import org.cmda.management.dtos.CurrentUserScopeDTO;
import org.cmda.management.dtos.FraternityDTO;
import org.cmda.management.dtos.RegionDTO;
import org.cmda.management.services.CurrentUserScopeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class CurrentUserScopeController {

    private final CurrentUserScopeService currentUserScopeService;

    public CurrentUserScopeController(CurrentUserScopeService currentUserScopeService) {
        this.currentUserScopeService = currentUserScopeService;
    }

    @GetMapping("/scope")
    public ResponseEntity<CurrentUserScopeDTO> getCurrentScope() {
        return ResponseEntity.ok(currentUserScopeService.getCurrentScope());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROVINCIAL')")
    @GetMapping("/province/regions")
    public ResponseEntity<java.util.List<RegionDTO>> getCurrentProvinceRegions() {
        return ResponseEntity.ok(currentUserScopeService.getCurrentProvinceRegions());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'REGIONAL')")
    @GetMapping("/region/fraternities")
    public ResponseEntity<java.util.List<FraternityDTO>> getCurrentRegionFraternities() {
        return ResponseEntity.ok(currentUserScopeService.getCurrentRegionFraternities());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROVINCIAL', 'REGIONAL')")
    @GetMapping("/regions/{regionId}/fraternities")
    public ResponseEntity<java.util.List<FraternityDTO>> getScopedRegionFraternities(@PathVariable Long regionId) {
        return ResponseEntity.ok(currentUserScopeService.getScopedRegionFraternities(regionId));
    }
}
