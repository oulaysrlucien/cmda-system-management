package org.cmda.management.services;

import org.cmda.management.entities.CmdaMember;
import org.cmda.management.dtos.CmdaMemberDTO;
import org.cmda.management.repositories.CmdaMemberRepository;
import org.cmda.management.enums.MemberStatus;
import org.cmda.management.entities.Fraternity;
import org.cmda.management.repositories.FraternityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.cmda.management.dtos.CmdaMemberWithFraternityDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.cmda.management.specifications.CmdaMemberSpecification;

import org.springframework.data.jpa.domain.Specification;

import org.cmda.management.entities.User; // MISE A JOUR : permet de manipuler l'utilisateur connecte

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


@Service
public class CmdaMemberService {

    @Autowired
    private CmdaMemberRepository cmdaMemberRepository;

    @Autowired
    private FraternityRepository fraternityRepository;


    // MISE A JOUR : service permettant de recuperer l'utilisateur actuellement connecte
    @Autowired
    private CurrentUserService currentUserService;



    // Créer un membre
      public CmdaMemberDTO saveCmdaMember(CmdaMemberDTO cmdaMemberDTO) {
        User currentUser = currentUserService.getCurrentUser();

        Fraternity targetFraternity = findTargetFraternityInCurrentUserScope(
                cmdaMemberDTO.getFraternityId(),
                currentUser
        );

        CmdaMember cmdaMember = convertToEntity(cmdaMemberDTO);
        cmdaMember.setFraternity(targetFraternity);

        CmdaMember savedMember = cmdaMemberRepository.save(cmdaMember);
        return convertToDTO(savedMember);
    }







    // Lire les membres
    public List<CmdaMemberDTO> getAllMembers() {
        List<CmdaMember> members = cmdaMemberRepository.findAll();
        List<CmdaMemberDTO> memberDTOs = new ArrayList<>();

        for (CmdaMember member : members) {
            memberDTOs.add(convertToDTO(member));
        }

        return memberDTOs;
    }

    /*
     * ADMINISTRATION METIER
     * Retourne tous les membres, tous statuts confondus.
     * Reserve a ADMIN via le controller.
     */
    public List<CmdaMemberDTO> getAllMembersForAdministration() {
        List<CmdaMember> members = cmdaMemberRepository.findAll();

        List<CmdaMemberDTO> memberDTOs = new ArrayList<>();

        for (CmdaMember member : members) {
            memberDTOs.add(convertToDTO(member));
        }

        return memberDTOs;
    }






    // Lire les membres avec leur fraternité
    public List<CmdaMemberWithFraternityDTO> getAllMembersWithFraternity() {
        List<CmdaMember> members = cmdaMemberRepository.findAll();
        List<CmdaMemberWithFraternityDTO> memberDTOs = new ArrayList<>();

        for (CmdaMember member : members) {
            CmdaMemberWithFraternityDTO dto = new CmdaMemberWithFraternityDTO();
            dto.setId(member.getId());
            dto.setFirstName(member.getFirstName());
            dto.setLastName(member.getLastName());
            dto.setEmail(member.getEmail());
            dto.setPhoneNumber(member.getPhoneNumber());
            dto.setBirthday(member.getBirthday().toString());
            dto.setProfession(member.getProfession());
            dto.setStatus(member.getStatus().toString());

            // Inclure uniquement le nom et l'ID de la fraternité
           // dto.setFraternityId(member.getFraternity().getId());
           // dto.setFraternityName(member.getFraternity().getName());
            // Inclure explicitement le nom et l'ID de la fraternité, au lieu d'utiliser la relation Fraternity
            dto.setFraternityId(member.getFraternity() != null ? member.getFraternity().getId() : null);
            dto.setFraternityName(member.getFraternity() != null ? member.getFraternity().getName() : null);

            memberDTOs.add(dto);
        }

        return memberDTOs;
    }


    // Lire les premiers 10 membres
    public List<CmdaMemberDTO> getFirst10Members() {
        List<CmdaMember> members = cmdaMemberRepository.findTop10ByOrderByIdAsc();
        List<CmdaMemberDTO> memberDTOs = new ArrayList<>();

        for (CmdaMember member : members) {
            memberDTOs.add(convertToDTO(member)); // Convertir les entités en DTO
        }

        return memberDTOs;
    }





    // Lire un membre par son ID
    public Optional<CmdaMemberDTO> getMemberById(Long id) {
        Optional<CmdaMember> member = cmdaMemberRepository.findById(id);
        return member.map(this::convertToDTO); // Conversion de l'entité en DTO
    }





