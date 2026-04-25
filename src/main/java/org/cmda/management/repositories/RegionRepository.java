package org.cmda.management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.cmda.management.entities.Region;
import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {

    // Trouver toutes les régions appartenant à une province spécifique
    List<Region> findByProvinceId(Long provinceId);


    // Trouver une région par son nom
    Optional<Region> findByName(String name);

    // Trouver toutes les régions contenant une certaine chaîne de caractères dans leur nom
    List<Region> findByNameContaining(String keyword);
}
