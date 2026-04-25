package org.cmda.management.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;

@Entity
@Table(name = "fraternities")
public class Fraternity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    @JsonBackReference  // Empêche la sérialisation infinie dans la relation inverse avec Region
    //@JsonIgnoreProperties({"fraternities"})  // Ignore la propriété "fraternities" lors de la sérialisation pour éviter la récursion
    //@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Region region;

    @OneToMany(mappedBy = "fraternity", cascade = CascadeType.ALL, orphanRemoval = true)
    //@JsonManagedReference  // Côté parent de la relation
    @JsonIgnoreProperties("cmdaMembers")  // Ignore la liste des membres lors de la sérialisation
    //@JsonIgnoreProperties({"fraternity"})  // Ignore la fraternité dans CmdaMember pour éviter la récursion
    //@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Ignore les proxys Hibernate
    private List<CmdaMember> cmdaMembers = new ArrayList<>();  // Initialisation de la liste


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

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public List<CmdaMember> getCmdaMembers() {
        return cmdaMembers;
    }

    public void setCmdaMembers(List<CmdaMember> cmdaMembers) {
        this.cmdaMembers = cmdaMembers;
    }
}
