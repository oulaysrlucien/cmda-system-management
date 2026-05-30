package org.cmda.management.dtos;


import java.util.List;

public class FraternityDTO {

    private Long id;
    private String name;
    private String description;
    private boolean archived;
    private Long regionId;

    // Utiliser CmdaMemberDTO pour lister les membres
    private List<CmdaMemberDTO> members;

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



    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }


    public List<CmdaMemberDTO> getMembers() {
        return members;
    }

    public void setMembers(List<CmdaMemberDTO> members) {
        this.members = members;
    }
}
