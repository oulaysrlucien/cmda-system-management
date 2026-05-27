package org.cmda.management.services;

import org.cmda.management.dtos.UserCreationDTO;
import org.cmda.management.entities.User;
import org.cmda.management.enums.Role;
import org.cmda.management.repositories.UserRepository;
import org.cmda.management.repositories.ProvinceRepository;
import org.cmda.management.repositories.RegionRepository;
import org.cmda.management.repositories.FraternityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


import org.cmda.management.entities.Province;
import org.cmda.management.entities.Region;
import org.cmda.management.dtos.UserDTO;
import java.util.stream.Collectors;
import org.cmda.management.entities.Fraternity;
import org.cmda.management.dtos.FraternityDTO;
import org.cmda.management.entities.Region;
import org.cmda.management.dtos.RegionDTO;
import org.cmda.management.dtos.ProvinceDTO;
import org.cmda.management.entities.CmdaMember;
import org.cmda.management.dtos.CmdaMemberDTO;

import org.cmda.management.dtos.UserProvincialDTO;
import org.cmda.management.dtos.UserRegionalDTO;
import org.cmda.management.dtos.UserBergerFraternityDTO;





@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProvinceRepository provinceRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private FraternityRepository fraternityRepository;






    // Créer ou mettre à jour un utilisateur
    public User saveUser(UserCreationDTO userDTO) {
        logger.info("Creating user with username: {}", userDTO.getUsername());

        // Validation simple des entrées
        if (userDTO.getUsername() == null || userDTO.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Le nom d'utilisateur est requis.");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        Role role = Role.valueOf(userDTO.getRole().toUpperCase());
        user.setRole(role);

        // Gestion des entités associées en fonction du rôle
        switch (role) {
            case PROVINCIAL:
                handleProvincialUser(userDTO, user);
                break;
            case REGIONAL:
                handleRegionalUser(userDTO, user);
                break;
            case BERGER:
                handleBergerUser(userDTO, user);
                break;
            case ADMIN:
                logger.info("Admin user created without additional associations.");
                break;
            default:
                throw new IllegalArgumentException("Invalid role specified");
        }

        logger.info("User created successfully with username: {}", userDTO.getUsername());
        return userRepository.save(user);
    }

    // Gestion du Provincial
    private void handleProvincialUser(UserCreationDTO userDTO, User user) {
        if (userDTO.getProvinceId() == null) {
            throw new IllegalArgumentException("Un utilisateur PROVINCIAL doit etre rattache a une province.");
        }

        user.setProvince(provinceRepository.findById(userDTO.getProvinceId())
                .orElseThrow(() -> new RuntimeException("Province not found")));
    }


    // Gestion du Regional
    private void handleRegionalUser(UserCreationDTO userDTO, User user) {
        if (userDTO.getRegionId() == null) {
            throw new IllegalArgumentException("Un utilisateur REGIONAL doit etre rattache a une region.");
        }

        Region region = regionRepository.findById(userDTO.getRegionId())
                .orElseThrow(() -> new RuntimeException("Region not found"));

        user.setRegion(region);
        user.setProvince(region.getProvince());
    }



    // Gestion du Berger
    private void handleBergerUser(UserCreationDTO userDTO, User user) {
        if (userDTO.getFraternityId() == null) {
            throw new IllegalArgumentException("Un utilisateur BERGER doit etre rattache a une fraternite.");
        }

        Fraternity fraternity = fraternityRepository.findById(userDTO.getFraternityId())
                .orElseThrow(() -> new RuntimeException("Fraternity not found"));

        user.setFraternity(fraternity);
    }





    // Méthode pour obtenir tous les utilisateurs (avec les détails province, région, fraternités)
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(this::convertToDTO).collect(Collectors.toList());
    }





    // Convertir User en UserDTO avec les informations pertinentes
    public UserDTO convertToDTO(User user) {
        switch (user.getRole()) {
            case PROVINCIAL:
                if (user.getProvince() == null) {
                    return createWarningDTO(user, "Province manquante pour cet utilisateur Provincial.");
                }
                UserProvincialDTO provincialDTO = new UserProvincialDTO();
                provincialDTO.setId(user.getId());
                provincialDTO.setUsername(user.getUsername());
                provincialDTO.setRole(user.getRole().name());
                provincialDTO.setProvince(convertProvinceToDTO(user.getProvince()));  // Associer la province
                return provincialDTO;

            case REGIONAL:
                if (user.getRegion() == null) {
                    return createWarningDTO(user, "Region manquante pour cet utilisateur Régional.");
                }
                UserRegionalDTO regionalDTO = new UserRegionalDTO();
                regionalDTO.setId(user.getId());
                regionalDTO.setUsername(user.getUsername());
                regionalDTO.setRole(user.getRole().name());
                regionalDTO.setRegion(convertRegionToDTO(user.getRegion()));  // Associer la région
                return regionalDTO;

            case BERGER:
                if (user.getFraternity() == null) {
                    return createWarningDTO(user, "Fraternité manquante pour cet utilisateur Berger.");
                }
                UserBergerFraternityDTO bergerDTO = new UserBergerFraternityDTO();
                bergerDTO.setId(user.getId());
                bergerDTO.setUsername(user.getUsername());
                bergerDTO.setRole(user.getRole().name());
                bergerDTO.setFraternity(convertFraternityToDTO(user.getFraternity()));  // Associer la fraternité
                return bergerDTO;

            default:
                UserDTO genericDTO = new UserDTO();
                genericDTO.setId(user.getId());
                genericDTO.setUsername(user.getUsername());
                genericDTO.setRole(user.getRole().name());
                return genericDTO;
        }
    }


    // Méthode pour créer un UserDTO avec un message d'avertissement
    private UserDTO createWarningDTO(User user, String warningMessage) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole().name());
        dto.setWarningMessage(warningMessage);
        return dto;
    }


    // Méthode pour convertir une entité Province en DTO
    // Méthode pour convertir une entité Province en DTO
    private ProvinceDTO convertProvinceToDTO(Province province) {
        ProvinceDTO dto = new ProvinceDTO();
        dto.setId(province.getId());
        dto.setName(province.getName());
        dto.setDescription(province.getDescription());

        // Inclure les régions associées si elles existent
        List<RegionDTO> regions = province.getRegions().stream()
                .map(this::convertRegionToDTO)
                .collect(Collectors.toList());
        dto.setRegions(regions);

        return dto;
    }


    // Méthode pour convertir une entité Region en DTO
    private RegionDTO convertRegionToDTO(Region region) {
        RegionDTO dto = new RegionDTO();
        dto.setId(region.getId());
        dto.setName(region.getName());
        dto.setDescription(region.getDescription());
        if (region.getProvince() != null) {
            dto.setProvinceId(region.getProvince().getId());
        }
        // Inclure les fraternities associées si elles existent
        List<FraternityDTO> fraternities = region.getFraternities().stream()
                .map(this::convertFraternityToDTO)
                .collect(Collectors.toList());
        dto.setFraternities(fraternities);
        return dto;
    }



    private FraternityDTO convertFraternityToDTO(Fraternity fraternity) {
        FraternityDTO dto = new FraternityDTO();
        dto.setId(fraternity.getId());
        dto.setName(fraternity.getName());
        dto.setDescription(fraternity.getDescription());

        // Ajouter le regionId dans le DTO
        if (fraternity.getRegion() != null) {
            dto.setRegionId(fraternity.getRegion().getId());
        }

        // Récupérer les membres associés à la fraternité et les convertir en CmdaMemberDTO
        List<CmdaMemberDTO> members = fraternity.getCmdaMembers().stream()
                .map(this::convertMemberToDTO)  // Assurez-vous que cette méthode existe
                .collect(Collectors.toList());
        dto.setMembers(members);  // Associer la liste des membres au DTO

        return dto;
    }

    private CmdaMemberDTO convertMemberToDTO(CmdaMember member) {
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






    // Méthode pour trouver un utilisateur par son nom d'utilisateur
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    //
    public List<UserDTO> findByRoleDTO(Role role) {
        List<User> users = userRepository.findByRole(role);
        return users.stream().map(this::convertToDTO).collect(Collectors.toList());
    }



    // Méthode pour obtenir les utilisateurs par rôle
    public UserDTO findUserByIdDTO(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return null;  // Gérer l'absence d'utilisateur
        }
        return convertToDTO(user);
    }





    //
    public User updateUser(Long id, UserCreationDTO userDTO) {
        logger.info("Updating user with ID: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("User with ID {} not found.", id);
                    return new RuntimeException("User not found");
                });

        existingUser.setUsername(userDTO.getUsername());
        existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        Role role = Role.valueOf(userDTO.getRole().toUpperCase());
        existingUser.setRole(role);

