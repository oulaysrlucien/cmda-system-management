package org.cmda.management.services;

import org.cmda.management.dtos.member.*;
import org.cmda.management.entities.*;
import org.cmda.management.enums.MemberStatus;
import org.cmda.management.enums.Role;
import org.cmda.management.repositories.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MemberSecureApiService {

    private final CmdaMemberRepository memberRepository;
    private final FraternityRepository fraternityRepository;
    private final JourneyStageRepository journeyStageRepository;
    private final LifeStateRepository lifeStateRepository;
    private final ArchiveReasonRepository archiveReasonRepository;
    private final MemberJourneyHistoryRepository journeyHistoryRepository;
    private final CommunityGroupRepository communityGroupRepository;
    private final CommunityServiceRepository communityServiceRepository;
    private final MemberGroupAssignmentRepository memberGroupAssignmentRepository;
    private final MemberServiceAssignmentRepository memberServiceAssignmentRepository;
    private final MemberResponsibilityRepository memberResponsibilityRepository;
    private final CurrentUserService currentUserService;

    public MemberSecureApiService(
            CmdaMemberRepository memberRepository,
            FraternityRepository fraternityRepository,
            JourneyStageRepository journeyStageRepository,
            LifeStateRepository lifeStateRepository,
            ArchiveReasonRepository archiveReasonRepository,
            MemberJourneyHistoryRepository journeyHistoryRepository,
            CommunityGroupRepository communityGroupRepository,
            CommunityServiceRepository communityServiceRepository,
            MemberGroupAssignmentRepository memberGroupAssignmentRepository,
            MemberServiceAssignmentRepository memberServiceAssignmentRepository,
            MemberResponsibilityRepository memberResponsibilityRepository,
            CurrentUserService currentUserService
    ) {
        this.memberRepository = memberRepository;
        this.fraternityRepository = fraternityRepository;
        this.journeyStageRepository = journeyStageRepository;
        this.lifeStateRepository = lifeStateRepository;
        this.archiveReasonRepository = archiveReasonRepository;
        this.journeyHistoryRepository = journeyHistoryRepository;
        this.communityGroupRepository = communityGroupRepository;
        this.communityServiceRepository = communityServiceRepository;
        this.memberGroupAssignmentRepository = memberGroupAssignmentRepository;
        this.memberServiceAssignmentRepository = memberServiceAssignmentRepository;
        this.memberResponsibilityRepository = memberResponsibilityRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public Page<MemberListDTO> getVisibleMembers(Pageable pageable) {
        User user = currentUserService.getCurrentUser();

        return switch (user.getRole()) {
            case ADMIN -> memberRepository.findByStatusNot(MemberStatus.ARCHIVED, pageable).map(this::toListDTO);
            case PROVINCIAL -> memberRepository.findByFraternityRegionProvinceIdAndStatusNot(
                    requiredProvinceId(user), MemberStatus.ARCHIVED, pageable).map(this::toListDTO);
            case REGIONAL -> memberRepository.findByFraternityRegionIdAndStatusNot(
                    requiredRegionId(user), MemberStatus.ARCHIVED, pageable).map(this::toListDTO);
            case BERGER -> memberRepository.findByFraternityIdAndStatusNot(
                    requiredFraternityId(user), MemberStatus.ARCHIVED, pageable).map(this::toListDTO);
        };
    }

    @Transactional(readOnly = true)
    public Page<MemberListDTO> getArchivedMembers(Pageable pageable) {
        User user = currentUserService.getCurrentUser();
        requireRole(user, Role.ADMIN, "Only ADMIN can read archived members.");
        return memberRepository.findByStatus(MemberStatus.ARCHIVED, pageable).map(this::toListDTO);
    }

    @Transactional(readOnly = true)
    public Page<MemberListDTO> searchVisibleMembers(
            String keyword,
            Long fraternityId,
            Long regionId,
            Long provinceId,
            String profession,
            String talentsAndSkills,
            String status,
            String missing,
            Pageable pageable
    ) {
        User user = currentUserService.getCurrentUser();
        Specification<CmdaMember> specification = (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.notEqual(root.get("status"), MemberStatus.ARCHIVED));

            switch (user.getRole()) {
                case ADMIN -> { }
                case PROVINCIAL -> predicates.add(criteriaBuilder.equal(
                        root.get("fraternity").get("region").get("province").get("id"), requiredProvinceId(user)));
                case REGIONAL -> predicates.add(criteriaBuilder.equal(
                        root.get("fraternity").get("region").get("id"), requiredRegionId(user)));
                case BERGER -> predicates.add(criteriaBuilder.equal(
                        root.get("fraternity").get("id"), requiredFraternityId(user)));
            }

            if (provinceId != null) predicates.add(criteriaBuilder.equal(
                    root.get("fraternity").get("region").get("province").get("id"), provinceId));
            if (regionId != null) predicates.add(criteriaBuilder.equal(
                    root.get("fraternity").get("region").get("id"), regionId));
            if (fraternityId != null) predicates.add(criteriaBuilder.equal(
                    root.get("fraternity").get("id"), fraternityId));
            if (status != null && !status.isBlank()) predicates.add(criteriaBuilder.equal(
                    root.get("status"), parseStatus(status)));
            if (profession != null && !profession.isBlank()) predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("profession")), contains(profession)));
            if (talentsAndSkills != null && !talentsAndSkills.isBlank()) predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("talentsAndSkills")), contains(talentsAndSkills)));
            addMissingPredicate(missing, root, criteriaBuilder, predicates);

            if (keyword != null && !keyword.isBlank()) {
                String pattern = startsWith(keyword);
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), pattern)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return memberRepository.findAll(specification, pageable).map(this::toListDTO);
    }

    private void addMissingPredicate(
            String missing,
            jakarta.persistence.criteria.Root<CmdaMember> root,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            List<jakarta.persistence.criteria.Predicate> predicates
    ) {
        if (missing == null || missing.isBlank()) {
            return;
        }

        switch (missing.trim()) {
            case "email" -> predicates.add(blankText(root, criteriaBuilder, "email"));
            case "phone" -> predicates.add(blankText(root, criteriaBuilder, "phoneNumber"));
            case "photo" -> predicates.add(blankText(root, criteriaBuilder, "photoReference"));
            case "baptismDate" -> predicates.add(criteriaBuilder.isNull(root.get("baptismDate")));
            case "journeyStage" -> predicates.add(criteriaBuilder.isNull(root.get("currentJourneyStage")));
            case "lifeState" -> predicates.add(criteriaBuilder.isNull(root.get("lifeState")));
            default -> throw badRequest("Unknown missing member filter.");
        }
    }

    private jakarta.persistence.criteria.Predicate blankText(
            jakarta.persistence.criteria.Root<CmdaMember> root,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            String field
    ) {
        return criteriaBuilder.or(
                criteriaBuilder.isNull(root.get(field)),
                criteriaBuilder.equal(criteriaBuilder.trim(root.get(field)), "")
        );
    }

    @Transactional(readOnly = true)
    public MemberDetailDTO getMemberDetail(Long memberId) {
        User user = currentUserService.getCurrentUser();
        CmdaMember member = findMemberInScope(memberId, user);

        if (member.getStatus() == MemberStatus.ARCHIVED && user.getRole() != Role.ADMIN) {
            throw notFound("Member not found");
        }

        return toDetailDTO(member, user);
    }

    @Transactional
    public MemberDetailDTO createMember(MemberCreateRequest request) {
        User user = currentUserService.getCurrentUser();
        validateRequiredIdentity(request.firstName(), request.lastName(), request.email(), request.phoneNumber());

        CmdaMember member = new CmdaMember();
        member.setFirstName(request.firstName().trim());
        member.setLastName(request.lastName().trim());
        member.setEmail(request.email().trim());
        member.setPhoneNumber(request.phoneNumber().trim());
        if (request.birthday() != null || request.baptismDate() != null) {
            requireRole(user, Role.ADMIN, "Only ADMIN can set birth and baptism dates.");
            member.setBirthday(request.birthday());
            member.setBaptismDate(request.baptismDate());
        }
        member.setProfession(request.profession());
        member.setTalentsAndSkills(request.talentsAndSkills());
        if (request.communityEntryDate() != null) {
            requireRole(user, Role.ADMIN, "Only ADMIN can set the community entry date.");
            member.setCommunityEntryDate(request.communityEntryDate());
        }
        member.setStatus(MemberStatus.ACTIVE);
        member.setFraternity(findTargetFraternity(request.fraternityId(), user));
        member.setLifeState(findLifeState(request.lifeStateId()));

        return toDetailDTO(memberRepository.save(member), user);
    }

    @Transactional
    public MemberDetailDTO updateMember(Long memberId, MemberUpdateRequest request) {
        User user = currentUserService.getCurrentUser();
        CmdaMember member = findEditableMember(memberId, user);

        setIfNotNull(request.firstName(), value -> member.setFirstName(required(value, "firstName")));
        setIfNotNull(request.lastName(), value -> member.setLastName(required(value, "lastName")));
        setIfNotNull(request.email(), value -> member.setEmail(required(value, "email")));
        setIfNotNull(request.phoneNumber(), value -> member.setPhoneNumber(required(value, "phoneNumber")));

        if (request.birthday() != null) {
            requireRole(user, Role.ADMIN, "Only ADMIN can update the birth date.");
            member.setBirthday(request.birthday());
        }
        if (request.baptismDate() != null) {
            requireRole(user, Role.ADMIN, "Only ADMIN can update the baptism date.");
            member.setBaptismDate(request.baptismDate());
        }
        if (request.profession() != null) member.setProfession(request.profession());
        if (request.talentsAndSkills() != null) member.setTalentsAndSkills(request.talentsAndSkills());
        if (request.lifeStateId() != null) member.setLifeState(findLifeState(request.lifeStateId()));
        if (request.communityEntryDate() != null || request.definitiveCommitmentDate() != null) {
            requireRole(user, Role.ADMIN, "Only ADMIN can update community journey dates.");
            if (request.communityEntryDate() != null) member.setCommunityEntryDate(request.communityEntryDate());
            if (request.definitiveCommitmentDate() != null) member.setDefinitiveCommitmentDate(request.definitiveCommitmentDate());
        }

        if (request.fraternityId() != null && !request.fraternityId().equals(member.getFraternity().getId())) {
            requireRole(user, Role.ADMIN, "Only ADMIN can move a member to another fraternity.");
            member.setFraternity(findTargetFraternity(request.fraternityId(), user));
        }

        if (hasAddressPayload(request)) {
            requireRole(user, Role.ADMIN, "Only ADMIN can update the complete postal address.");
            member.setAddressLine1(request.addressLine1());
            member.setAddressLine2(request.addressLine2());
            member.setPostalCode(request.postalCode());
            member.setCity(request.city());
            member.setAdministrativeArea(request.administrativeArea());
            member.setCountryCode(normalizeCountryCode(request.countryCode()));
        }

        if (request.internalNotes() != null) {
            requireAnyRole(user, "Only PROVINCIAL and ADMIN can update internal notes.", Role.PROVINCIAL, Role.ADMIN);
            member.setInternalNotes(request.internalNotes());
        }

        return toDetailDTO(memberRepository.save(member), user);
    }

    @Transactional
    public MemberDetailDTO updateStatus(Long memberId, MemberStatusUpdateRequest request) {
        User user = currentUserService.getCurrentUser();
        CmdaMember member = findEditableMember(memberId, user);
        MemberStatus status = parseStatus(request.status());

        if (status == MemberStatus.ARCHIVED) {
            throw badRequest("Use archive endpoint to archive a member.");
        }

        member.setStatus(status);
        return toDetailDTO(memberRepository.save(member), user);
    }

    @Transactional
    public MemberDetailDTO archiveMember(Long memberId, MemberArchiveRequest request) {
        User user = currentUserService.getCurrentUser();
        requireRole(user, Role.ADMIN, "Only ADMIN can archive a member.");
        CmdaMember member = findEditableMember(memberId, user);
        ArchiveReason reason = archiveReasonRepository.findByIdAndActiveTrue(requiredId(request.archiveReasonId(), "archiveReasonId"))
                .orElseThrow(() -> badRequest("Unknown or inactive archive reason."));

        member.setStatus(MemberStatus.ARCHIVED);
        member.setArchiveReason(reason);
        member.setArchiveComment(normalizeArchiveComment(request.archiveComment()));
        member.setArchivedAt(LocalDateTime.now());
        return toDetailDTO(memberRepository.save(member), user);
    }

    @Transactional
    public MemberDetailDTO restoreMember(Long memberId) {
        User user = currentUserService.getCurrentUser();
        requireRole(user, Role.ADMIN, "Only ADMIN can restore a member.");
        CmdaMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> notFound("Member not found"));

        if (member.getStatus() != MemberStatus.ARCHIVED) {
            throw badRequest("Only archived members can be restored.");
        }

        member.setStatus(MemberStatus.ACTIVE);
        member.setArchiveReason(null);
        member.setArchiveComment(null);
        member.setArchivedAt(null);
        return toDetailDTO(memberRepository.save(member), user);
    }

    @Transactional(readOnly = true)
    public MemberReferenceDataDTO getReferenceData() {
        User user = currentUserService.getCurrentUser();
        List<ReferenceValueDTO> archiveReasons = canReadSensitiveFields(user)
                ? archiveReasonRepository.findByActiveTrueOrderByDisplayOrderAsc().stream().map(this::toReferenceDTO).toList()
                : List.of();

        return new MemberReferenceDataDTO(
                journeyStageRepository.findByActiveTrueOrderByDisplayOrderAsc().stream().map(this::toReferenceDTO).toList(),
                lifeStateRepository.findByActiveTrueOrderByDisplayOrderAsc().stream().map(this::toReferenceDTO).toList(),
                archiveReasons,
                communityGroupRepository.findByActiveTrueOrderByDisplayOrderAsc().stream().map(this::toReferenceDTO).toList(),
                communityServiceRepository.findByActiveTrueOrderByDisplayOrderAsc().stream().map(this::toReferenceDTO).toList()
        );
    }

    @Transactional
    public MemberDetailDTO assignGroup(Long memberId, MemberAffiliationRequest request) {
        User user = currentUserService.getCurrentUser();
        requireRole(user, Role.ADMIN, "Only ADMIN can assign member groups.");
        CmdaMember member = findEditableMember(memberId, user);
        CommunityGroup group = communityGroupRepository.findByIdAndActiveTrue(requiredId(request.referenceId(), "referenceId"))
                .orElseThrow(() -> badRequest("Unknown or inactive group."));
        if (memberGroupAssignmentRepository.existsByMemberIdAndGroupIdAndEndDateIsNull(memberId, group.getId())) {
            throw badRequest("This group is already active for the member.");
        }
        MemberGroupAssignment assignment = new MemberGroupAssignment();
        assignment.setMember(member);
        assignment.setGroup(group);
        assignment.setStartDate(request.startDate() != null ? request.startDate() : LocalDate.now());
        memberGroupAssignmentRepository.save(assignment);
        return toDetailDTO(member, user);
    }

    @Transactional
    public MemberDetailDTO assignService(Long memberId, MemberAffiliationRequest request) {
        User user = currentUserService.getCurrentUser();
        requireRole(user, Role.ADMIN, "Only ADMIN can assign member services.");
        CmdaMember member = findEditableMember(memberId, user);
        CommunityService service = communityServiceRepository.findByIdAndActiveTrue(requiredId(request.referenceId(), "referenceId"))
                .orElseThrow(() -> badRequest("Unknown or inactive service."));
        if (memberServiceAssignmentRepository.existsByMemberIdAndServiceIdAndEndDateIsNull(memberId, service.getId())) {
            throw badRequest("This service is already active for the member.");
        }
        MemberServiceAssignment assignment = new MemberServiceAssignment();
        assignment.setMember(member);
        assignment.setService(service);
        assignment.setStartDate(request.startDate() != null ? request.startDate() : LocalDate.now());
        memberServiceAssignmentRepository.save(assignment);
        return toDetailDTO(member, user);
    }

    @Transactional
    public MemberDetailDTO addResponsibility(Long memberId, MemberResponsibilityRequest request) {
        User user = currentUserService.getCurrentUser();
        requireRole(user, Role.ADMIN, "Only ADMIN can assign member responsibilities.");
        CmdaMember member = findEditableMember(memberId, user);
        MemberResponsibility responsibility = new MemberResponsibility();
        responsibility.setMember(member);
        responsibility.setTitle(required(request.title(), "title"));
        responsibility.setContextLabel(request.contextLabel());
        responsibility.setDescription(request.description());
        responsibility.setStartDate(request.startDate() != null ? request.startDate() : LocalDate.now());
        memberResponsibilityRepository.save(responsibility);
        return toDetailDTO(member, user);
    }

    @Transactional
    public MemberDetailDTO endGroupAssignment(Long memberId, Long assignmentId, MemberEndDateRequest request) {
        User user = currentUserService.getCurrentUser();
        requireRole(user, Role.ADMIN, "Only ADMIN can end member group assignments.");
        CmdaMember member = findEditableMember(memberId, user);
        MemberGroupAssignment assignment = memberGroupAssignmentRepository.findByIdAndMemberId(assignmentId, memberId)
                .orElseThrow(() -> notFound("Group assignment not found"));
        assignment.setEndDate(validateEndDate(request.endDate(), assignment.getStartDate()));
        memberGroupAssignmentRepository.save(assignment);
        return toDetailDTO(member, user);
    }

    @Transactional
    public MemberDetailDTO endServiceAssignment(Long memberId, Long assignmentId, MemberEndDateRequest request) {
        User user = currentUserService.getCurrentUser();
        requireRole(user, Role.ADMIN, "Only ADMIN can end member service assignments.");
        CmdaMember member = findEditableMember(memberId, user);
        MemberServiceAssignment assignment = memberServiceAssignmentRepository.findByIdAndMemberId(assignmentId, memberId)
                .orElseThrow(() -> notFound("Service assignment not found"));
        assignment.setEndDate(validateEndDate(request.endDate(), assignment.getStartDate()));
        memberServiceAssignmentRepository.save(assignment);
        return toDetailDTO(member, user);
    }

    @Transactional
    public MemberDetailDTO endResponsibility(Long memberId, Long responsibilityId, MemberEndDateRequest request) {
        User user = currentUserService.getCurrentUser();
        requireRole(user, Role.ADMIN, "Only ADMIN can end member responsibilities.");
        CmdaMember member = findEditableMember(memberId, user);
        MemberResponsibility responsibility = memberResponsibilityRepository.findByIdAndMemberId(responsibilityId, memberId)
                .orElseThrow(() -> notFound("Responsibility not found"));
        responsibility.setEndDate(validateEndDate(request.endDate(), responsibility.getStartDate()));
        memberResponsibilityRepository.save(responsibility);
        return toDetailDTO(member, user);
    }

    @Transactional
    public MemberDetailDTO transitionJourney(Long memberId, MemberJourneyTransitionRequest request) {
        User user = currentUserService.getCurrentUser();
        requireAnyRole(user, "Only PROVINCIAL and ADMIN can update the current journey stage.", Role.PROVINCIAL, Role.ADMIN);

        CmdaMember member = findEditableMember(memberId, user);
        JourneyStage stage = journeyStageRepository.findByIdAndActiveTrue(requiredId(request.journeyStageId(), "journeyStageId"))
                .orElseThrow(() -> badRequest("Unknown or inactive journey stage."));
        LocalDate startDate = request.startDate() != null ? request.startDate() : LocalDate.now();

        journeyHistoryRepository.findByMemberIdAndEndDateIsNull(memberId).ifPresent(current -> {
            if (!startDate.isAfter(current.getStartDate())) {
                throw badRequest("The new journey stage must start after the current stage.");
            }
            current.setEndDate(startDate.minusDays(1));
            current.setUpdatedByUserId(user.getId());
            journeyHistoryRepository.save(current);
        });

        MemberJourneyHistory history = new MemberJourneyHistory();
        history.setMember(member);
        history.setJourneyStage(stage);
        history.setStartDate(startDate);
        history.setCreatedByUserId(user.getId());
        history.setUpdatedByUserId(user.getId());
        journeyHistoryRepository.save(history);

        member.setCurrentJourneyStage(stage);
        memberRepository.save(member);

        return toDetailDTO(member, user);
    }

    @Transactional
    public MemberDetailDTO correctJourneyHistory(Long memberId, Long historyId, MemberJourneyCorrectionRequest request) {
        User user = currentUserService.getCurrentUser();
        requireRole(user, Role.ADMIN, "Only ADMIN can correct journey history dates.");

        CmdaMember member = findMemberInScope(memberId, user);
        MemberJourneyHistory history = journeyHistoryRepository.findByIdAndMemberId(historyId, memberId)
                .orElseThrow(() -> notFound("Journey history entry not found"));

        LocalDate startDate = request.startDate() != null ? request.startDate() : history.getStartDate();
        LocalDate endDate = request.endDate() != null ? request.endDate() : history.getEndDate();

        if (endDate != null && endDate.isBefore(startDate)) {
            throw badRequest("Journey history end date cannot be before start date.");
        }

        if (history.getEndDate() == null && request.endDate() != null) {
            throw badRequest("Use a journey transition to close the current stage.");
        }

        history.setStartDate(startDate);
        history.setEndDate(endDate);
        history.setUpdatedByUserId(user.getId());
        journeyHistoryRepository.save(history);

        return toDetailDTO(member, user);
    }

    private CmdaMember findEditableMember(Long memberId, User user) {
        CmdaMember member = findMemberInScope(memberId, user);
        if (member.getStatus() == MemberStatus.ARCHIVED) {
            throw badRequest("Archived members cannot be modified.");
        }
        return member;
    }

    private CmdaMember findMemberInScope(Long memberId, User user) {
        return switch (user.getRole()) {
            case ADMIN -> memberRepository.findById(memberId).orElseThrow(() -> notFound("Member not found"));
            case PROVINCIAL -> memberRepository.findByIdAndFraternityRegionProvinceId(memberId, requiredProvinceId(user))
                    .orElseThrow(() -> notFound("Member not found"));
            case REGIONAL -> memberRepository.findByIdAndFraternityRegionId(memberId, requiredRegionId(user))
                    .orElseThrow(() -> notFound("Member not found"));
            case BERGER -> memberRepository.findByIdAndFraternityId(memberId, requiredFraternityId(user))
                    .orElseThrow(() -> notFound("Member not found"));
        };
    }

    private Fraternity findTargetFraternity(Long requestedFraternityId, User user) {
        if (user.getRole() == Role.BERGER) {
            Long fraternityId = requiredFraternityId(user);
            if (requestedFraternityId != null && !requestedFraternityId.equals(fraternityId)) {
                throw forbidden("A BERGER can create members only in their fraternity.");
            }
            return fraternityRepository.findById(fraternityId).filter(fraternity -> !fraternity.isArchived())
                    .orElseThrow(() -> notFound("Fraternity not found"));
        }

        Long fraternityId = requiredId(requestedFraternityId, "fraternityId");
        return switch (user.getRole()) {
            case ADMIN -> fraternityRepository.findById(fraternityId).filter(fraternity -> !fraternity.isArchived())
                    .orElseThrow(() -> notFound("Fraternity not found"));
            case PROVINCIAL -> fraternityRepository.findByIdAndRegionProvinceIdAndArchivedFalse(fraternityId, requiredProvinceId(user))
                    .orElseThrow(() -> notFound("Fraternity not found"));
            case REGIONAL -> fraternityRepository.findByIdAndRegionIdAndArchivedFalse(fraternityId, requiredRegionId(user))
                    .orElseThrow(() -> notFound("Fraternity not found"));
            case BERGER -> throw new IllegalStateException("BERGER scope already handled.");
        };
    }

    private LifeState findLifeState(Long lifeStateId) {
        if (lifeStateId == null) return null;
        return lifeStateRepository.findByIdAndActiveTrue(lifeStateId)
                .orElseThrow(() -> badRequest("Unknown or inactive life state."));
    }

    private MemberListDTO toListDTO(CmdaMember member) {
        Fraternity fraternity = member.getFraternity();
        Region region = fraternity != null ? fraternity.getRegion() : null;
        Province province = region != null ? region.getProvince() : null;

        return new MemberListDTO(
                member.getId(), member.getFirstName(), member.getLastName(), member.getEmail(),
                member.getPhoneNumber(), member.getProfession(), member.getTalentsAndSkills(),
                member.getStatus().name(), id(fraternity), name(fraternity), id(region), name(region),
                id(province), name(province), member.getCity(), toReferenceDTO(member.getCurrentJourneyStage()),
                toReferenceDTO(member.getLifeState())
        );
    }

    private MemberDetailDTO toDetailDTO(CmdaMember member, User user) {
        Fraternity fraternity = member.getFraternity();
        Region region = fraternity != null ? fraternity.getRegion() : null;
        Province province = region != null ? region.getProvince() : null;
        List<MemberJourneyHistoryDTO> journey = journeyHistoryRepository.findByMemberIdOrderByStartDateAsc(member.getId())
                .stream().map(this::toJourneyDTO).toList();
        LocalDate journeyStageSince = journey.stream().filter(item -> item.endDate() == null)
                .map(MemberJourneyHistoryDTO::startDate).findFirst().orElse(null);
        boolean admin = user.getRole() == Role.ADMIN;
        boolean sensitive = canReadSensitiveFields(user);

        return new MemberDetailDTO(
                member.getId(), member.getFirstName(), member.getLastName(), member.getEmail(), member.getPhoneNumber(),
                admin ? member.getBirthday() : null, member.getBaptismDate(), member.getProfession(),
                member.getTalentsAndSkills(), member.getStatus().name(),
                id(fraternity), name(fraternity), id(region), name(region), id(province), name(province),
                toReferenceDTO(member.getCurrentJourneyStage()), journeyStageSince, toReferenceDTO(member.getLifeState()),
                member.getCommunityEntryDate(), member.getDefinitiveCommitmentDate(), member.getPhotoReference(),
                member.getCity(), admin ? member.getAddressLine1() : null, admin ? member.getAddressLine2() : null,
                admin ? member.getPostalCode() : null, admin ? member.getAdministrativeArea() : null,
                admin ? member.getCountryCode() : null, sensitive ? member.getInternalNotes() : null,
                sensitive ? toReferenceDTO(member.getArchiveReason()) : null, sensitive ? member.getArchiveComment() : null,
                sensitive ? member.getArchivedAt() : null, member.getCreatedAt(), member.getUpdatedAt(), journey
                , memberGroupAssignmentRepository.findByMemberIdOrderByStartDateAsc(member.getId()).stream().map(this::toAffiliationDTO).toList()
                , memberServiceAssignmentRepository.findByMemberIdOrderByStartDateAsc(member.getId()).stream().map(this::toAffiliationDTO).toList()
                , memberResponsibilityRepository.findByMemberIdOrderByStartDateAsc(member.getId()).stream().map(this::toResponsibilityDTO).toList()
        );
    }

    private MemberAffiliationDTO toAffiliationDTO(MemberGroupAssignment assignment) {
        return new MemberAffiliationDTO(assignment.getId(), toReferenceDTO(assignment.getGroup()), assignment.getStartDate(), assignment.getEndDate());
    }

    private MemberAffiliationDTO toAffiliationDTO(MemberServiceAssignment assignment) {
        return new MemberAffiliationDTO(assignment.getId(), toReferenceDTO(assignment.getService()), assignment.getStartDate(), assignment.getEndDate());
    }

    private MemberResponsibilityDTO toResponsibilityDTO(MemberResponsibility responsibility) {
        return new MemberResponsibilityDTO(responsibility.getId(), responsibility.getTitle(), responsibility.getContextLabel(),
                responsibility.getDescription(), responsibility.getStartDate(), responsibility.getEndDate());
    }

    private MemberJourneyHistoryDTO toJourneyDTO(MemberJourneyHistory history) {
        return new MemberJourneyHistoryDTO(
                history.getId(), toReferenceDTO(history.getJourneyStage()), history.getStartDate(), history.getEndDate(),
                history.getCreatedAt(), history.getCreatedByUserId(), history.getUpdatedAt(), history.getUpdatedByUserId()
        );
    }

    private ReferenceValueDTO toReferenceDTO(JourneyStage value) {
        return value == null ? null : new ReferenceValueDTO(value.getId(), value.getCode(), value.getLabel());
    }

    private ReferenceValueDTO toReferenceDTO(LifeState value) {
        return value == null ? null : new ReferenceValueDTO(value.getId(), value.getCode(), value.getLabel());
    }

    private ReferenceValueDTO toReferenceDTO(ArchiveReason value) {
        return value == null ? null : new ReferenceValueDTO(value.getId(), value.getCode(), value.getLabel());
    }

    private ReferenceValueDTO toReferenceDTO(CommunityGroup value) {
        return value == null ? null : new ReferenceValueDTO(value.getId(), value.getCode(), value.getLabel());
    }

    private ReferenceValueDTO toReferenceDTO(CommunityService value) {
        return value == null ? null : new ReferenceValueDTO(value.getId(), value.getCode(), value.getLabel());
    }

    private boolean canReadSensitiveFields(User user) {
        return user.getRole() == Role.PROVINCIAL || user.getRole() == Role.ADMIN;
    }

    private boolean hasAddressPayload(MemberUpdateRequest request) {
        return request.addressLine1() != null || request.addressLine2() != null || request.postalCode() != null
                || request.city() != null || request.administrativeArea() != null || request.countryCode() != null;
    }

    private String normalizeCountryCode(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) return countryCode;
        String normalized = countryCode.trim().toUpperCase();
        if (normalized.length() != 2) throw badRequest("countryCode must contain exactly two characters.");
        return normalized;
    }

    private String normalizeArchiveComment(String archiveComment) {
        if (archiveComment == null || archiveComment.isBlank()) return null;
        String normalized = archiveComment.trim();
        if (normalized.length() > 1000) throw badRequest("archiveComment cannot exceed 1000 characters.");
        return normalized;
    }

    private LocalDate validateEndDate(LocalDate requestedEndDate, LocalDate startDate) {
        LocalDate endDate = requestedEndDate != null ? requestedEndDate : LocalDate.now();
        if (endDate.isBefore(startDate)) throw badRequest("End date cannot be before start date.");
        return endDate;
    }

    private MemberStatus parseStatus(String status) {
        try {
            return MemberStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw badRequest("Unknown member status.");
        }
    }

    private String contains(String value) {
        return "%" + value.trim().toLowerCase() + "%";
    }

    private String startsWith(String value) {
        return value.trim().toLowerCase() + "%";
    }

    private void validateRequiredIdentity(String firstName, String lastName, String email, String phoneNumber) {
        required(firstName, "firstName");
        required(lastName, "lastName");
        required(email, "email");
        required(phoneNumber, "phoneNumber");
    }

    private String required(String value, String fieldName) {
        if (value == null || value.isBlank()) throw badRequest(fieldName + " is required.");
        return value.trim();
    }

    private Long requiredId(Long value, String fieldName) {
        if (value == null) throw badRequest(fieldName + " is required.");
        return value;
    }

    private Long requiredProvinceId(User user) {
        if (user.getProvince() == null) throw badRequest("PROVINCIAL user has no province.");
        return user.getProvince().getId();
    }

    private Long requiredRegionId(User user) {
        if (user.getRegion() == null) throw badRequest("REGIONAL user has no region.");
        return user.getRegion().getId();
    }

    private Long requiredFraternityId(User user) {
        if (user.getFraternity() == null) throw badRequest("BERGER user has no fraternity.");
        return user.getFraternity().getId();
    }

    private void requireRole(User user, Role role, String message) {
        if (user.getRole() != role) throw forbidden(message);
    }

    private void requireAnyRole(User user, String message, Role... roles) {
        for (Role role : roles) {
            if (user.getRole() == role) return;
        }
        throw forbidden(message);
    }

    private void setIfNotNull(String value, java.util.function.Consumer<String> setter) {
        if (value != null) setter.accept(value);
    }

    private Long id(Province value) { return value != null ? value.getId() : null; }
    private Long id(Region value) { return value != null ? value.getId() : null; }
    private Long id(Fraternity value) { return value != null ? value.getId() : null; }
    private String name(Province value) { return value != null ? value.getName() : null; }
    private String name(Region value) { return value != null ? value.getName() : null; }
    private String name(Fraternity value) { return value != null ? value.getName() : null; }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}
