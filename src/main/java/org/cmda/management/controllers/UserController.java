package org.cmda.management.controllers;

import org.cmda.management.entities.User;
import org.cmda.management.enums.Role;
import org.cmda.management.services.UserService;
import org.cmda.management.dtos.UserDTO;
import org.cmda.management.dtos.UserCreationDTO;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Méthode pour créer un utilisateur
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserCreationDTO userCreationDTO) {
        try {
            User user = userService.saveUser(userCreationDTO);
            UserDTO userDTO = userService.convertToDTO(user);  // Convertir User en UserDTO avec les détails
            return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Méthode pour obtenir tous les utilisateurs
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        try {
            List<UserDTO> users = userService.getAllUsers();  // Utilisation de UserDTO pour renvoyer des détails
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Méthode pour obtenir un utilisateur par rôle
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable Role role) {
        try {
            List<UserDTO> users = userService.findByRoleDTO(role);  // Utilisation de DTO pour obtenir les utilisateurs par rôle
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Méthode pour obtenir un utilisateur par son ID
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        try {
            UserDTO userDTO = userService.findUserByIdDTO(id);  // Obtenir les détails d'un utilisateur par son ID
            if (userDTO != null) {
                return new ResponseEntity<>(userDTO, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Méthode pour supprimer un utilisateur par ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Méthode pour mettre à jour un utilisateur (si nécessaire)
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserCreationDTO userCreationDTO) {
        try {
            User updatedUser = userService.updateUser(id, userCreationDTO);
            if (updatedUser != null) {
                UserDTO userDTO = userService.convertToDTO(updatedUser);  // Conversion de l'utilisateur mis à jour en DTO
                return new ResponseEntity<>(userDTO, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
