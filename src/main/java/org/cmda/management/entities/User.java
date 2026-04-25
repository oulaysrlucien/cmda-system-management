package org.cmda.management.entities;

import jakarta.persistence.*;
import org.cmda.management.enums.Role;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;



@Entity
@Table(name = "users")  // Optionnel: spécifie explicitement le nom de la table
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Identifiant unique pour chaque utilisateur

    @NotNull
    @Size(min = 5, max = 20)
    @Column(nullable = false, unique = true)
    private String username; // Nom d'utilisateur unique pour l'authentification

    @NotNull
    @Size(min = 8)
    @Column(nullable = false)
    private String password; // Mot de passe de l'utilisateur (généralement haché)

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // Rôle de l'utilisateur (Provincial, Régional, Berger, Admin)

    @ManyToOne
    @JoinColumn(name = "province_id")
    @JsonIgnoreProperties({"regions", "hibernateLazyInitializer"})  // Ignorer la sérialisation de ces propriétés pour éviter la récursivité
    private Province province; // Province associée à l'utilisateur

    @ManyToOne
    @JoinColumn(name = "region_id")
    @JsonIgnoreProperties({"province", "fraternities", "hibernateLazyInitializer"})  // Ignorer ces propriétés pour éviter la récursivité
    private Region region; // Région associée à l'utilisateur


    @ManyToOne
    @JoinColumn(name = "fraternity_id")
    private Fraternity fraternity; // Fraternité associée à l'utilisateur




    // Getters et setters pour accéder et modifier les propriétés de l'entité

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Province getProvince() {
        return province;
    }

    public void setProvince(Province province) {
        this.province = province;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public Fraternity getFraternity() {
        return fraternity;
    }

    public void setFraternity(Fraternity fraternity) {
        this.fraternity = fraternity;
    }

}
