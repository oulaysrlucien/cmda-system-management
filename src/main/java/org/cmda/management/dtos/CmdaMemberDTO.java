package org.cmda.management.dtos;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;

public class CmdaMemberDTO {


    private Long id;
    private String firstName;
    private String lastName;
    private String email;

    private String phoneNumber;  // Champ pour le numéro de téléphone

    // Format explicite pour les dates
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthday;  // Champ pour la date de naissance

    private String profession;  // Champ pour la profession
    private String status;  // Champ pour le statut (ACTIVE, INACTIVE, etc.)

    private Long fraternityId;
    private String fraternityName;  // Ajout du nom de la fraternité






    // Getters et setters
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


    // Getter et setter pour le phoneNumber
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


    // Getter et setter pour la birthday
    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    // Getter et setter pour la profession
    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    // Getter et setter pour le status
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public Long getFraternityId() {
        return fraternityId;
    }

    public void setFraternityId(Long fraternityId) {
        this.fraternityId = fraternityId;
    }


    public String getFraternityName(){
        return fraternityName;
    }

    public void setFraternityName(String fraternityName){
        this.fraternityName = fraternityName;
    }


}
