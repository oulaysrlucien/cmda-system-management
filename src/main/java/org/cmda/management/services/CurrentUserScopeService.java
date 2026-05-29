package org.cmda.management.services;

import org.cmda.management.dtos.CurrentUserScopeDTO;
import org.cmda.management.entities.Fraternity;
import org.cmda.management.entities.Province;
import org.cmda.management.entities.Region;
import org.cmda.management.entities.User;
import org.cmda.management.enums.Role;
import org.cmda.management.repositories.CmdaMemberRepository;
import org.cmda.management.repositories.FraternityRepository;
import org.cmda.management.repositories.ProvinceRepository;
import org.cmda.management.repositories.RegionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CurrentUserScopeService {

    private final CurrentUserService currentUserService;
    private final ProvinceRepository provinceRepository;
    private final RegionRepository regionRepository;
    private final FraternityRepository fraternityRepository;
    private final CmdaMemberRepository cmdaMemberRepository;

    public CurrentUserScopeService(
            CurrentUserService currentUserService,
            ProvinceRepository provinceRepository,
            RegionRepository regionRepository,
            FraternityRepository fraternityRepository,
            CmdaMemberRepository cmdaMemberRepository
    ) {
        this.currentUserService = currentUserService;
        this.provinceRepository = provinceRepository;
        this.regionRepository = regionRepository;
        this.fraternityRepository = fraternityRepository;
        this.cmdaMemberRepository = cmdaMemberRepository;
    }

    @Transactional(readOnly = true)
    public CurrentUserScopeDTO getCurrentScope() {
        User user = currentUserService.getCurrentUser();
        CurrentUserScopeDTO scope = new CurrentUserScopeDTO();

        scope.setUserId(user.getId());
        scope.setUsername(user.getUsername());
        scope.setRole(user.getRole().name());
        scope.setProvince(toRef(user.getProvince()));
        scope.setRegion(toRef(user.getRegion()));
        scope.setFraternity(toRef(user.getFraternity()));
        scope.setScopeLevel(resolveScopeLevel(user.getRole()));
        scope.setReadableResources(resolveReadableResources(user.getRole()));
        scope.setManageableResources(resolveManageableResources(user.getRole()));
        scope.setMetrics(resolveMetrics(user));

        return scope;
    }

    private String resolveScopeLevel(Role role) {
        return switch (role) {
            case ADMIN -> "GLOBAL";
            case PROVINCIAL -> "PROVINCE";
            case REGIONAL -> "REGION";
            case BERGER -> "FRATERNITY";
        };
    }

    private List<String> resolveReadableResources(Role role) {
        return switch (role) {
            case ADMIN -> List.of("PROVINCES", "REGIONS", "FRATERNITIES", "MEMBERS", "USERS");
            case PROVINCIAL -> List.of("OWN_PROVINCE", "OWN_REGIONS", "OWN_FRATERNITIES", "OWN_MEMBERS");
            case REGIONAL -> List.of("OWN_REGION", "OWN_FRATERNITIES", "OWN_MEMBERS");
            case BERGER -> List.of("OWN_FRATERNITY", "OWN_MEMBERS");
        };
    }

    private List<String> resolveManageableResources(Role role) {
        return switch (role) {
            case ADMIN -> List.of("USERS", "PROVINCES", "REGIONS", "FRATERNITIES", "MEMBERS");
            case PROVINCIAL -> List.of("OWN_REGIONS", "OWN_FRATERNITIES", "OWN_MEMBERS");
            case REGIONAL -> List.of("OWN_FRATERNITIES", "OWN_MEMBERS");
            case BERGER -> List.of("OWN_MEMBERS");
        };
    }

    private CurrentUserScopeDTO.ScopeMetricsDTO resolveMetrics(User user) {
        CurrentUserScopeDTO.ScopeMetricsDTO metrics = new CurrentUserScopeDTO.ScopeMetricsDTO();

        switch (user.getRole()) {
            case ADMIN -> {
                metrics.setProvincesCount(provinceRepository.count());
                metrics.setRegionsCount(regionRepository.count());
                metrics.setFraternitiesCount(fraternityRepository.count());
                metrics.setMembersCount(cmdaMemberRepository.count());
            }
            case PROVINCIAL -> {
                Long provinceId = requireProvince(user).getId();
                metrics.setProvincesCount(1);
                metrics.setRegionsCount(regionRepository.countByProvinceId(provinceId));
                metrics.setFraternitiesCount(fraternityRepository.countByRegionProvinceId(provinceId));
                metrics.setMembersCount(cmdaMemberRepository.countByFraternityRegionProvinceId(provinceId));
            }
            case REGIONAL -> {
                Long regionId = requireRegion(user).getId();
                metrics.setProvincesCount(1);
                metrics.setRegionsCount(1);
                metrics.setFraternitiesCount(fraternityRepository.countByRegionId(regionId));
                metrics.setMembersCount(cmdaMemberRepository.countByFraternityRegionId(regionId));
            }
            case BERGER -> {
                Long fraternityId = requireFraternity(user).getId();
                metrics.setProvincesCount(user.getProvince() != null ? 1 : 0);
                metrics.setRegionsCount(user.getRegion() != null ? 1 : 0);
                metrics.setFraternitiesCount(1);
                metrics.setMembersCount(cmdaMemberRepository.countByFraternityId(fraternityId));
            }
        }

        return metrics;
    }

    private Province requireProvince(User user) {
        if (user.getProvince() == null) {
            throw new IllegalStateException("Utilisateur PROVINCIAL sans province associee.");
        }

        return user.getProvince();
    }

    private Region requireRegion(User user) {
        if (user.getRegion() == null) {
            throw new IllegalStateException("Utilisateur REGIONAL sans region associee.");
        }

        return user.getRegion();
    }

    private Fraternity requireFraternity(User user) {
        if (user.getFraternity() == null) {
            throw new IllegalStateException("Utilisateur BERGER sans fraternite associee.");
        }

        return user.getFraternity();
    }

    private CurrentUserScopeDTO.ScopeRefDTO toRef(Province province) {
        if (province == null) {
            return null;
        }

        return new CurrentUserScopeDTO.ScopeRefDTO(province.getId(), province.getName(), province.getDescription());
    }

    private CurrentUserScopeDTO.ScopeRefDTO toRef(Region region) {
        if (region == null) {
            return null;
        }

        return new CurrentUserScopeDTO.ScopeRefDTO(region.getId(), region.getName(), region.getDescription());
    }

    private CurrentUserScopeDTO.ScopeRefDTO toRef(Fraternity fraternity) {
        if (fraternity == null) {
            return null;
        }

        return new CurrentUserScopeDTO.ScopeRefDTO(fraternity.getId(), fraternity.getName(), fraternity.getDescription());
    }
}
