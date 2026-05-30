package org.cmda.management.services;

import org.cmda.management.dtos.CmdaMemberDTO;
import org.cmda.management.dtos.FraternityDTO;
import org.cmda.management.entities.Fraternity;
import org.cmda.management.enums.MemberStatus;
import org.cmda.management.repositories.CmdaMemberRepository;
import org.cmda.management.repositories.FraternityRepository;
import org.cmda.management.repositories.RegionRepository;
import org.cmda.management.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FraternityService {

    private final FraternityRepository fraternityRepository;
    private final RegionRepository regionRepository;
    private final CmdaMemberRepository cmdaMemberRepository;
    private final UserRepository userRepository;

    public FraternityService(
            FraternityRepository fraternityRepository,
            RegionRepository regionRepository,
            CmdaMemberRepository cmdaMemberRepository,
            UserRepository userRepository
    ) {
        this.fraternityRepository = fraternityRepository;
        this.regionRepository = regionRepository;
        this.cmdaMemberRepository = cmdaMemberRepository;
        this.userRepository = userRepository;
    }

    public FraternityDTO createFraternity(FraternityDTO dto) {
        var region = requireActiveRegion(dto.getRegionId());
        String name = normalizeName(dto.getName());
        if (fraternityRepository.existsByRegionIdAndNameIgnoreCase(region.getId(), name)) {
            throw new IllegalArgumentException("Une fraternite portant ce nom existe deja dans cette region.");
        }
        Fraternity fraternity = new Fraternity();
        fraternity.setName(name);
        fraternity.setDescription(dto.getDescription());
        fraternity.setRegion(region);
        fraternity.setArchived(false);
        fraternity.setArchivedAt(null);
        return convertToFraternityDTO(fraternityRepository.save(fraternity));
    }

    public List<FraternityDTO> getAllFraternities() {
        return fraternityRepository.findByArchivedFalse().stream()
                .map(this::convertToFraternityDTO)
                .toList();
    }

    public List<FraternityDTO> getArchivedFraternities() {
        return fraternityRepository.findByArchivedTrue().stream()
                .map(this::convertToFraternityDTO)
                .toList();
    }

    public Optional<FraternityDTO> getFraternityById(Long id) {
        return fraternityRepository.findById(id)
                .filter(fraternity -> !fraternity.isArchived())
                .map(this::convertToFraternityDTO);
    }

    public Optional<FraternityDTO> getFraternityByIdAndRegion(Long id, Long regionId) {
        return fraternityRepository.findByIdAndRegionIdAndArchivedFalse(id, regionId)
                .map(this::convertToFraternityDTO);
    }

    public Optional<FraternityDTO> getFraternityByIdAndProvince(Long id, Long provinceId) {
        return fraternityRepository.findByIdAndRegionProvinceIdAndArchivedFalse(id, provinceId)
                .map(this::convertToFraternityDTO);
    }

    public FraternityDTO updateFraternity(Long id, FraternityDTO dto) {
        Fraternity fraternity = getFraternity(id);
        if (fraternity.isArchived()) {
            throw new IllegalStateException("Une fraternite archivee doit etre reactivee avant modification.");
        }
        if (dto.getRegionId() == null || !dto.getRegionId().equals(fraternity.getRegion().getId())) {
            throw new IllegalArgumentException("Le deplacement d'une fraternite vers une autre region est interdit pour le MVP.");
        }
        String name = normalizeName(dto.getName());
        if (fraternityRepository.existsByRegionIdAndNameIgnoreCaseAndIdNot(fraternity.getRegion().getId(), name, id)) {
            throw new IllegalArgumentException("Une fraternite portant ce nom existe deja dans cette region.");
        }
        fraternity.setName(name);
        fraternity.setDescription(dto.getDescription());
        return convertToFraternityDTO(fraternityRepository.save(fraternity));
    }

    @Transactional
    public FraternityDTO archiveFraternity(Long id) {
        Fraternity fraternity = getFraternity(id);
        if (fraternity.isArchived()) {
            throw new IllegalStateException("Cette fraternite est deja archivee.");
        }
        if (cmdaMemberRepository.existsByFraternityIdAndStatusNot(id, MemberStatus.ARCHIVED)) {
            throw new IllegalStateException("Impossible d'archiver cette fraternite tant qu'elle contient des membres actifs ou inactifs.");
        }
        fraternity.setArchived(true);
        fraternity.setArchivedAt(LocalDateTime.now());
        var users = userRepository.findByFraternityId(id);
        users.forEach(user -> user.setFraternity(null));
        userRepository.saveAll(users);
        return convertToFraternityDTO(fraternityRepository.save(fraternity));
    }

    public FraternityDTO restoreFraternity(Long id) {
        Fraternity fraternity = getFraternity(id);
        if (!fraternity.isArchived()) {
            throw new IllegalStateException("Cette fraternite est deja active.");
        }
        if (fraternity.getRegion() == null || fraternity.getRegion().isArchived()) {
            throw new IllegalStateException("La region parente doit etre active avant de reactiver cette fraternite.");
        }
        fraternity.setArchived(false);
        fraternity.setArchivedAt(null);
        return convertToFraternityDTO(fraternityRepository.save(fraternity));
    }

    public void deleteFraternity(Long id) {
        throw new IllegalStateException("La suppression physique des fraternites est interdite. Utilisez l'archivage logique.");
    }

    public List<FraternityDTO> getFraternitiesByRegion(Long regionId) {
        requireActiveRegion(regionId);
        return fraternityRepository.findByRegionIdAndArchivedFalse(regionId).stream()
                .map(this::convertToFraternityDTO)
                .toList();
    }

    private FraternityDTO convertToFraternityDTO(Fraternity fraternity) {
        FraternityDTO dto = new FraternityDTO();
        dto.setId(fraternity.getId());
        dto.setName(fraternity.getName());
        dto.setDescription(fraternity.getDescription());
        dto.setArchived(fraternity.isArchived());
        dto.setRegionId(fraternity.getRegion().getId());
        dto.setMembers(fraternity.getCmdaMembers() == null ? List.of() : fraternity.getCmdaMembers().stream()
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
        return dto;
    }

    private Fraternity getFraternity(Long id) {
        return fraternityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fraternity not found"));
    }

    private org.cmda.management.entities.Region requireActiveRegion(Long regionId) {
        if (regionId == null) {
            throw new IllegalArgumentException("Une fraternite doit etre rattachee a une region existante.");
        }
        var region = regionRepository.findById(regionId)
                .orElseThrow(() -> new RuntimeException("Region not found"));
        if (region.isArchived()) {
            throw new IllegalStateException("Impossible d'utiliser une region archivee.");
        }
        return region;
    }

    private String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de la fraternite est requis.");
        }
        return name.trim().replaceAll("\\s+", " ");
    }
}
