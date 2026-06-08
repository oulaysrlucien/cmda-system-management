package org.cmda.management.services;

import org.cmda.management.dtos.CmdaMemberDTO;
import org.cmda.management.dtos.FraternityDTO;
import org.cmda.management.dtos.ProvinceDTO;
import org.cmda.management.dtos.RegionDTO;
import org.cmda.management.dtos.UserBergerFraternityDTO;
import org.cmda.management.dtos.UserCreationDTO;
import org.cmda.management.dtos.UserDTO;
import org.cmda.management.dtos.UserProvincialDTO;
import org.cmda.management.dtos.UserRegionalDTO;
import org.cmda.management.entities.CmdaMember;
import org.cmda.management.entities.Fraternity;
import org.cmda.management.entities.Province;
import org.cmda.management.entities.Region;
import org.cmda.management.entities.User;
import org.cmda.management.enums.Role;
import org.cmda.management.repositories.CmdaMemberRepository;
import org.cmda.management.repositories.FraternityRepository;
import org.cmda.management.repositories.ProvinceRepository;
import org.cmda.management.repositories.RegionRepository;
import org.cmda.management.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ProvinceRepository provinceRepository;
    private final RegionRepository regionRepository;
    private final FraternityRepository fraternityRepository;
    private final CmdaMemberRepository cmdaMemberRepository;

    public UserService(
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            ProvinceRepository provinceRepository,
            RegionRepository regionRepository,
            FraternityRepository fraternityRepository,
            CmdaMemberRepository cmdaMemberRepository
    ) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.provinceRepository = provinceRepository;
        this.regionRepository = regionRepository;
        this.fraternityRepository = fraternityRepository;
        this.cmdaMemberRepository = cmdaMemberRepository;
    }

    public User saveUser(UserCreationDTO userDTO) {
        logger.info("Creating user with username: {}", userDTO.getUsername());
        validateCredentialsForCreate(userDTO);

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEnabled(userDTO.getEnabled() == null || userDTO.getEnabled());

        Role role = parseRole(userDTO.getRole());
        if (role == Role.ADMIN) {
            throw new IllegalArgumentException("La creation d'un compte ADMIN depuis l'application est interdite pour le MVP.");
        }
        user.setRole(role);
        applyScope(userDTO, user);
        attachMemberIfProvided(userDTO, user, null);

        return userRepository.save(user);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::convertToDTO).toList();
    }

    public UserDTO convertToDTO(User user) {
        return switch (user.getRole()) {
            case PROVINCIAL -> toProvincialDTO(user);
            case REGIONAL -> toRegionalDTO(user);
            case BERGER -> toBergerDTO(user);
            case ADMIN -> toGenericDTO(user);
        };
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<UserDTO> findByRoleDTO(Role role) {
        return userRepository.findByRole(role).stream().map(this::convertToDTO).toList();
    }

    public UserDTO findUserByIdDTO(Long id) {
        return userRepository.findById(id).map(this::convertToDTO).orElse(null);
    }

    public User updateUser(Long id, UserCreationDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (userDTO.getUsername() != null && !userDTO.getUsername().isBlank()) {
            existingUser.setUsername(userDTO.getUsername());
        }
        if (userDTO.getPassword() != null && !userDTO.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        if (userDTO.getEnabled() != null) {
            existingUser.setEnabled(userDTO.getEnabled());
        }

        Role role = parseRole(userDTO.getRole());
        if (role == Role.ADMIN && existingUser.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("La promotion vers ADMIN depuis l'application est interdite pour le MVP.");
        }

        existingUser.setRole(role);
        existingUser.setProvince(null);
        existingUser.setRegion(null);
        existingUser.setFraternity(null);
        applyScope(userDTO, existingUser);
        attachMemberIfProvided(userDTO, existingUser, id);

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public void recordSuccessfulLogin(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    private void validateCredentialsForCreate(UserCreationDTO userDTO) {
        if (userDTO.getUsername() == null || userDTO.getUsername().isBlank()) {
            throw new IllegalArgumentException("Le nom d'utilisateur est requis.");
        }
        if (userDTO.getPassword() == null || userDTO.getPassword().isBlank()) {
            throw new IllegalArgumentException("Le mot de passe est requis.");
        }
    }

    private Role parseRole(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Le role est requis.");
        }
        return Role.valueOf(role.toUpperCase());
    }

    private void applyScope(UserCreationDTO userDTO, User user) {
        switch (user.getRole()) {
            case PROVINCIAL -> handleProvincialUser(userDTO, user);
            case REGIONAL -> handleRegionalUser(userDTO, user);
            case BERGER -> handleBergerUser(userDTO, user);
            case ADMIN -> logger.info("Admin user updated without additional associations.");
        }
    }

    private void handleProvincialUser(UserCreationDTO userDTO, User user) {
        if (userDTO.getProvinceId() == null) {
            throw new IllegalArgumentException("Un utilisateur PROVINCIAL doit etre rattache a une province.");
        }
        Province province = provinceRepository.findById(userDTO.getProvinceId())
                .orElseThrow(() -> new RuntimeException("Province not found"));
        if (province.isArchived()) {
            throw new IllegalArgumentException("Impossible d'affecter un Provincial a une province archivee.");
        }
        user.setProvince(province);
    }

    private void handleRegionalUser(UserCreationDTO userDTO, User user) {
        if (userDTO.getRegionId() == null) {
            throw new IllegalArgumentException("Un utilisateur REGIONAL doit etre rattache a une region.");
        }
        Region region = regionRepository.findById(userDTO.getRegionId())
                .orElseThrow(() -> new RuntimeException("Region not found"));
        if (region.isArchived() || region.getProvince().isArchived()) {
            throw new IllegalArgumentException("Impossible d'affecter un Regional a une region archivee.");
        }
        user.setRegion(region);
        user.setProvince(region.getProvince());
    }

    private void handleBergerUser(UserCreationDTO userDTO, User user) {
        if (userDTO.getFraternityId() == null) {
            throw new IllegalArgumentException("Un utilisateur BERGER doit etre rattache a une fraternite.");
        }
        Fraternity fraternity = fraternityRepository.findById(userDTO.getFraternityId())
                .orElseThrow(() -> new RuntimeException("Fraternity not found"));
        if (fraternity.isArchived() || fraternity.getRegion().isArchived()) {
            throw new IllegalArgumentException("Impossible d'affecter un Berger a une fraternite archivee.");
        }
        user.setFraternity(fraternity);
        user.setRegion(fraternity.getRegion());
        user.setProvince(fraternity.getRegion().getProvince());
    }

    private void attachMemberIfProvided(UserCreationDTO userDTO, User user, Long existingUserId) {
        if (userDTO.getMemberId() == null) {
            user.setMember(null);
            return;
        }

        boolean alreadyLinked = existingUserId == null
                ? userRepository.existsByMember_Id(userDTO.getMemberId())
                : userRepository.existsByMember_IdAndIdNot(userDTO.getMemberId(), existingUserId);
        if (alreadyLinked) {
            throw new IllegalArgumentException("Ce membre dispose deja d'un compte utilisateur.");
        }

        CmdaMember member = cmdaMemberRepository.findById(userDTO.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Membre introuvable."));
        validateMemberScope(user, member);
        user.setMember(member);
    }

    private void validateMemberScope(User user, CmdaMember member) {
        if (member.getFraternity() == null || member.getFraternity().getRegion() == null) {
            throw new IllegalArgumentException("Le membre doit etre rattache a une fraternite et une region.");
        }

        Long memberFraternityId = member.getFraternity().getId();
        Long memberRegionId = member.getFraternity().getRegion().getId();
        Long memberProvinceId = member.getFraternity().getRegion().getProvince() != null
                ? member.getFraternity().getRegion().getProvince().getId()
                : null;

        if (user.getRole() == Role.BERGER && (user.getFraternity() == null || !user.getFraternity().getId().equals(memberFraternityId))) {
            throw new IllegalArgumentException("Un Berger doit etre lie a un membre de sa fraternite.");
        }
        if (user.getRole() == Role.REGIONAL && (user.getRegion() == null || !user.getRegion().getId().equals(memberRegionId))) {
            throw new IllegalArgumentException("Un Regional doit etre lie a un membre de sa region.");
        }
        if (user.getRole() == Role.PROVINCIAL && (user.getProvince() == null || !user.getProvince().getId().equals(memberProvinceId))) {
            throw new IllegalArgumentException("Un Provincial doit etre lie a un membre de sa province.");
        }
    }

    private UserDTO toGenericDTO(User user) {
        UserDTO dto = new UserDTO();
        fillBaseDTO(dto, user);
        return dto;
    }

    private UserDTO toProvincialDTO(User user) {
        if (user.getProvince() == null) {
            return createWarningDTO(user, "Province manquante pour cet utilisateur Provincial.");
        }
        UserProvincialDTO dto = new UserProvincialDTO();
        fillBaseDTO(dto, user);
        dto.setProvince(convertProvinceToDTO(user.getProvince()));
        return dto;
    }

    private UserDTO toRegionalDTO(User user) {
        if (user.getRegion() == null) {
            return createWarningDTO(user, "Region manquante pour cet utilisateur Regional.");
        }
        UserRegionalDTO dto = new UserRegionalDTO();
        fillBaseDTO(dto, user);
        dto.setRegion(convertRegionToDTO(user.getRegion()));
        return dto;
    }

    private UserDTO toBergerDTO(User user) {
        if (user.getFraternity() == null) {
            return createWarningDTO(user, "Fraternite manquante pour cet utilisateur Berger.");
        }
        UserBergerFraternityDTO dto = new UserBergerFraternityDTO();
        fillBaseDTO(dto, user);
        dto.setFraternity(convertFraternityToDTO(user.getFraternity()));
        return dto;
    }

    private UserDTO createWarningDTO(User user, String warningMessage) {
        UserDTO dto = new UserDTO();
        fillBaseDTO(dto, user);
        dto.setWarningMessage(warningMessage);
        return dto;
    }

    private void fillBaseDTO(UserDTO dto, User user) {
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole().name());
        dto.setMemberId(user.getMember() != null ? user.getMember().getId() : null);
        dto.setDisplayName(resolveDisplayName(user));
        dto.setEnabled(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setLastLoginAt(user.getLastLoginAt());
    }

    private String resolveDisplayName(User user) {
        if (user.getMember() != null) {
            String firstName = user.getMember().getFirstName() != null ? user.getMember().getFirstName().trim() : "";
            String lastName = user.getMember().getLastName() != null ? user.getMember().getLastName().trim() : "";
            String fullName = (firstName + " " + lastName).trim();
            if (!fullName.isBlank()) {
                return fullName;
            }
        }
        return user.getUsername();
    }

    private ProvinceDTO convertProvinceToDTO(Province province) {
        ProvinceDTO dto = new ProvinceDTO();
        dto.setId(province.getId());
        dto.setName(province.getName());
        dto.setDescription(province.getDescription());
        return dto;
    }

    private RegionDTO convertRegionToDTO(Region region) {
        RegionDTO dto = new RegionDTO();
        dto.setId(region.getId());
        dto.setName(region.getName());
        dto.setDescription(region.getDescription());
        if (region.getProvince() != null) {
            dto.setProvinceId(region.getProvince().getId());
        }
        return dto;
    }

    private FraternityDTO convertFraternityToDTO(Fraternity fraternity) {
        FraternityDTO dto = new FraternityDTO();
        dto.setId(fraternity.getId());
        dto.setName(fraternity.getName());
        dto.setDescription(fraternity.getDescription());
        if (fraternity.getRegion() != null) {
            dto.setRegionId(fraternity.getRegion().getId());
        }
        return dto;
    }

    private CmdaMemberDTO convertMemberToDTO(CmdaMember member) {
        CmdaMemberDTO dto = new CmdaMemberDTO();
        dto.setId(member.getId());
        dto.setFirstName(member.getFirstName());
        dto.setLastName(member.getLastName());
        dto.setEmail(member.getEmail());
        dto.setPhoneNumber(member.getPhoneNumber());
        dto.setBirthday(null);
        dto.setBaptismDate(member.getBaptismDate());
        dto.setProfession(member.getProfession());
        dto.setStatus(member.getStatus().name());
        dto.setFraternityId(member.getFraternity() != null ? member.getFraternity().getId() : null);
        return dto;
    }
}
