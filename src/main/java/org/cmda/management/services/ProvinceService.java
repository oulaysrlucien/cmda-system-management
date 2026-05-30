package org.cmda.management.services;

import org.cmda.management.dtos.CmdaMemberDTO;
import org.cmda.management.dtos.FraternityDTO;
import org.cmda.management.dtos.ProvinceDTO;
import org.cmda.management.dtos.RegionDTO;
import org.cmda.management.entities.Fraternity;
import org.cmda.management.entities.Province;
import org.cmda.management.entities.Region;
import org.cmda.management.repositories.ProvinceRepository;
import org.cmda.management.repositories.RegionRepository;
import org.cmda.management.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProvinceService {

    private final ProvinceRepository provinceRepository;
    private final RegionRepository regionRepository;
    private final UserRepository userRepository;

    public ProvinceService(
            ProvinceRepository provinceRepository,
            RegionRepository regionRepository,
            UserRepository userRepository
    ) {
        this.provinceRepository = provinceRepository;
        this.regionRepository = regionRepository;
        this.userRepository = userRepository;
    }

    public Province createProvince(Province province) {
        String name = normalizeName(province.getName(), "Le nom de la province est requis.");
        if (provinceRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Une province portant ce nom existe deja.");
        }
        province.setName(name);
        province.setArchived(false);
        province.setArchivedAt(null);
        return provinceRepository.save(province);
    }

    public List<ProvinceDTO> getAllProvinces() {
        return provinceRepository.findByArchivedFalse().stream()
                .map(this::convertToProvinceDTO)
                .toList();
    }

    public List<ProvinceDTO> getArchivedProvinces() {
        return provinceRepository.findByArchivedTrue().stream()
                .map(this::convertToProvinceDTO)
                .toList();
    }

    public ProvinceDTO getProvinceById(Long id) {
        Province province = getProvince(id);
        if (province.isArchived()) {
            throw new IllegalStateException("Cette province est archivee.");
        }
        return convertToProvinceDTO(province);
    }

    public Province updateProvince(Long id, Province provinceDetails) {
        Province province = getProvince(id);
        if (province.isArchived()) {
            throw new IllegalStateException("Une province archivee doit etre reactivee avant modification.");
        }
        String name = normalizeName(provinceDetails.getName(), "Le nom de la province est requis.");
        if (provinceRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new IllegalArgumentException("Une province portant ce nom existe deja.");
        }
        province.setName(name);
        province.setDescription(provinceDetails.getDescription());
        return provinceRepository.save(province);
    }

    @Transactional
    public ProvinceDTO archiveProvince(Long id) {
        Province province = getProvince(id);
        if (province.isArchived()) {
            throw new IllegalStateException("Cette province est deja archivee.");
        }
        if (regionRepository.existsByProvinceIdAndArchivedFalse(id)) {
            throw new IllegalStateException("Impossible d'archiver cette province tant qu'elle contient des regions actives.");
        }
        province.setArchived(true);
        province.setArchivedAt(LocalDateTime.now());
        var users = userRepository.findByProvinceId(id);
        users.forEach(user -> user.setProvince(null));
        userRepository.saveAll(users);
        return convertToProvinceDTO(provinceRepository.save(province));
    }

    public ProvinceDTO restoreProvince(Long id) {
        Province province = getProvince(id);
        if (!province.isArchived()) {
            throw new IllegalStateException("Cette province est deja active.");
        }
        province.setArchived(false);
        province.setArchivedAt(null);
        return convertToProvinceDTO(provinceRepository.save(province));
    }

    public void deleteProvince(Long id) {
        throw new IllegalStateException("La suppression physique des provinces est interdite. Utilisez l'archivage logique.");
    }

    private Province getProvince(Long id) {
        return provinceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Province not found"));
    }

    private ProvinceDTO convertToProvinceDTO(Province province) {
        ProvinceDTO dto = new ProvinceDTO();
        dto.setId(province.getId());
        dto.setName(province.getName());
        dto.setDescription(province.getDescription());
        dto.setArchived(province.isArchived());
        dto.setRegions(province.getRegions() == null ? List.of() : province.getRegions().stream()
                .filter(region -> !region.isArchived())
                .map(this::convertToRegionDTO)
                .toList());
        return dto;
    }

    private RegionDTO convertToRegionDTO(Region region) {
        RegionDTO dto = new RegionDTO();
        dto.setId(region.getId());
        dto.setName(region.getName());
        dto.setDescription(region.getDescription());
        dto.setArchived(region.isArchived());
        dto.setProvinceId(region.getProvince().getId());
        dto.setFraternities(region.getFraternities() == null ? List.of() : region.getFraternities().stream()
                .filter(fraternity -> !fraternity.isArchived())
                .map(this::convertToFraternityDTO)
                .toList());
        return dto;
    }

    private FraternityDTO convertToFraternityDTO(Fraternity fraternity) {
        FraternityDTO dto = new FraternityDTO();
        dto.setId(fraternity.getId());
        dto.setName(fraternity.getName());
        dto.setDescription(fraternity.getDescription());
        dto.setArchived(fraternity.isArchived());
        dto.setRegionId(fraternity.getRegion().getId());
        dto.setMembers(fraternity.getCmdaMembers() == null ? List.of() : fraternity.getCmdaMembers().stream()
                .map(this::convertToMemberDTO)
                .toList());
        return dto;
    }

    private CmdaMemberDTO convertToMemberDTO(org.cmda.management.entities.CmdaMember member) {
        CmdaMemberDTO dto = new CmdaMemberDTO();
        dto.setId(member.getId());
        dto.setFirstName(member.getFirstName());
        dto.setLastName(member.getLastName());
        dto.setEmail(member.getEmail());
        dto.setPhoneNumber(member.getPhoneNumber());
        dto.setBirthday(member.getBirthday());
        dto.setProfession(member.getProfession());
        dto.setStatus(member.getStatus().name());
        dto.setFraternityId(member.getFraternity().getId());
        return dto;
    }

    private String normalizeName(String name, String errorMessage) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return name.trim().replaceAll("\\s+", " ");
    }
}
