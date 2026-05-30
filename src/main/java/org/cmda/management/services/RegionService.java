package org.cmda.management.services;

import org.cmda.management.dtos.CmdaMemberDTO;
import org.cmda.management.dtos.FraternityDTO;
import org.cmda.management.dtos.RegionDTO;
import org.cmda.management.entities.Region;
import org.cmda.management.repositories.FraternityRepository;
import org.cmda.management.repositories.ProvinceRepository;
import org.cmda.management.repositories.RegionRepository;
import org.cmda.management.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RegionService {

    private final RegionRepository regionRepository;
    private final ProvinceRepository provinceRepository;
    private final FraternityRepository fraternityRepository;
    private final UserRepository userRepository;

    public RegionService(
            RegionRepository regionRepository,
            ProvinceRepository provinceRepository,
            FraternityRepository fraternityRepository,
            UserRepository userRepository
    ) {
        this.regionRepository = regionRepository;
        this.provinceRepository = provinceRepository;
        this.fraternityRepository = fraternityRepository;
        this.userRepository = userRepository;
    }

    public RegionDTO createRegion(RegionDTO dto) {
        var province = requireActiveProvince(dto.getProvinceId());
        String name = normalizeName(dto.getName());
        if (regionRepository.existsByProvinceIdAndNameIgnoreCase(province.getId(), name)) {
            throw new IllegalArgumentException("Une region portant ce nom existe deja dans cette province.");
        }
        Region region = new Region();
        region.setName(name);
        region.setDescription(dto.getDescription());
        region.setProvince(province);
        region.setArchived(false);
        region.setArchivedAt(null);
        return convertToDTO(regionRepository.save(region));
    }

    public List<RegionDTO> getAllRegions() {
        return regionRepository.findByArchivedFalse().stream().map(this::convertToRegionDTO).toList();
    }

    public List<RegionDTO> getArchivedRegions() {
        return regionRepository.findByArchivedTrue().stream().map(this::convertToRegionDTO).toList();
    }

    public Optional<Region> getRegionById(Long id) {
        return regionRepository.findById(id).filter(region -> !region.isArchived());
    }

    public RegionDTO updateRegion(Long id, RegionDTO dto) {
        Region region = getRegion(id);
        if (region.isArchived()) {
            throw new IllegalStateException("Une region archivee doit etre reactivee avant modification.");
        }
        if (dto.getProvinceId() == null || !dto.getProvinceId().equals(region.getProvince().getId())) {
            throw new IllegalArgumentException("Le deplacement d'une region vers une autre province est interdit pour le MVP.");
        }
        String name = normalizeName(dto.getName());
        if (regionRepository.existsByProvinceIdAndNameIgnoreCaseAndIdNot(region.getProvince().getId(), name, id)) {
            throw new IllegalArgumentException("Une region portant ce nom existe deja dans cette province.");
        }
        region.setName(name);
        region.setDescription(dto.getDescription());
        return convertToRegionDTO(regionRepository.save(region));
    }

    @Transactional
    public RegionDTO archiveRegion(Long id) {
        Region region = getRegion(id);
        if (region.isArchived()) {
            throw new IllegalStateException("Cette region est deja archivee.");
        }
        if (fraternityRepository.existsByRegionIdAndArchivedFalse(id)) {
            throw new IllegalStateException("Impossible d'archiver cette region tant qu'elle contient des fraternites actives.");
        }
        region.setArchived(true);
        region.setArchivedAt(LocalDateTime.now());
        var users = userRepository.findByRegionId(id);
        users.forEach(user -> {
            user.setRegion(null);
            user.setProvince(null);
        });
        userRepository.saveAll(users);
        return convertToRegionDTO(regionRepository.save(region));
    }

    public RegionDTO restoreRegion(Long id) {
        Region region = getRegion(id);
        if (!region.isArchived()) {
            throw new IllegalStateException("Cette region est deja active.");
        }
        if (region.getProvince() == null || region.getProvince().isArchived()) {
            throw new IllegalStateException("La province parente doit etre active avant de reactiver cette region.");
        }
        region.setArchived(false);
        region.setArchivedAt(null);
        return convertToRegionDTO(regionRepository.save(region));
    }

    public void deleteRegion(Long id) {
        throw new IllegalStateException("La suppression physique des regions est interdite. Utilisez l'archivage logique.");
    }

    public List<Region> getRegionsByProvince(Long provinceId) {
        requireActiveProvince(provinceId);
        return regionRepository.findByProvinceIdAndArchivedFalse(provinceId);
    }

    public RegionDTO convertToRegionDTO(Region region) {
        RegionDTO dto = convertToDTO(region);
        dto.setFraternities(region.getFraternities() == null ? List.of() : region.getFraternities().stream()
                .filter(fraternity -> !fraternity.isArchived())
                .map(fraternity -> {
                    FraternityDTO fraternityDTO = new FraternityDTO();
                    fraternityDTO.setId(fraternity.getId());
                    fraternityDTO.setName(fraternity.getName());
                    fraternityDTO.setDescription(fraternity.getDescription());
                    fraternityDTO.setArchived(fraternity.isArchived());
                    fraternityDTO.setRegionId(fraternity.getRegion().getId());
                    fraternityDTO.setMembers(fraternity.getCmdaMembers() == null ? List.of() : fraternity.getCmdaMembers().stream()
                            .map(member -> {
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
                            })
                            .toList());
                    return fraternityDTO;
                })
                .toList());
        return dto;
    }

    public RegionDTO convertToDTO(Region region) {
        RegionDTO dto = new RegionDTO();
        dto.setId(region.getId());
        dto.setName(region.getName());
        dto.setDescription(region.getDescription());
        dto.setArchived(region.isArchived());
        dto.setProvinceId(region.getProvince().getId());
        return dto;
    }

    private Region getRegion(Long id) {
        return regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Region not found"));
    }

    private org.cmda.management.entities.Province requireActiveProvince(Long provinceId) {
        if (provinceId == null) {
            throw new IllegalArgumentException("Une region doit etre rattachee a une province existante.");
        }
        var province = provinceRepository.findById(provinceId)
                .orElseThrow(() -> new RuntimeException("Province not found"));
        if (province.isArchived()) {
            throw new IllegalStateException("Impossible d'utiliser une province archivee.");
        }
        return province;
    }

    private String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de la region est requis.");
        }
        return name.trim().replaceAll("\\s+", " ");
    }
}
