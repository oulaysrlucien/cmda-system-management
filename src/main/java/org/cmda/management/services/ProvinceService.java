package org.cmda.management.services;

import org.cmda.management.entities.Province;
import org.cmda.management.dtos.ProvinceDTO;
import org.cmda.management.dtos.RegionDTO;
import org.cmda.management.dtos.FraternityDTO;
import org.cmda.management.dtos.CmdaMemberDTO;
import org.cmda.management.entities.Region;
import org.cmda.management.entities.Fraternity;
import org.cmda.management.repositories.ProvinceRepository;
import org.cmda.management.services.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProvinceService {

    @Autowired
    private ProvinceRepository provinceRepository;

    @Autowired
    private RegionService regionService;

    // Créer une province
    public Province createProvince(Province province) {
        return provinceRepository.save(province);
    }

    // Convertir une entité Province en DTO Province avec les régions et fraternities
    private ProvinceDTO convertToProvinceDTO(Province province) {
        ProvinceDTO provinceDTO = new ProvinceDTO();
        provinceDTO.setId(province.getId());
        provinceDTO.setName(province.getName());
        provinceDTO.setDescription(province.getDescription());

        // Convertir les régions associées en DTOs et inclure les fraternities et members
        List<RegionDTO> regionDTOs = province.getRegions().stream()
                .map(this::convertToRegionDTO)
                .collect(Collectors.toList());

        provinceDTO.setRegions(regionDTOs);
        return provinceDTO;
    }

    // Convertir une entité Region en DTO avec les fraternities et leurs members
    private RegionDTO convertToRegionDTO(Region region) {
        RegionDTO regionDTO = new RegionDTO();
        regionDTO.setId(region.getId());
        regionDTO.setName(region.getName());
        regionDTO.setDescription(region.getDescription());
        regionDTO.setProvinceId(region.getProvince().getId());

        // Convertir les fraternities associées en DTOs et inclure les members
        List<FraternityDTO> fraternityDTOs = region.getFraternities().stream()
                .map(this::convertToFraternityDTO)
                .collect(Collectors.toList());

        regionDTO.setFraternities(fraternityDTOs);
        return regionDTO;
    }

    // Convertir une entité Fraternity en DTO avec ses members
    private FraternityDTO convertToFraternityDTO(Fraternity fraternity) {
        FraternityDTO fraternityDTO = new FraternityDTO();
        fraternityDTO.setId(fraternity.getId());
        fraternityDTO.setName(fraternity.getName());
        fraternityDTO.setDescription(fraternity.getDescription());
        fraternityDTO.setRegionId(fraternity.getRegion().getId());

        // Convertir les members associés en DTOs
        List<CmdaMemberDTO> memberDTOs = fraternity.getCmdaMembers().stream()
                .map(this::convertToMemberDTO)
                .collect(Collectors.toList());

        fraternityDTO.setMembers(memberDTOs);
        return fraternityDTO;
    }

    // Convertir une entité CmdaMember en DTO
    private CmdaMemberDTO convertToMemberDTO(org.cmda.management.entities.CmdaMember member) {
        CmdaMemberDTO memberDTO = new CmdaMemberDTO();
        memberDTO.setId(member.getId());
        memberDTO.setFirstName(member.getFirstName());
        memberDTO.setLastName(member.getLastName());
        memberDTO.setEmail(member.getEmail());
        memberDTO.setPhoneNumber(member.getPhoneNumber());
        memberDTO.setBirthday(member.getBirthday());
        memberDTO.setProfession(member.getProfession());
        memberDTO.setStatus(member.getStatus().name());
        memberDTO.setFraternityId(member.getFraternity().getId());
        return memberDTO;
    }

    // Lire toutes les provinces avec leurs régions, fraternities, et members
    public List<ProvinceDTO> getAllProvinces() {
        List<Province> provinces = provinceRepository.findAll();
        return provinces.stream()
                .map(this::convertToProvinceDTO)
                .collect(Collectors.toList());
    }

    // Lire une province par ID avec ses régions, fraternities, et members
    public ProvinceDTO getProvinceById(Long id) {
        Province province = provinceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Province not found"));
        return convertToProvinceDTO(province);
    }

    // Mettre à jour une province
    public Province updateProvince(Long id, Province provinceDetails) {
        Province province = provinceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Province not found"));
        province.setName(provinceDetails.getName());
        province.setDescription(provinceDetails.getDescription());
        return provinceRepository.save(province);
    }

    // Supprimer une province
    public void deleteProvince(Long id) {
        Province province = provinceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Province not found"));
        provinceRepository.delete(province);
    }
}
