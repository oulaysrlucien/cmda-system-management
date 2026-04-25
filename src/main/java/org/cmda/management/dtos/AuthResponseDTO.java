package org.cmda.management.dtos;

public class AuthResponseDTO {

    private String token;

    public AuthResponseDTO(String token) {
        this.token = token;
    }


    // getter pour le token
    public String getToken() {
        return token;
    }



}
