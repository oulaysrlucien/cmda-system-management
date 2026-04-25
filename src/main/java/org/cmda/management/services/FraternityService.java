package org.cmda.management.services;

import org.cmda.management.entities.Fraternity;
import org.cmda.management.entities.Region;
import org.cmda.management.repositories.FraternityRepository;
import org.cmda.management.repositories.RegionRepository;
import org.cmda.management.dtos.CmdaMemberDTO;
import org.cmda.management.dtos.FraternityDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.List;
import java.util.Optional;

@Service
public class FraternityService {

    @Autowired
    private FraternityRepository fraternityRepository;

    @Autowired
    private RegionRepository regionRepository;

    // Créer une fraternité
    public FraternityDTO createFraternity(FraternityDTO fraternityDTO) {
        Fraternity fraternity = new Fraternity();
        fraternity.setName(fraternityDTO.getName());
        fraternity.setDescription(fraternityDTO.getDescription());

        Region region = regionRepository.findById(fraternityDTO.getRegionId())
                .orElseThrow(() -> new RuntimeException("Region not found"));
        fraternity.setRegion(region);

        Fraternity savedFraternity = fraternityRepository.save(fraternity);

        return convertToFraternityDTO(savedFraternity);
    }

    // Lire toutes les fraternités
    public List<FraternityDTO> getAllFraternities() {
        List<Fraternity> fraternities = fraternityRepository.findAll();
        return fraternities.stream().map(this::convertToFraternityDTO).collect(Collectors.toList());
    }

    // Lire une fraternité par ID
    public Optional<FraternityDTO> getFraternityById(Long id) {
        return fraternityRepository.findById(id).map(this::convertToFraternityDTO);
    }

    // Mettre à jour une fraternité
    public FraternityDTO updateFraternity(Long id, FraternityDTO fraternityDTO) {
        Fraternity fraternity = fraternityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fraternity not found"));

        fraternity.setName(fraternityDTO.getName());
        fraternity.setDescription(fraternityDTO.getDescription());

        Region region = regionRepository.findById(fraternityDTO.getRegionId())
                .orElseThrow(() -> new RuntimeException("Region not found"));
        fraternity.setRegion(region);

        Fraternity updatedFraternity = fraternityRepository.save(fraternity);
        return convertToFraternityDTO(updatedFraternity);
    }

    // Supprimer une fraternité
    public void deleteFraternity(Long id) {
        Fraternity fraternity = fraternityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fraternity not found"));
        fraternityRepository.delete(fraternity);
    }

    // Lire les fraternités par région
    public List<FraternityDTO> getFraternitiesByRegion(Long regionId) {
        return fraternityRepository.findByRegionId(regionId).stream()
                .map(this::convertToFraternityDTO)
                .collect(Collectors.toList());
    }

    // Convertir une entité Fraternity en FraternityDTO
    private FraternityDTO convertToFraternityDTO(Fraternity fraternity) {
        FraternityDTO fraternityDTO = new FraternityDTO();
        fraternityDTO.setId(fraternity.getId());
        fraternityDTO.setName(fraternity.getName());
        fraternityDTO.setDescription(fraternity.getDescription());
        fraternityDTO.setRegionId(fraternity.getRegion().getId());

        // Si la liste des membres est null, on la remplace par une liste vide
        List<CmdaMemberDTO> members = fraternity.getCmdaMembers() != null ?
                fraternity.getCmdaMembers().stream().map(member -> {
                    CmdaMemberDTO memberDTO = new CmdaMemberDTO();
                    memberDTO.setId(member.getId());
                    memberDTO.setFirstName(member.getFirstName());
                    memberDTO.setLastName(member.getLastName());
                    memberDTO.setEmail(member.getEmail());
                    memberDTO.setPhoneNumber(member.getPhoneNumber());
                    memberDTO.setBirthday(member.getBirthday());
                    memberDTO.setProfession(member.getProfession());
                    memberDTO.setStatus(member.getStatus().name());
                    memberDTO.setFraternityId(fraternity.getId());
                    return memberDTO;
                }).collect(Collectors.toList()) : List.of();

        fraternityDTO.setMembers(members);
        return fraternityDTO;
    }
}
