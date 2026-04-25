package org.cmda.management.services;


/*
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

@Service
public class CmdaMemberService {

    @Autowired
    private CmdaMemberRepository cmdaMemberRepository;

    @Autowired
    private FraternityRepository fraternityRepository;

    // Créer un membre
    public CmdaMember saveCmdaMember(CmdaMemberDTO cmdaMemberDTO) {
        CmdaMember cmdaMember = new CmdaMember();
        cmdaMember.setFirstName(cmdaMemberDTO.getFirstName());
        cmdaMember.setLastName(cmdaMemberDTO.getLastName());
        cmdaMember.setEmail(cmdaMemberDTO.getEmail());
        cmdaMember.setPhoneNumber(cmdaMemberDTO.getPhoneNumber());
        cmdaMember.setBirthday(cmdaMemberDTO.getBirthday());
        cmdaMember.setProfession(cmdaMemberDTO.getProfession());

        // Conversion du statut de String à Enum
        cmdaMember.setStatus(MemberStatus.valueOf(cmdaMemberDTO.getStatus().toUpperCase()));

        // Récupérer la Fraternity à partir de l'ID fourni
        Fraternity fraternity = fraternityRepository.findById(cmdaMemberDTO.getFraternityId())
                .orElseThrow(() -> new RuntimeException("Fraternity not found"));
        cmdaMember.setFraternity(fraternity);

        return cmdaMemberRepository.save(cmdaMember);
    }

    // Lire les membres
    public List<CmdaMember> getAllMembers() {
        return cmdaMemberRepository.findAll();
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
            dto.setFraternityId(member.getFraternity().getId());
            dto.setFraternityName(member.getFraternity().getName());

            memberDTOs.add(dto);
        }

        return memberDTOs;
    }


    // Lire les premiers 10 membres
    public List<CmdaMember> getFirst10Members() {
        return cmdaMemberRepository.findTop10ByOrderByIdAsc();
    }

    // Lire un membre par son ID
    public Optional<CmdaMember> getMemberById(Long id) {
        return cmdaMemberRepository.findById(id);
    }

    // Mettre à jour un membre
    public CmdaMember updateCmdaMember(Long id, CmdaMemberDTO cmdaMemberDTO) {
        Optional<CmdaMember> existingMember = cmdaMemberRepository.findById(id);
        if (existingMember.isPresent()) {
            CmdaMember cmdaMember = existingMember.get();
            cmdaMember.setFirstName(cmdaMemberDTO.getFirstName());
            cmdaMember.setLastName(cmdaMemberDTO.getLastName());
            cmdaMember.setEmail(cmdaMemberDTO.getEmail());
            cmdaMember.setPhoneNumber(cmdaMemberDTO.getPhoneNumber());
            cmdaMember.setBirthday(cmdaMemberDTO.getBirthday());
            cmdaMember.setProfession(cmdaMemberDTO.getProfession());

            // Conversion du statut de String à Enum
            cmdaMember.setStatus(MemberStatus.valueOf(cmdaMemberDTO.getStatus().toUpperCase()));

            Fraternity fraternity = fraternityRepository.findById(cmdaMemberDTO.getFraternityId())
                    .orElseThrow(() -> new RuntimeException("Fraternity not found"));
            cmdaMember.setFraternity(fraternity);

            return cmdaMemberRepository.save(cmdaMember);
        } else {
            throw new RuntimeException("Member not found");
        }
    }

    // Supprimer un membre
    public void deleteCmdaMember(Long id) {
        cmdaMemberRepository.deleteById(id);
    }
}
*/





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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;




@Service
public class CmdaMemberService {

    @Autowired
    private CmdaMemberRepository cmdaMemberRepository;

    @Autowired
    private FraternityRepository fraternityRepository;

    // Créer un membre
    public CmdaMemberDTO saveCmdaMember(CmdaMemberDTO cmdaMemberDTO) {
        CmdaMember cmdaMember = convertToEntity(cmdaMemberDTO);
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


    // Mettre à jour un membre
    public CmdaMemberDTO updateCmdaMember(Long id, CmdaMemberDTO cmdaMemberDTO) {
        CmdaMember cmdaMember = cmdaMemberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // Mettre à jour les champs
        cmdaMember.setFirstName(cmdaMemberDTO.getFirstName());
        cmdaMember.setLastName(cmdaMemberDTO.getLastName());
        cmdaMember.setEmail(cmdaMemberDTO.getEmail());
        cmdaMember.setPhoneNumber(cmdaMemberDTO.getPhoneNumber());
        cmdaMember.setBirthday(cmdaMemberDTO.getBirthday());
        cmdaMember.setProfession(cmdaMemberDTO.getProfession());
        cmdaMember.setStatus(MemberStatus.valueOf(cmdaMemberDTO.getStatus().toUpperCase()));

        // Récupérer la Fraternity à partir de l'ID fourni
        Fraternity fraternity = fraternityRepository.findById(cmdaMemberDTO.getFraternityId())
                .orElseThrow(() -> new RuntimeException("Fraternity not found"));
        cmdaMember.setFraternity(fraternity);

        CmdaMember updatedMember = cmdaMemberRepository.save(cmdaMember);
        return convertToDTO(updatedMember); // Conversion de l'entité mise à jour en DTO
    }


    // Supprimer un membre
    public void deleteCmdaMember(Long id) {
        cmdaMemberRepository.deleteById(id);
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

        // Récupérer la Fraternity à partir de l'ID fourni
        Fraternity fraternity = fraternityRepository.findById(cmdaMemberDTO.getFraternityId())
                .orElseThrow(() -> new RuntimeException("Fraternity not found"));
        cmdaMember.setFraternity(fraternity);

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


        //dto.setFraternityId(cmdaMember.getFraternity().getId());
        //dto.setFraternityName(cmdaMember.getFraternity().getName());  // Assigner le nom de la fraternité

        // Assurer que vous ne renvoyez pas la relation complète `fraternity`
        // mais seulement son `Id` et `Name`
        if (cmdaMember.getFraternity() != null) {
            dto.setFraternityId(cmdaMember.getFraternity().getId());
            dto.setFraternityName(cmdaMember.getFraternity().getName());
        } else {
            dto.setFraternityId(null);
            dto.setFraternityName(null);
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






}
