package org.cmda.management.services;

import jakarta.persistence.criteria.Predicate;
import org.cmda.management.dtos.dashboard.DashboardAlertDTO;
import org.cmda.management.dtos.dashboard.DashboardDistributionDTO;
import org.cmda.management.dtos.dashboard.DashboardDistributionItemDTO;
import org.cmda.management.dtos.dashboard.DashboardMetricDTO;
import org.cmda.management.dtos.dashboard.DashboardRecentItemDTO;
import org.cmda.management.dtos.dashboard.DashboardSummaryDTO;
import org.cmda.management.entities.CmdaMember;
import org.cmda.management.entities.User;
import org.cmda.management.enums.MemberStatus;
import org.cmda.management.enums.Role;
import org.cmda.management.repositories.CmdaMemberRepository;
import org.cmda.management.repositories.FraternityRepository;
import org.cmda.management.repositories.ProvinceRepository;
import org.cmda.management.repositories.RegionRepository;
import org.cmda.management.repositories.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final CurrentUserService currentUserService;
    private final CmdaMemberRepository memberRepository;
    private final ProvinceRepository provinceRepository;
    private final RegionRepository regionRepository;
    private final FraternityRepository fraternityRepository;
    private final UserRepository userRepository;

    public DashboardService(
            CurrentUserService currentUserService,
            CmdaMemberRepository memberRepository,
            ProvinceRepository provinceRepository,
            RegionRepository regionRepository,
            FraternityRepository fraternityRepository,
            UserRepository userRepository
    ) {
        this.currentUserService = currentUserService;
        this.memberRepository = memberRepository;
        this.provinceRepository = provinceRepository;
        this.regionRepository = regionRepository;
        this.fraternityRepository = fraternityRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getCurrentUserDashboard() {
        User user = currentUserService.getCurrentUser();

        return new DashboardSummaryDTO(
                user.getRole().name(),
                resolveScopeLevel(user),
                resolveScopeLabel(user),
                buildMetrics(user),
                buildAlerts(user),
                buildDistributions(user),
                buildRecentMembers(user),
                buildRecentUsers(user)
        );
    }

    private List<DashboardMetricDTO> buildMetrics(User user) {
        List<DashboardMetricDTO> metrics = new ArrayList<>();

        switch (user.getRole()) {
            case ADMIN -> {
                metrics.add(metric("members_active", "Membres actifs", countMembers(user, MemberStatus.ACTIVE), "bi-people", "/app/members", qp("status", "ACTIVE"), "Plateforme complete"));
                metrics.add(metric("members_archived", "Membres archives", countMembers(user, MemberStatus.ARCHIVED), "bi-archive", "/app/admin/members/archives", Map.of(), "Archivage logique"));
                metrics.add(metric("provinces_active", "Provinces", provinceRepository.countByArchivedFalse(), "bi-building", "/app/admin/structures", qp("level", "province"), "Structures actives"));
                metrics.add(metric("regions_active", "Regions", regionRepository.countByArchivedFalse(), "bi-geo-alt", "/app/admin/structures", qp("level", "region"), "Structures actives"));
                metrics.add(metric("fraternities_active", "Fraternites", fraternityRepository.countByArchivedFalse(), "bi-house-heart", "/app/admin/structures", qp("level", "fraternity"), "Structures actives"));
                metrics.add(metric("users_total", "Utilisateurs", userRepository.count(), "bi-person-gear", "/app/users", Map.of(), userRepository.countByMemberIsNull() + " a completer"));
            }
            case PROVINCIAL -> {
                metrics.add(metric("members_followed", "Membres province", countMembers(user, null), "bi-people", "/app/members", Map.of(), "Hors archives"));
                metrics.add(metric("members_active", "Membres actifs", countMembers(user, MemberStatus.ACTIVE), "bi-check2-circle", "/app/members", qp("status", "ACTIVE"), "Province"));
                metrics.add(metric("members_inactive", "Membres inactifs", countMembers(user, MemberStatus.INACTIVE), "bi-pause-circle", "/app/members", qp("status", "INACTIVE"), "Province"));
                metrics.add(metric("members_archived", "Membres archives", countMembers(user, MemberStatus.ARCHIVED), "bi-archive", "/app/admin/members/archives", Map.of(), "Province"));
                metrics.add(metric("regions_active", "Regions actives", regionRepository.countByProvinceIdAndArchivedFalse(requireProvinceId(user)), "bi-geo-alt", "/app/provincial/province", Map.of(), "Province"));
                metrics.add(metric("fraternities_active", "Fraternites", fraternityRepository.countByRegionProvinceIdAndArchivedFalse(requireProvinceId(user)), "bi-house-heart", "/app/provincial/province", Map.of(), "Province"));
            }
            case REGIONAL -> {
                metrics.add(metric("members_followed", "Membres region", countMembers(user, null), "bi-people", "/app/members", Map.of(), "Hors archives"));
                metrics.add(metric("members_active", "Membres actifs", countMembers(user, MemberStatus.ACTIVE), "bi-check2-circle", "/app/members", qp("status", "ACTIVE"), "Region"));
                metrics.add(metric("members_inactive", "Membres inactifs", countMembers(user, MemberStatus.INACTIVE), "bi-pause-circle", "/app/members", qp("status", "INACTIVE"), "Region"));
                metrics.add(metric("fraternities_active", "Fraternites actives", fraternityRepository.countByRegionIdAndArchivedFalse(requireRegionId(user)), "bi-house-heart", "/app/regional/region", Map.of(), "Region"));
            }
            case BERGER -> {
                metrics.add(metric("members_followed", "Membres fraternite", countMembers(user, null), "bi-people", "/app/members", Map.of(), "Hors archives"));
                metrics.add(metric("members_active", "Membres actifs", countMembers(user, MemberStatus.ACTIVE), "bi-check2-circle", "/app/members", qp("status", "ACTIVE"), "Fraternite"));
                metrics.add(metric("members_inactive", "Membres inactifs", countMembers(user, MemberStatus.INACTIVE), "bi-pause-circle", "/app/members", qp("status", "INACTIVE"), "Fraternite"));
            }
        }

        return metrics;
    }

    private List<DashboardAlertDTO> buildAlerts(User user) {
        List<DashboardAlertDTO> alerts = new ArrayList<>();

        alerts.add(alert("members_without_email", "Membres sans email", countMissingText(user, "email"), "warning", "/app/members", qp("missing", "email")));
        alerts.add(alert("members_without_phone", "Membres sans telephone", countMissingText(user, "phoneNumber"), "warning", "/app/members", qp("missing", "phone")));
        alerts.add(alert("members_without_baptism_date", "Membres sans date de bapteme", countMissing(user, "baptismDate"), "info", "/app/members", qp("missing", "baptismDate")));
        alerts.add(alert("members_without_journey", "Membres sans cheminement", countMissing(user, "currentJourneyStage"), "warning", "/app/members", qp("missing", "journeyStage")));
        alerts.add(alert("members_without_life_state", "Membres sans etat de vie", countMissing(user, "lifeState"), "warning", "/app/members", qp("missing", "lifeState")));
        alerts.add(alert("members_without_photo", "Membres sans photo", countMissingText(user, "photoReference"), "info", "/app/members", qp("missing", "photo")));

        if (user.getRole() == Role.ADMIN) {
            alerts.add(alert("users_without_member", "Comptes sans membre rattache", userRepository.countByMemberIsNull(), "warning", "/app/users", qp("status", "incomplete")));
            alerts.add(alert("users_disabled", "Comptes desactives", userRepository.countByEnabled(false), "info", "/app/users", qp("status", "disabled")));
        }

        return alerts;
    }

    private List<DashboardDistributionDTO> buildDistributions(User user) {
        List<DashboardDistributionDTO> distributions = new ArrayList<>();

        List<DashboardDistributionItemDTO> statusItems = new ArrayList<>();
        statusItems.add(new DashboardDistributionItemDTO("Actifs", countMembers(user, MemberStatus.ACTIVE)));
        statusItems.add(new DashboardDistributionItemDTO("Inactifs", countMembers(user, MemberStatus.INACTIVE)));
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.PROVINCIAL) {
            statusItems.add(new DashboardDistributionItemDTO("Archives", countMembers(user, MemberStatus.ARCHIVED)));
        }
        distributions.add(new DashboardDistributionDTO("members_by_status", "Membres par statut", statusItems));

        List<CmdaMember> scopedMembers = memberRepository.findAll(memberScope(user, false));
        distributions.add(new DashboardDistributionDTO("members_by_journey", "Membres par cheminement", groupMembers(scopedMembers, member -> member.getCurrentJourneyStage() != null ? member.getCurrentJourneyStage().getLabel() : "Non renseigne")));
        distributions.add(new DashboardDistributionDTO("members_by_life_state", "Membres par etat de vie", groupMembers(scopedMembers, member -> member.getLifeState() != null ? member.getLifeState().getLabel() : "Non renseigne")));

        if (user.getRole() == Role.ADMIN || user.getRole() == Role.PROVINCIAL) {
            distributions.add(new DashboardDistributionDTO("members_by_region", "Membres par region", groupMembers(scopedMembers, this::regionName)));
        }
        if (user.getRole() == Role.REGIONAL || user.getRole() == Role.PROVINCIAL) {
            distributions.add(new DashboardDistributionDTO("members_by_fraternity", "Membres par fraternite", groupMembers(scopedMembers, this::fraternityName)));
        }
        if (user.getRole() == Role.ADMIN) {
            distributions.add(new DashboardDistributionDTO("users_by_role", "Utilisateurs par role", List.of(
                    new DashboardDistributionItemDTO("Admins", userRepository.countByRole(Role.ADMIN)),
                    new DashboardDistributionItemDTO("Provinciaux", userRepository.countByRole(Role.PROVINCIAL)),
                    new DashboardDistributionItemDTO("Regionaux", userRepository.countByRole(Role.REGIONAL)),
                    new DashboardDistributionItemDTO("Bergers", userRepository.countByRole(Role.BERGER))
            )));
        }

        return distributions;
    }

    private List<DashboardRecentItemDTO> buildRecentMembers(User user) {
        return memberRepository.findAll(
                        memberScope(user, false),
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
                )
                .stream()
                .map(member -> recentMember(member, "MEMBER_ADDED", member.getCreatedAt()))
                .toList();
    }

    private List<DashboardRecentItemDTO> buildRecentUsers(User user) {
        if (user.getRole() != Role.ADMIN) {
            return List.of();
        }

        return userRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(this::recentUser)
                .toList();
    }

    private long countMembers(User user, MemberStatus status) {
        boolean includeArchived = status == MemberStatus.ARCHIVED;
        if (includeArchived && user.getRole() != Role.ADMIN && user.getRole() != Role.PROVINCIAL) {
            return 0;
        }
        return memberRepository.count(memberScope(user, status, includeArchived));
    }

    private long countMissing(User user, String field) {
        return memberRepository.count(memberScope(user, false).and((root, query, cb) -> cb.isNull(root.get(field))));
    }

    private long countMissingText(User user, String field) {
        return memberRepository.count(memberScope(user, false).and((root, query, cb) ->
                cb.or(cb.isNull(root.get(field)), cb.equal(cb.trim(root.get(field)), ""))
        ));
    }

    private Specification<CmdaMember> memberScope(User user, boolean includeArchived) {
        return memberScope(user, null, includeArchived);
    }

    private Specification<CmdaMember> memberScope(User user, MemberStatus status, boolean includeArchived) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (!includeArchived) {
                predicates.add(cb.notEqual(root.get("status"), MemberStatus.ARCHIVED));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            switch (user.getRole()) {
                case PROVINCIAL -> predicates.add(cb.equal(root.get("fraternity").get("region").get("province").get("id"), requireProvinceId(user)));
                case REGIONAL -> predicates.add(cb.equal(root.get("fraternity").get("region").get("id"), requireRegionId(user)));
                case BERGER -> predicates.add(cb.equal(root.get("fraternity").get("id"), requireFraternityId(user)));
                case ADMIN -> {
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<DashboardDistributionItemDTO> groupMembers(List<CmdaMember> members, Function<CmdaMember, String> classifier) {
        return members.stream()
                .collect(Collectors.groupingBy(classifier, LinkedHashMap::new, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(8)
                .map(entry -> new DashboardDistributionItemDTO(entry.getKey(), entry.getValue()))
                .toList();
    }

    private DashboardMetricDTO metric(String code, String label, long value, String icon, String route, Map<String, String> queryParams, String hint) {
        return new DashboardMetricDTO(code, label, value, icon, route, queryParams, hint);
    }

    private DashboardAlertDTO alert(String code, String label, long value, String severity, String route, Map<String, String> queryParams) {
        return new DashboardAlertDTO(code, label, value, severity, route, queryParams);
    }

    private DashboardRecentItemDTO recentMember(CmdaMember member, String type, LocalDateTime date) {
        return new DashboardRecentItemDTO(
                member.getId(),
                type,
                displayName(member),
                fraternityName(member),
                date,
                "/app/members/" + member.getId(),
                Map.of()
        );
    }

    private DashboardRecentItemDTO recentUser(User user) {
        return new DashboardRecentItemDTO(
                user.getId(),
                "USER_CREATED",
                resolveUserDisplayName(user),
                user.getRole().name() + " - " + user.getUsername(),
                user.getCreatedAt(),
                "/app/users",
                Map.of("userId", String.valueOf(user.getId()))
        );
    }

    private Map<String, String> qp(String key, String value) {
        return Map.of(key, value);
    }

    private String resolveScopeLevel(User user) {
        return switch (user.getRole()) {
            case ADMIN -> "GLOBAL";
            case PROVINCIAL -> "PROVINCE";
            case REGIONAL -> "REGION";
            case BERGER -> "FRATERNITY";
        };
    }

    private String resolveScopeLabel(User user) {
        return switch (user.getRole()) {
            case ADMIN -> "Plateforme complete";
            case PROVINCIAL -> user.getProvince() != null ? user.getProvince().getName() : "Province non rattachee";
            case REGIONAL -> user.getRegion() != null ? user.getRegion().getName() : "Region non rattachee";
            case BERGER -> user.getFraternity() != null ? user.getFraternity().getName() : "Fraternite non rattachee";
        };
    }

    private String resolveUserDisplayName(User user) {
        if (user.getMember() != null) {
            return displayName(user.getMember());
        }
        return user.getUsername();
    }

    private String displayName(CmdaMember member) {
        return ((member.getFirstName() != null ? member.getFirstName() : "") + " " + (member.getLastName() != null ? member.getLastName() : "")).trim();
    }

    private String fraternityName(CmdaMember member) {
        return member.getFraternity() != null ? member.getFraternity().getName() : "Fraternite non renseignee";
    }

    private String regionName(CmdaMember member) {
        if (member.getFraternity() == null || member.getFraternity().getRegion() == null) {
            return "Region non renseignee";
        }
        return member.getFraternity().getRegion().getName();
    }

    private Long requireProvinceId(User user) {
        if (user.getProvince() == null) {
            throw new IllegalStateException("Utilisateur sans province associee.");
        }
        return user.getProvince().getId();
    }

    private Long requireRegionId(User user) {
        if (user.getRegion() == null) {
            throw new IllegalStateException("Utilisateur sans region associee.");
        }
        return user.getRegion().getId();
    }

    private Long requireFraternityId(User user) {
        if (user.getFraternity() == null) {
            throw new IllegalStateException("Utilisateur sans fraternite associee.");
        }
        return user.getFraternity().getId();
    }
}
