package org.cmda.management.dtos;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class UserCreationDTO {

    @NotNull(message = "Le nom d'utilisateur ne peut pas être nul")
    private String username;

    @NotNull(message = "Le mot de passe ne peut pas être nul")
    private String password;  // Mot de passe à hacher lors de la création

    @NotNull(message = "Le rôle ne peut pas être nul")
    private String role;  // Rôle (ADMIN, PROVINCIAL, REGIONAL, BERGER)

    private Long provinceId;  // Si l'utilisateur est Provincial
    private Long regionId;    // Si l'utilisateur est Regional
    private Long fraternityId;  // Si l'utilisateur est Berger
    private Long memberId;
    private Boolean enabled;

    // Getters et Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Long provinceId) {
        this.provinceId = provinceId;
    }

    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public Long getFraternityId() {
        return fraternityId;
    }

    public void setFraternityId(Long fraternityId) {
        this.fraternityId = fraternityId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
