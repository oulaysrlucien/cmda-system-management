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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate baptismDate;

    private String profession;  // Champ pour la profession
    private String status;  // Champ pour le statut (ACTIVE, INACTIVE, etc.)

    private Long fraternityId;
    private String fraternityName;  // Ajout du nom de la fraternité


    // MISE A JOUR : informations de region pour valider le filtrage metier
    private Long regionId;
    private String regionName;

    // MISE A JOUR : informations de province pour valider le filtrage metier
    private Long provinceId;
    private String provinceName;
    private String photoReference;







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

    public LocalDate getBaptismDate() {
        return baptismDate;
    }

    public void setBaptismDate(LocalDate baptismDate) {
        this.baptismDate = baptismDate;
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



    // MISE A JOUR : getters et setters pour la region
    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    // MISE A JOUR : getters et setters pour la province
    public Long getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Long provinceId) {
        this.provinceId = provinceId;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getPhotoReference() {
        return photoReference;
    }

    public void setPhotoReference(String photoReference) {
        this.photoReference = photoReference;
    }



}
