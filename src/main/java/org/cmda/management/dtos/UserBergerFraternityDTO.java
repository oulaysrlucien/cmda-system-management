package org.cmda.management.dtos;

public class UserBergerFraternityDTO extends UserDTO {
    private FraternityDTO fraternity;

    // Getters and Setters
    public FraternityDTO getFraternity() {
        return fraternity;
    }

    public void setFraternity(FraternityDTO fraternity) {
        this.fraternity = fraternity;
    }
}
