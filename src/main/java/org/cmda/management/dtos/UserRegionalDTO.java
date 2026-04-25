package org.cmda.management.dtos;

public class UserRegionalDTO extends UserDTO {
    private RegionDTO region;

    // Getters and Setters
    public RegionDTO getRegion() {
        return region;
    }

    public void setRegion(RegionDTO region) {
        this.region = region;
    }
}
