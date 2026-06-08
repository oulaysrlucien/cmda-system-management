package org.cmda.management.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "community_groups")
public class CommunityGroup {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 64) private String code;
    @Column(nullable = false, length = 120) private String label;
    @Column(length = 500) private String description;
    @Column(nullable = false) private int displayOrder;
    @Column(nullable = false) private boolean active = true;

    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