// Nettoyer les anciens rattachements avant d'appliquer le nouveau role
        existingUser.setProvince(null);
        existingUser.setRegion(null);
        existingUser.setFraternity(null);

        switch (role) {
            case PROVINCIAL:
                if (userDTO.getProvinceId() == null) {
                    throw new IllegalArgumentException("Un utilisateur PROVINCIAL doit etre rattache a une province.");
                }

                existingUser.setProvince(provinceRepository.findById(userDTO.getProvinceId())
                        .orElseThrow(() -> new RuntimeException("Province not found")));
                break;

            case REGIONAL:
                if (userDTO.getRegionId() == null) {
                    throw new IllegalArgumentException("Un utilisateur REGIONAL doit etre rattache a une region.");
                }

                Region region = regionRepository.findById(userDTO.getRegionId())
                        .orElseThrow(() -> new RuntimeException("Region not found"));

                existingUser.setRegion(region);
                existingUser.setProvince(region.getProvince());
                break;

            case BERGER:
                if (userDTO.getFraternityId() == null) {
                    throw new IllegalArgumentException("Un utilisateur BERGER doit etre rattache a une fraternite.");
                }

                Fraternity fraternity = fraternityRepository.findById(userDTO.getFraternityId())
                        .orElseThrow(() -> new RuntimeException("Fraternity not found"));

                existingUser.setFraternity(fraternity);
                break;

            case ADMIN:
                logger.info("Admin user updated without additional associations.");
                break;

            default:
                throw new IllegalArgumentException("Invalid role specified");
        }

        logger.info("User updated successfully with ID: {}", id);
        return userRepository.save(existingUser);
    }







    //
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }







}
