package org.cmda.management.dtos;


import java.util.List;

public class ProvinceDTO {


    private Long id;
    private String name;
    private String description;
    private List<RegionDTO> regions;  // Simplifier en utilisant un autre DTO pour éviter la récursion infinie

    // Getters et setters
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

    public List<RegionDTO> getRegions() {
        return regions;
    }

    public void setRegions(List<RegionDTO> regions) {
        this.regions = regions;
    }


}
