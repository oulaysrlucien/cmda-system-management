package org.cmda.management.services;

import org.cmda.management.entities.Region;
import org.cmda.management.entities.Province;
import org.cmda.management.repositories.RegionRepository;
import org.cmda.management.repositories.ProvinceRepository;
import org.cmda.management.dtos.RegionDTO;
import org.cmda.management.dtos.FraternityDTO;
import org.cmda.management.dtos.CmdaMemberDTO;
import org.cmda.management.entities.Fraternity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.List;
import java.util.Optional;

@Service
public class RegionService {

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private ProvinceRepository provinceRepository;

    // Créer une région
    public RegionDTO createRegion(RegionDTO regionDTO) {
        Region region = new Region();
        region.setName(regionDTO.getName());
        region.setDescription(regionDTO.getDescription());

        Province province = provinceRepository.findById(regionDTO.getProvinceId())
                .orElseThrow(() -> new RuntimeException("Province not found"));
        region.setProvince(province);

        Region savedRegion = regionRepository.save(region);

        RegionDTO createdRegionDTO = new RegionDTO();
        createdRegionDTO.setId(savedRegion.getId());
        createdRegionDTO.setName(savedRegion.getName());
        createdRegionDTO.setDescription(savedRegion.getDescription());
        createdRegionDTO.setProvinceId(savedRegion.getProvince().getId());

        return createdRegionDTO;
    }

    // Convertir une région en RegionDTO en incluant les fraternities et leurs members
    public RegionDTO convertToRegionDTO(Region region) {
        RegionDTO regionDTO = new RegionDTO();
        regionDTO.setId(region.getId());
        regionDTO.setName(region.getName());
        regionDTO.setDescription(region.getDescription());
        regionDTO.setProvinceId(region.getProvince().getId());

        // Convertir les fraternities associées avec leurs membres
        List<FraternityDTO> fraternityDTOs = region.getFraternities()
                .stream()
                .map(fraternity -> {
                    FraternityDTO fraternityDTO = new FraternityDTO();
                    fraternityDTO.setId(fraternity.getId());
                    fraternityDTO.setName(fraternity.getName());
                    fraternityDTO.setDescription(fraternity.getDescription());
                    fraternityDTO.setRegionId(fraternity.getRegion().getId());

                    // Convertir les members associés
                    List<CmdaMemberDTO> memberDTOs = fraternity.getCmdaMembers() != null ?
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

                    fraternityDTO.setMembers(memberDTOs);
                    return fraternityDTO;
                })
                .collect(Collectors.toList());

        regionDTO.setFraternities(fraternityDTOs);
        return regionDTO;
    }

    // Récupérer toutes les régions avec leurs fraternities et members sous forme de DTO
    public List<RegionDTO> getAllRegions() {
        List<Region> regions = regionRepository.findAll();
        return regions.stream().map(this::convertToRegionDTO).collect(Collectors.toList());
    }

    // Lire une région par ID
    public Optional<Region> getRegionById(Long id) {
        return regionRepository.findById(id);
    }

    // Mettre à jour une région
    /*
    public Region updateRegion(Long id, Region regionDetails) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Region not found"));
        region.setName(regionDetails.getName());
        region.setDescription(regionDetails.getDescription());

        Province province = provinceRepository.findById(regionDetails.getProvince().getId())
                .orElseThrow(() -> new RuntimeException("Province not found"));
        region.setProvince(province);

        return regionRepository.save(region);
    }
    */
    public RegionDTO updateRegion(Long id, RegionDTO regionDTO) {
        // Récupérer la région par son ID
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Region not found"));

        // Vérifier et mettre à jour la province associée
        Province province = provinceRepository.findById(regionDTO.getProvinceId())
                .orElseThrow(() -> new RuntimeException("Province not found"));

        // Mettre à jour les propriétés de la région
        region.setName(regionDTO.getName());
        region.setDescription(regionDTO.getDescription());
        region.setProvince(province);  // Mettre à jour la province associée

        // Sauvegarder la région mise à jour
        Region updatedRegion = regionRepository.save(region);

        // Convertir et renvoyer la région mise à jour sous forme de DTO complet avec fraternities et members
        return convertToRegionDTO(updatedRegion);
    }






    // Supprimer une région
    public void deleteRegion(Long id) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Region not found"));
        regionRepository.delete(region);
    }

    // Lire les régions par province
    public List<Region> getRegionsByProvince(Long provinceId) {
        return regionRepository.findByProvinceId(provinceId);
    }



    // Méthode pour convertir une entité Region en DTO
    public RegionDTO convertToDTO(Region region) {
        RegionDTO dto = new RegionDTO();
        dto.setId(region.getId());
        dto.setName(region.getName());
        dto.setDescription(region.getDescription());
        dto.setProvinceId(region.getProvince().getId());
        return dto;
    }



}
