package org.cmda.management.repositories;


import org.cmda.management.entities.Fraternity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FraternityRepository extends JpaRepository<Fraternity, Long> {

    // Trouver toutes les fraternités appartenant à une région spécifique
    List<Fraternity> findByRegionId(Long regionId);

    // Trouver une fraternité par son nom
    Optional<Fraternity> findByName(String name);

    // Trouver toutes les fraternités contenant une certaine chaîne de caractères dans leur nom
    List<Fraternity> findByNameContaining(String keyword);

    // Permmet de vérifier qu'une Fraternité appartient à une région donnée // Utile pour le REGIONAL
    Optional<Fraternity> findByIdAndRegionId(Long id, Long regionId);

    // Permmet de vérifier qu'une Fraternité appartient à une province donnée // Utile pour le PROVINCIAL
    Optional<Fraternity> findByIdAndRegionProvinceId(Long id, Long provinceId);

}
