package org.cmda.management.controllers;


import org.cmda.management.dtos.FraternityDTO;
import org.cmda.management.entities.Fraternity;
import org.cmda.management.services.FraternityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/fraternities")
public class BergerFraternityController {

    @Autowired
    private FraternityService fraternityService;

    // Créer une fraternité
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<FraternityDTO> createFraternity(@RequestBody FraternityDTO fraternityDTO) {
        FraternityDTO createdFraternity = fraternityService.createFraternity(fraternityDTO);
        return new ResponseEntity<>(createdFraternity, HttpStatus.CREATED);
    }

    // Lire toutes les fraternités
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<FraternityDTO>> getAllFraternities() {
        List<FraternityDTO> fraternities = fraternityService.getAllFraternities();
        return new ResponseEntity<>(fraternities, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/archived")
    public ResponseEntity<List<FraternityDTO>> getArchivedFraternities() {
        return ResponseEntity.ok(fraternityService.getArchivedFraternities());
    }

    // Lire une fraternité par ID
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<FraternityDTO> getFraternityById(@PathVariable Long id) {
        Optional<FraternityDTO> fraternityDTO = fraternityService.getFraternityById(id);
        return fraternityDTO.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Mettre à jour une fraternité
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<FraternityDTO> updateFraternity(@PathVariable Long id, @RequestBody FraternityDTO fraternityDTO) {
        FraternityDTO updatedFraternity = fraternityService.updateFraternity(id, fraternityDTO);
        return new ResponseEntity<>(updatedFraternity, HttpStatus.OK);
    }

    // Supprimer une fraternité
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteFraternity(@PathVariable Long id) {
        fraternityService.deleteFraternity(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/archive")
    public ResponseEntity<FraternityDTO> archiveFraternity(@PathVariable Long id) {
        return ResponseEntity.ok(fraternityService.archiveFraternity(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/restore")
    public ResponseEntity<FraternityDTO> restoreFraternity(@PathVariable Long id) {
        return ResponseEntity.ok(fraternityService.restoreFraternity(id));
    }

    // Lire les fraternités par région
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/region/{regionId}")
    public ResponseEntity<List<FraternityDTO>> getFraternitiesByRegion(@PathVariable Long regionId) {
        List<FraternityDTO> fraternities = fraternityService.getFraternitiesByRegion(regionId);
        return new ResponseEntity<>(fraternities, HttpStatus.OK);
    }
}
