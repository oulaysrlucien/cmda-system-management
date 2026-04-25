package org.cmda.management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.cmda.management.entities.Province;
import java.util.List;
import java.util.Optional;

public interface ProvinceRepository extends JpaRepository<Province, Long> {

    // Trouver une province par son nom
    Optional<Province> findByName(String name);

    // Trouver toutes les provinces contenant une certaine chaîne de caractères dans leur nom
    List<Province> findByNameContaining(String keyword);
}