    /*
    * Met à jour un membre uniquement s'il appartient au perimetre  de l'utilisateur connectée
    *
    * Vérifie aussi que la Fraternité cible appartient au périmètre      */
    public CmdaMemberDTO updateCmdaMember(Long id, CmdaMemberDTO cmdaMemberDTO) {
        User currentUser = currentUserService.getCurrentUser();

        CmdaMember cmdaMember = findMemberInCurrentUserScope(id, currentUser);

        if (cmdaMember.getStatus() == MemberStatus.ARCHIVED) {
            throw notFound("Member not found");
        }


        cmdaMember.setFirstName(cmdaMemberDTO.getFirstName());
        cmdaMember.setLastName(cmdaMemberDTO.getLastName());
        cmdaMember.setEmail(cmdaMemberDTO.getEmail());
        cmdaMember.setPhoneNumber(cmdaMemberDTO.getPhoneNumber());
        cmdaMember.setBirthday(cmdaMemberDTO.getBirthday());
        cmdaMember.setProfession(cmdaMemberDTO.getProfession());
       // cmdaMember.setStatus(MemberStatus.valueOf(cmdaMemberDTO.getStatus().toUpperCase()));

        if (cmdaMemberDTO.getFraternityId() != null) {
            Fraternity targetFraternity = findTargetFraternityInCurrentUserScope(
                    cmdaMemberDTO.getFraternityId(),
                    currentUser
            );

            cmdaMember.setFraternity(targetFraternity);
        }

        CmdaMember updatedMember = cmdaMemberRepository.save(cmdaMember);
        return convertToDTO(updatedMember);
    }





    /*
     * MISE A JOUR
     * Recupere le membre uniquement s'il appartient au perimetre
     * de l'utilisateur connecte.
     */
    private CmdaMember findMemberInCurrentUserScope(Long memberId, User currentUser) {
        switch (currentUser.getRole()) {
            case ADMIN:
                return cmdaMemberRepository.findById(memberId)
                        .orElseThrow(() -> notFound("Member not found"));

            case PROVINCIAL:
                if (currentUser.getProvince() == null) {
                    throw new IllegalStateException("Utilisateur PROVINCIAL sans province associee.");
                }

                return cmdaMemberRepository
                        .findByIdAndFraternityRegionProvinceId(memberId, currentUser.getProvince().getId())
                        .orElseThrow(() -> notFound("Member not found"));

            case REGIONAL:
                if (currentUser.getRegion() == null) {
                    throw new IllegalStateException("Utilisateur REGIONAL sans region associee.");
                }

                return cmdaMemberRepository
                        .findByIdAndFraternityRegionId(memberId, currentUser.getRegion().getId())
                        .orElseThrow(() -> notFound("Member not found"));

            case BERGER:
                if (currentUser.getFraternity() == null) {
                    throw new IllegalStateException("Utilisateur BERGER sans fraternite associee.");
                }

                return cmdaMemberRepository
                        .findByIdAndFraternityId(memberId, currentUser.getFraternity().getId())
                        .orElseThrow(() -> notFound("Member not found"));

            default:
                throw new IllegalStateException("Role utilisateur non pris en charge: " + currentUser.getRole());
        }
    }




