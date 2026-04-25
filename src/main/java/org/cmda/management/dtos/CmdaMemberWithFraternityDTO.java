package org.cmda.management.dtos;

public class CmdaMemberWithFraternityDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String birthday;
    private String profession;
    private String status;

    //private FraternityDTO fraternity;  // Inclure la fraternité sous forme de DTO
    private Long fraternityId;        // ID de la fraternité
    private String fraternityName;

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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    /*
    public FraternityDTO getFraternity() {
        return fraternity;
    }

    public void setFraternity(FraternityDTO fraternity) {
        this.fraternity = fraternity;
    }
    */
    public Long getFraternityId() {
        return fraternityId;
    }

    public void setFraternityId(Long fraternityId) {
        this.fraternityId = fraternityId;
    }

    public String getFraternityName() {
        return fraternityName;
    }

    public void setFraternityName(String fraternityName) {
        this.fraternityName = fraternityName;
    }


}
