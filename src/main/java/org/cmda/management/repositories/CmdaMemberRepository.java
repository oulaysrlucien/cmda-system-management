package org.cmda.management.repositories;

import org.cmda.management.entities.CmdaMember;
import org.cmda.management.enums.MemberStatus; // MISE A JOUR : necessaire pour filtrer par statut ACTIVE / INACTIVE / etc.
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional; // MISE A JOUR : necessaire pour verifier le perimetre avant lecture / modification / archivage

public interface CmdaMemberRepository extends JpaRepository<CmdaMember, Long>, JpaSpecificationExecutor<CmdaMember> {


    List<CmdaMember> findAll();


    List<CmdaMember> findTop10ByOrderByIdAsc();


    CmdaMember findByEmail(String email);


    Page<CmdaMember> findByFraternityId(Long fraternityId, Pageable pageable);


    Page<CmdaMember> findByFraternityRegionId(Long regionId, Pageable pageable);


    Page<CmdaMember> findByFraternityRegionProvinceId(Long provinceId, Pageable pageable);


    Page<CmdaMember> findByStatus(MemberStatus status, Pageable pageable);


    Page<CmdaMember> findByFraternityIdAndStatus(Long fraternityId, MemberStatus status, Pageable pageable);


    Page<CmdaMember> findByFraternityRegionIdAndStatus(Long regionId, MemberStatus status, Pageable pageable);


    Page<CmdaMember> findByFraternityRegionProvinceIdAndStatus(Long provinceId, MemberStatus status, Pageable pageable);


    Optional<CmdaMember> findByIdAndFraternityId(Long id, Long fraternityId);


    Optional<CmdaMember> findByIdAndFraternityRegionId(Long id, Long regionId);


    Optional<CmdaMember> findByIdAndFraternityRegionProvinceId(Long id, Long provinceId);


    // MISE A JOUR : liste globale hors membres archives
    Page<CmdaMember> findByStatusNot(MemberStatus status, Pageable pageable);

    // MISE A JOUR : liste par fraternite hors membres archives
    Page<CmdaMember> findByFraternityIdAndStatusNot(Long fraternityId, MemberStatus status, Pageable pageable);

    // MISE A JOUR : liste par region hors membres archives
    Page<CmdaMember> findByFraternityRegionIdAndStatusNot(Long regionId, MemberStatus status, Pageable pageable);

    // MISE A JOUR : liste par province hors membres archives
    Page<CmdaMember> findByFraternityRegionProvinceIdAndStatusNot(Long provinceId, MemberStatus status, Pageable pageable);
}
