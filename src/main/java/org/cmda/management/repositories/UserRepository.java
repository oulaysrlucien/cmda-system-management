package org.cmda.management.repositories;


import org.cmda.management.entities.User;
import org.cmda.management.enums.Role;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>{

    // Trouver un utilisateur par son nom d'utilisateur
    User findByUsername(String username);

    // Trouver tous les utilisateurs ayant un rôle spécifique
    List<User> findByRole(Role role);

    // Trouver tous les utilisateurs dont le nom d'utilisateur contient une certaine chaîne de caractères
    List<User> findByUsernameContaining(String keyword);

    List<User> findByProvinceId(Long provinceId);

    List<User> findByRegionId(Long regionId);

    List<User> findByFraternityId(Long fraternityId);


    // Trouver tous les utilisateurs actifs ayant un rôle spécifique


}
