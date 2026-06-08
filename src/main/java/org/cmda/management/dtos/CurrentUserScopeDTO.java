package org.cmda.management.dtos;

import java.util.List;

public class CurrentUserScopeDTO {

    private Long userId;
    private String username;
    private Long memberId;
    private String displayName;
    private String role;
    private String scopeLevel;
    private ScopeRefDTO province;
    private ScopeRefDTO region;
    private ScopeRefDTO fraternity;
    private ScopeMetricsDTO metrics;
    private List<String> readableResources;
    private List<String> manageableResources;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getScopeLevel() {
        return scopeLevel;
    }

    public void setScopeLevel(String scopeLevel) {
        this.scopeLevel = scopeLevel;
    }

    public ScopeRefDTO getProvince() {
        return province;
    }

    public void setProvince(ScopeRefDTO province) {
        this.province = province;
    }

    public ScopeRefDTO getRegion() {
        return region;
    }

    public void setRegion(ScopeRefDTO region) {
        this.region = region;
    }

    public ScopeRefDTO getFraternity() {
        return fraternity;
    }

    public void setFraternity(ScopeRefDTO fraternity) {
        this.fraternity = fraternity;
    }

    public ScopeMetricsDTO getMetrics() {
        return metrics;
    }

    public void setMetrics(ScopeMetricsDTO metrics) {
        this.metrics = metrics;
    }

    public List<String> getReadableResources() {
        return readableResources;
    }

    public void setReadableResources(List<String> readableResources) {
        this.readableResources = readableResources;
    }

    public List<String> getManageableResources() {
        return manageableResources;
    }

    public void setManageableResources(List<String> manageableResources) {
        this.manageableResources = manageableResources;
    }

    public static class ScopeRefDTO {
        private Long id;
        private String name;
        private String description;

        public ScopeRefDTO() {
        }

        public ScopeRefDTO(Long id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

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
    }

    public static class ScopeMetricsDTO {
        private long provincesCount;
        private long regionsCount;
        private long fraternitiesCount;
        private long membersCount;

        public long getProvincesCount() {
            return provincesCount;
        }

        public void setProvincesCount(long provincesCount) {
            this.provincesCount = provincesCount;
        }

        public long getRegionsCount() {
            return regionsCount;
        }

        public void setRegionsCount(long regionsCount) {
            this.regionsCount = regionsCount;
        }

        public long getFraternitiesCount() {
            return fraternitiesCount;
        }

        public void setFraternitiesCount(long fraternitiesCount) {
            this.fraternitiesCount = fraternitiesCount;
        }

        public long getMembersCount() {
            return membersCount;
        }

        public void setMembersCount(long membersCount) {
            this.membersCount = membersCount;
        }
    }
}
