package org.cmda.management.controllers;

import org.cmda.management.dtos.ProvinceDTO;
import org.cmda.management.entities.Province;
import org.cmda.management.services.ProvinceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/provinces")
public class ProvincialController {

    private static final Logger logger = LoggerFactory.getLogger(ProvincialController.class);

    @Autowired
    private ProvinceService provinceService;

    // Créer une province
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<Province> createProvince(@RequestBody Province province) {
        Province createdProvince = provinceService.createProvince(province);
        return new ResponseEntity<>(createdProvince, HttpStatus.CREATED);
    }

    // Lire toutes les provinces avec leurs régions, fraternities, et members
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<ProvinceDTO>> getAllProvinces() {
        logger.info("Requête pour obtenir toutes les provinces reçue.");
        List<ProvinceDTO> provinces = provinceService.getAllProvinces();
        logger.info("Renvoi de {} provinces.", provinces.size());
        return new ResponseEntity<>(provinces, HttpStatus.OK);
    }

    // Lire une province par ID avec ses régions, fraternities, et members
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ProvinceDTO> getProvinceById(@PathVariable Long id) {
        ProvinceDTO province = provinceService.getProvinceById(id);
        return new ResponseEntity<>(province, HttpStatus.OK);
    }

    // Mettre à jour une province
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<Province> updateProvince(@PathVariable Long id, @RequestBody Province provinceDetails) {
        Province updatedProvince = provinceService.updateProvince(id, provinceDetails);
        return new ResponseEntity<>(updatedProvince, HttpStatus.OK);
    }

    // Supprimer une province
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteProvince(@PathVariable Long id) {
        provinceService.deleteProvince(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
