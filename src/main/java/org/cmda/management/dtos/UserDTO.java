package org.cmda.management.dtos;

import java.util.List;

public class UserDTO {
    private Long id;
    private String username;
    private String role;  // Rôle de l'utilisateur (ADMIN, PROVINCIAL, REGIONAL, BERGER)
    private String warningMessage;  // Champ pour indiquer les informations manquantes



    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }




    public String getWarningMessage() {
        return warningMessage;
    }

    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }


}
