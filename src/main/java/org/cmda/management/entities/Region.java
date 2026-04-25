package org.cmda.management.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "regions")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id", nullable = false)
    @JsonBackReference  // Côté enfant de la relation // Gérer la relation inverse pour éviter la récursivité
    //@JsonIgnoreProperties("regions")  // Ignorer les régions pour éviter la récursion inverse
    //@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "regions"})  // Ignore les proxys et la propriété "regions"
    private Province province;

    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("region")  // Ignorer la relation avec région pour éviter la récursion inverse
    //@JsonManagedReference
    private List<Fraternity> fraternities;

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Province getProvince() {
        return province;
    }

    public void setProvince(Province province) {
        this.province = province;
    }

    public List<Fraternity> getFraternities() {
        return fraternities;
    }

    public void setFraternities(List<Fraternity> fraternities) {
        this.fraternities = fraternities;
    }
}
