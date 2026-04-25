package org.cmda.management.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import org.cmda.management.enums.MemberStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.cmda.management.configs.WebConfig;

@Entity
@Table(name = "cmda_members")
public class CmdaMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    //@Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @Column(nullable = true)
    private String profession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fraternity_id")
    @JsonBackReference  // Côté enfant de la relation
    //@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "region"})  // Ignore les proxys Hibernate et la région
    private Fraternity fraternity;


    // Getters et Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }

    public Fraternity getFraternity() {
        return fraternity;
    }

    public void setFraternity(Fraternity fraternity) {
        this.fraternity = fraternity;
    }
}
