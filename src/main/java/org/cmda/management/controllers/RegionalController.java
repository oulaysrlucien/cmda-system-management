package org.cmda.management.controllers;


import org.cmda.management.dtos.RegionDTO;
import org.cmda.management.entities.Region;
import org.cmda.management.services.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// PreAuthorize
import org.springframework.security.access.prepost.PreAuthorize;


import java.util.List;

import java.util.Optional;

@RestController
@RequestMapping("/regions")
public class RegionalController {

    @Autowired
    private RegionService regionService;

    // Créer une région
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<RegionDTO> createRegion(@RequestBody RegionDTO regionDTO) {
        RegionDTO createdRegion = regionService.createRegion(regionDTO);
        return new ResponseEntity<>(createdRegion, HttpStatus.CREATED);
    }


    // Lire toutes les régions
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<RegionDTO>> getAllRegions() {
        List<RegionDTO> regions = regionService.getAllRegions();
        return new ResponseEntity<>(regions, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/archived")
    public ResponseEntity<List<RegionDTO>> getArchivedRegions() {
        return ResponseEntity.ok(regionService.getArchivedRegions());
    }

    // Lire une région par ID
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Region> getRegionById(@PathVariable Long id) {
        Optional<Region> region = regionService.getRegionById(id);
        return region.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Mettre à jour une région

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<RegionDTO> updateRegion(@PathVariable Long id, @RequestBody RegionDTO regionDTO) {
        //Region updatedRegion = regionService.updateRegion(id, regionDTO);
        RegionDTO updatedRegionDTO = regionService.updateRegion(id, regionDTO);
        return new ResponseEntity<>(updatedRegionDTO, HttpStatus.OK);
    }



    // Supprimer une région
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteRegion(@PathVariable Long id) {
        regionService.deleteRegion(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/archive")
    public ResponseEntity<RegionDTO> archiveRegion(@PathVariable Long id) {
        return ResponseEntity.ok(regionService.archiveRegion(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/restore")
    public ResponseEntity<RegionDTO> restoreRegion(@PathVariable Long id) {
        return ResponseEntity.ok(regionService.restoreRegion(id));
    }

    // Lire les régions par province
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/province/{provinceId}")
    public ResponseEntity<List<Region>> getRegionsByProvince(@PathVariable Long provinceId) {
        List<Region> regions = regionService.getRegionsByProvince(provinceId);
        return new ResponseEntity<>(regions, HttpStatus.OK);
    }






}
