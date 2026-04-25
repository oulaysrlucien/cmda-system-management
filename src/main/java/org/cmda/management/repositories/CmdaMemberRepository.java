package org.cmda.management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.cmda.management.entities.CmdaMember;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CmdaMemberRepository extends JpaRepository<CmdaMember, Long>, JpaSpecificationExecutor<CmdaMember> {

    // Trouver tous les membres
    List<CmdaMember> findAll();

    // Trouver les premiers 10 membres
    List<CmdaMember> findTop10ByOrderByIdAsc();

    // Rechercher un membre par email
    CmdaMember findByEmail(String email);

    // Méthode pour récupérer les membres d'une fraternité avec pagination
    Page<CmdaMember> findByFraternityId(Long fraternityId, Pageable pageable);



}