    /*
     * MISE A JOUR
     * Recupere la fraternite cible uniquement si elle appartient
     * au perimetre de l'utilisateur connecte.
     */
    private Fraternity findTargetFraternityInCurrentUserScope(Long fraternityId, User currentUser) {
        switch (currentUser.getRole()) {
            case ADMIN:
                return fraternityRepository.findById(fraternityId)
                        .orElseThrow(() -> notFound("Fraternity not found"));

            case PROVINCIAL:
                if (currentUser.getProvince() == null) {
                    throw new IllegalStateException("Utilisateur PROVINCIAL sans province associee.");
                }

                return fraternityRepository
                        .findByIdAndRegionProvinceId(fraternityId, currentUser.getProvince().getId())
                        .orElseThrow(() -> notFound("Fraternity not found"));

            case REGIONAL:
                if (currentUser.getRegion() == null) {
                    throw new IllegalStateException("Utilisateur REGIONAL sans region associee.");
                }

                return fraternityRepository
                        .findByIdAndRegionId(fraternityId, currentUser.getRegion().getId())
                        .orElseThrow(() -> notFound("Fraternity not found"));

            case BERGER:
                if (currentUser.getFraternity() == null) {
                    throw new IllegalStateException("Utilisateur BERGER sans fraternite associee.");
                }

                if (!currentUser.getFraternity().getId().equals(fraternityId)) {
                    throw notFound("Fraternity not found");
                }

                return fraternityRepository.findById(fraternityId)
                        .orElseThrow(() -> notFound("Fraternity not found"));

            default:
                throw new IllegalStateException("Role utilisateur non pris en charge: " + currentUser.getRole());
        }
    }




    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }



    /*
     * MISE A JOUR
     * Archive logiquement un membre au lieu de le supprimer physiquement.
     * Le membre doit appartenir au perimetre de l'utilisateur connecte.
     */
    public void archiveCmdaMember(Long id) {
        User currentUser = currentUserService.getCurrentUser();

        CmdaMember cmdaMember = findMemberInCurrentUserScope(id, currentUser);
        cmdaMember.setStatus(MemberStatus.ARCHIVED);

        cmdaMemberRepository.save(cmdaMember);
    }





    /*
     * ADMINISTRATION METIER
     * Retourne uniquement les membres archives.
     * Reserve a ADMIN via le controller.
     */
    public List<CmdaMemberDTO> getArchivedMembersForAdministration() {
        List<CmdaMember> members = cmdaMemberRepository.findAll();

        List<CmdaMemberDTO> memberDTOs = new ArrayList<>();

        for (CmdaMember member : members) {
            if (member.getStatus() == MemberStatus.ARCHIVED) {
                memberDTOs.add(convertToDTO(member));
            }
        }

        return memberDTOs;
    }



    /*
     * ADMINISTRATION METIER
     * Retourne uniquement les membres inactifs.
     * Reserve a ADMIN via le controller.
     */
    public List<CmdaMemberDTO> getInactiveMembersForAdministration() {
        List<CmdaMember> members = cmdaMemberRepository.findAll();

        List<CmdaMemberDTO> memberDTOs = new ArrayList<>();

        for (CmdaMember member : members) {
            if (member.getStatus() == MemberStatus.INACTIVE) {
                memberDTOs.add(convertToDTO(member));
            }
        }

        return memberDTOs;
    }




    // Méthodes de conversion entre CmdaMember et CmdaMemberDTO
    private CmdaMember convertToEntity(CmdaMemberDTO cmdaMemberDTO) {
        CmdaMember cmdaMember = new CmdaMember();
        cmdaMember.setFirstName(cmdaMemberDTO.getFirstName());
        cmdaMember.setLastName(cmdaMemberDTO.getLastName());
        cmdaMember.setEmail(cmdaMemberDTO.getEmail());
        cmdaMember.setPhoneNumber(cmdaMemberDTO.getPhoneNumber());
        cmdaMember.setBirthday(cmdaMemberDTO.getBirthday());
        cmdaMember.setProfession(cmdaMemberDTO.getProfession());
        cmdaMember.setStatus(MemberStatus.valueOf(cmdaMemberDTO.getStatus().toUpperCase()));



        return cmdaMember;
    }

    public CmdaMemberDTO convertToDTO(CmdaMember cmdaMember) {
        CmdaMemberDTO dto = new CmdaMemberDTO();
        dto.setId(cmdaMember.getId());
        dto.setFirstName(cmdaMember.getFirstName());
        dto.setLastName(cmdaMember.getLastName());
        dto.setEmail(cmdaMember.getEmail());
        dto.setPhoneNumber(cmdaMember.getPhoneNumber());
        dto.setBirthday(cmdaMember.getBirthday());
        dto.setProfession(cmdaMember.getProfession());
        dto.setStatus(cmdaMember.getStatus().toString());


        // MISE A JOUR : ajout des informations hierarchiques Fraternity -> Region -> Province
        if (cmdaMember.getFraternity() != null) {
            Fraternity fraternity = cmdaMember.getFraternity();

            dto.setFraternityId(fraternity.getId());
            dto.setFraternityName(fraternity.getName());

            if (fraternity.getRegion() != null) {
                dto.setRegionId(fraternity.getRegion().getId());
                dto.setRegionName(fraternity.getRegion().getName());

                if (fraternity.getRegion().getProvince() != null) {
                    dto.setProvinceId(fraternity.getRegion().getProvince().getId());
                    dto.setProvinceName(fraternity.getRegion().getProvince().getName());
                } else {
                    dto.setProvinceId(null);
                    dto.setProvinceName(null);
                }
            } else {
                dto.setRegionId(null);
                dto.setRegionName(null);
                dto.setProvinceId(null);
                dto.setProvinceName(null);
            }
        } else {
            dto.setFraternityId(null);
            dto.setFraternityName(null);
            dto.setRegionId(null);
            dto.setRegionName(null);
            dto.setProvinceId(null);
            dto.setProvinceName(null);
        }






        return dto;

    }



    // Méthode pour récupérer les membres d'une fraternité avec pagination
    // Méthode mise à jour avec DTO et le champ fraternityName
    public Page<CmdaMemberDTO> getMembersByFraternity(Long fraternityId, Pageable pageable) {
        return cmdaMemberRepository.findByFraternityId(fraternityId, pageable)
                .map(member -> {
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
                    dto.setFraternityName(member.getFraternity().getName());  // Assigner le nom de la fraternité
                    return dto;
                });
    }




    // Méthode pour récupérer les membres filtrés avec pagination
    public Page<CmdaMemberDTO> getFilteredMembers(Long fraternityId, String firstName, String lastName, String profession, Pageable pageable) {
        Specification<CmdaMember> spec = CmdaMemberSpecification.withFilters(fraternityId, firstName, lastName, profession);
        return cmdaMemberRepository.findAll(spec, pageable)
                .map(member -> {
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
                    dto.setFraternityName(member.getFraternity().getName());
                    return dto;
                });
    }








    /*
     * MISE A JOUR
     * Export securise des membres selon le perimetre de l'utilisateur connecte.
     *
     * Le frontend peut envoyer des filtres, mais le perimetre metier
     * reste toujours impose par le backend.
     */
    public List<CmdaMemberDTO> getMembersForCurrentUserExport(
            Long fraternityId,
            String firstName,
            String lastName,
            String profession
    ) {
        User currentUser = currentUserService.getCurrentUser();

        Specification<CmdaMember> spec = (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // Exclure les membres archives
            predicates.add(criteriaBuilder.notEqual(root.get("status"), MemberStatus.ARCHIVED));

            // Perimetre metier selon le role
            switch (currentUser.getRole()) {
                case ADMIN:
                    break;

                case PROVINCIAL:
                    if (currentUser.getProvince() == null) {
                        throw new IllegalStateException("Utilisateur PROVINCIAL sans province associee.");
                    }
                    predicates.add(criteriaBuilder.equal(
                            root.get("fraternity").get("region").get("province").get("id"),
                            currentUser.getProvince().getId()
                    ));
                    break;

                case REGIONAL:
                    if (currentUser.getRegion() == null) {
                        throw new IllegalStateException("Utilisateur REGIONAL sans region associee.");
                    }
                    predicates.add(criteriaBuilder.equal(
                            root.get("fraternity").get("region").get("id"),
                            currentUser.getRegion().getId()
                    ));
                    break;

                case BERGER:
                    if (currentUser.getFraternity() == null) {
                        throw new IllegalStateException("Utilisateur BERGER sans fraternite associee.");
                    }
                    predicates.add(criteriaBuilder.equal(
                            root.get("fraternity").get("id"),
                            currentUser.getFraternity().getId()
                    ));
                    break;

                default:
                    throw new IllegalStateException("Role utilisateur non pris en charge: " + currentUser.getRole());
            }

            // Filtres optionnels demandes par le frontend
            if (fraternityId != null) {
                predicates.add(criteriaBuilder.equal(root.get("fraternity").get("id"), fraternityId));
            }

            if (firstName != null && !firstName.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("firstName")),
                        "%" + firstName.toLowerCase() + "%"
                ));
            }

            if (lastName != null && !lastName.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("lastName")),
                        "%" + lastName.toLowerCase() + "%"
                ));
            }

            if (profession != null && !profession.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("profession")),
                        "%" + profession.toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return cmdaMemberRepository.findAll(spec)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }








    //
    public List<CmdaMemberDTO> getFilteredMembers(Long fraternityId, String firstName, String lastName, String profession) {
        Specification<CmdaMember> spec = CmdaMemberSpecification.withFilters(fraternityId, firstName, lastName, profession);
        List<CmdaMember> members = cmdaMemberRepository.findAll(spec);
        List<CmdaMemberDTO> memberDTOs = new ArrayList<>();

        for (CmdaMember member : members) {
            memberDTOs.add(convertToDTO(member));
        }

        return memberDTOs;
    }




    /*
     * MISE A JOUR
     * Retourne les membres autorises pour l'utilisateur connecte.
     *
     * Regles metier :
     * - ADMIN : tous les membres
     * - PROVINCIAL : membres de toutes les fraternites de sa province
     * - REGIONAL : membres de toutes les fraternites de sa region
     * - BERGER : membres de sa fraternite uniquement
     *
     * Important :
     * Le frontend ne fournit pas le perimetre.
     * Le perimetre est deduit cote backend depuis l'utilisateur connecte.
     */
    public Page<CmdaMemberDTO> getMembersForCurrentUser(Pageable pageable) {
        User currentUser = currentUserService.getCurrentUser();

        switch (currentUser.getRole()) {
            case ADMIN:
                return cmdaMemberRepository
                        .findByStatusNot(MemberStatus.ARCHIVED, pageable)
                        .map(this::convertToDTO);

            case PROVINCIAL:
                if (currentUser.getProvince() == null) {
                    throw new IllegalStateException("Utilisateur PROVINCIAL sans province associee.");
                }

                return cmdaMemberRepository
                        .findByFraternityRegionProvinceIdAndStatusNot(
                                currentUser.getProvince().getId(),
                                MemberStatus.ARCHIVED,
                                pageable
                        )
                        .map(this::convertToDTO);

            case REGIONAL:
                if (currentUser.getRegion() == null) {
                    throw new IllegalStateException("Utilisateur REGIONAL sans region associee.");
                }

                return cmdaMemberRepository
                        .findByFraternityRegionIdAndStatusNot(
                                currentUser.getRegion().getId(),
                                MemberStatus.ARCHIVED,
                                pageable
                        )
                        .map(this::convertToDTO);

            case BERGER:
                if (currentUser.getFraternity() == null) {
                    throw new IllegalStateException("Utilisateur BERGER sans fraternite associee.");
                }

                return cmdaMemberRepository
                        .findByFraternityIdAndStatusNot(
                                currentUser.getFraternity().getId(),
                                MemberStatus.ARCHIVED,
                                pageable
                        )
                        .map(this::convertToDTO);

            default:
                throw new IllegalStateException("Role utilisateur non pris en charge: " + currentUser.getRole());
        }
    }




    /*
     * MISE A JOUR
     * Recherche securisee des membres selon le perimetre
     * de l'utilisateur connecte.
     *
     * Les filtres envoyes par le frontend ne peuvent jamais elargir
     * le perimetre metier de l'utilisateur connecte.
     */
    public Page<CmdaMemberDTO> searchMembers(
            Long fraternityId,
            Long regionId,
            Long provinceId,
            String firstName,
            String lastName,
            String profession,
            String status,
            Pageable pageable
    ) {
        User currentUser = currentUserService.getCurrentUser();

        Specification<CmdaMember> spec = (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // Exclure les membres archives par defaut
            predicates.add(criteriaBuilder.notEqual(root.get("status"), MemberStatus.ARCHIVED));

            // Perimetre metier impose par le backend
            switch (currentUser.getRole()) {
                case ADMIN:
                    break;

                case PROVINCIAL:
                    if (currentUser.getProvince() == null) {
                        throw new IllegalStateException("Utilisateur PROVINCIAL sans province associee.");
                    }
                    predicates.add(criteriaBuilder.equal(
                            root.get("fraternity").get("region").get("province").get("id"),
                            currentUser.getProvince().getId()
                    ));
                    break;

                case REGIONAL:
                    if (currentUser.getRegion() == null) {
                        throw new IllegalStateException("Utilisateur REGIONAL sans region associee.");
                    }
                    predicates.add(criteriaBuilder.equal(
                            root.get("fraternity").get("region").get("id"),
                            currentUser.getRegion().getId()
                    ));
                    break;

                case BERGER:
                    if (currentUser.getFraternity() == null) {
                        throw new IllegalStateException("Utilisateur BERGER sans fraternite associee.");
                    }
                    predicates.add(criteriaBuilder.equal(
                            root.get("fraternity").get("id"),
                            currentUser.getFraternity().getId()
                    ));
                    break;

                default:
                    throw new IllegalStateException("Role utilisateur non pris en charge: " + currentUser.getRole());
            }

            // Filtres hierarchiques optionnels
            if (provinceId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("fraternity").get("region").get("province").get("id"),
                        provinceId
                ));
            }

            if (regionId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("fraternity").get("region").get("id"),
                        regionId
                ));
            }

            if (fraternityId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("fraternity").get("id"),
                        fraternityId
                ));
            }

            // Filtres simples optionnels
            if (firstName != null && !firstName.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("firstName")),
                        "%" + firstName.toLowerCase() + "%"
                ));
            }

            if (lastName != null && !lastName.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("lastName")),
                        "%" + lastName.toLowerCase() + "%"
                ));
            }

            if (profession != null && !profession.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("profession")),
                        "%" + profession.toLowerCase() + "%"
                ));
            }

            if (status != null && !status.isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                        root.get("status"),
                        MemberStatus.valueOf(status.toUpperCase())
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return cmdaMemberRepository.findAll(spec, pageable)
                .map(this::convertToDTO);
    }




    /*
     * CRUD METIER
     * Met a jour uniquement le statut d'un membre.
     *
     * Regles :
     * - le membre doit appartenir au perimetre de l'utilisateur connecte
     * - un membre ARCHIVED ne peut pas etre modifie ici
     * - cette route autorise uniquement ACTIVE et INACTIVE
     * - ARCHIVED est gere par archive/restore
     */
    public CmdaMemberDTO updateMemberStatus(Long id, String status) {
        User currentUser = currentUserService.getCurrentUser();

        CmdaMember cmdaMember = findMemberInCurrentUserScope(id, currentUser);

        if (cmdaMember.getStatus() == MemberStatus.ARCHIVED) {
            throw notFound("Member not found");
        }

        MemberStatus newStatus = MemberStatus.valueOf(status.toUpperCase());

        if (newStatus == MemberStatus.ARCHIVED) {
            throw new IllegalArgumentException("Use archive endpoint to archive a member.");
        }

        cmdaMember.setStatus(newStatus);

        CmdaMember savedMember = cmdaMemberRepository.save(cmdaMember);
        return convertToDTO(savedMember);
    }





    /*
     * CRUD METIER
     * Restaure un membre archive.
     *
     * Regles :
     * - reserve a ADMIN via le controller
     * - restaure uniquement les membres ARCHIVED
     * - remet le statut a ACTIVE
     */
    public CmdaMemberDTO restoreMember(Long id) {
        CmdaMember cmdaMember = cmdaMemberRepository.findById(id)
                .orElseThrow(() -> notFound("Member not found"));

        if (cmdaMember.getStatus() != MemberStatus.ARCHIVED) {
            throw notFound("Member not found");
        }

        cmdaMember.setStatus(MemberStatus.ACTIVE);

        CmdaMember savedMember = cmdaMemberRepository.save(cmdaMember);
        return convertToDTO(savedMember);
    }





    /*
     * MISE A JOUR
     * Recupere un membre par ID uniquement s'il appartient
     * au perimetre de l'utilisateur connecte.
     *
     * ADMIN : acces global
     * PROVINCIAL : membre dans sa province
     * REGIONAL : membre dans sa region
     * BERGER : membre dans sa fraternite
     */
    public Optional<CmdaMemberDTO> getMemberByIdForCurrentUser(Long id) {
        User currentUser = currentUserService.getCurrentUser();

        switch (currentUser.getRole()) {
            case ADMIN:
                return cmdaMemberRepository.findById(id)
                        .filter(member -> member.getStatus() != MemberStatus.ARCHIVED)
                        .map(this::convertToDTO);

            case PROVINCIAL:
                if (currentUser.getProvince() == null) {
                    throw new IllegalStateException("Utilisateur PROVINCIAL sans province associee.");
                }

                return cmdaMemberRepository
                        .findByIdAndFraternityRegionProvinceId(id, currentUser.getProvince().getId())
                        .filter(member -> member.getStatus() != MemberStatus.ARCHIVED)
                        .map(this::convertToDTO);

            case REGIONAL:
                if (currentUser.getRegion() == null) {
                    throw new IllegalStateException("Utilisateur REGIONAL sans region associee.");
                }

                return cmdaMemberRepository
                        .findByIdAndFraternityRegionId(id, currentUser.getRegion().getId())
                        .filter(member -> member.getStatus() != MemberStatus.ARCHIVED)
                        .map(this::convertToDTO);

            case BERGER:
                if (currentUser.getFraternity() == null) {
                    throw new IllegalStateException("Utilisateur BERGER sans fraternite associee.");
                }

                return cmdaMemberRepository
                        .findByIdAndFraternityId(id, currentUser.getFraternity().getId())
                        .filter(member -> member.getStatus() != MemberStatus.ARCHIVED)
                        .map(this::convertToDTO);

            default:
                throw new IllegalStateException("Role utilisateur non pris en charge: " + currentUser.getRole());
        }
    }




}
