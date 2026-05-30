package org.cmda.management.dtos;


import java.util.List;

public class RegionDTO {

    private Long id;
    private String name;
    private String description;
    private boolean archived;
    private Long provinceId;

    // Liste des fraternities associées (seulement les informations nécessaires)
    private List<FraternityDTO> fraternities;  // Lien avec les fraternités

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

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public Long getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Long provinceId) {
        this.provinceId = provinceId;
    }

    public List<FraternityDTO> getFraternities() {
        return fraternities;
    }

    public void setFraternities(List<FraternityDTO> fraternities) {
        this.fraternities = fraternities;
    }
}
