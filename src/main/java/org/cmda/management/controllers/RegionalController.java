package org.cmda.management.controllers;


import org.cmda.management.dtos.RegionDTO;
import org.cmda.management.entities.Region;
import org.cmda.management.services.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



import java.util.List;

import java.util.Optional;

@RestController
@RequestMapping("/regions")
public class RegionalController {

    @Autowired
    private RegionService regionService;

    // Créer une région
    @PostMapping("/create")
    public ResponseEntity<RegionDTO> createRegion(@RequestBody RegionDTO regionDTO) {
        RegionDTO createdRegion = regionService.createRegion(regionDTO);
        return new ResponseEntity<>(createdRegion, HttpStatus.CREATED);
    }


    // Lire toutes les régions
    @GetMapping("/all")
    public ResponseEntity<List<RegionDTO>> getAllRegions() {
        List<RegionDTO> regions = regionService.getAllRegions();
        return new ResponseEntity<>(regions, HttpStatus.OK);
    }

    // Lire une région par ID
    @GetMapping("/{id}")
    public ResponseEntity<Region> getRegionById(@PathVariable Long id) {
        Optional<Region> region = regionService.getRegionById(id);
        return region.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Mettre à jour une région
    /*
    @PutMapping("/update/{id}")
    public ResponseEntity<Region> updateRegion(@PathVariable Long id, @RequestBody Region regionDetails) {
        Region updatedRegion = regionService.updateRegion(id, regionDetails);
        return new ResponseEntity<>(updatedRegion, HttpStatus.OK);
    }
    */
    @PutMapping("/update/{id}")
    public ResponseEntity<RegionDTO> updateRegion(@PathVariable Long id, @RequestBody RegionDTO regionDTO) {
        //Region updatedRegion = regionService.updateRegion(id, regionDTO);
        RegionDTO updatedRegionDTO = regionService.updateRegion(id, regionDTO);
        return new ResponseEntity<>(updatedRegionDTO, HttpStatus.OK);
    }



    // Supprimer une région
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteRegion(@PathVariable Long id) {
        regionService.deleteRegion(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Lire les régions par province
    @GetMapping("/province/{provinceId}")
    public ResponseEntity<List<Region>> getRegionsByProvince(@PathVariable Long provinceId) {
        List<Region> regions = regionService.getRegionsByProvince(provinceId);
        return new ResponseEntity<>(regions, HttpStatus.OK);
    }






}