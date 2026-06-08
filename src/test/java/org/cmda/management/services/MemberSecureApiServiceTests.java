package org.cmda.management.services;

import org.cmda.management.dtos.member.MemberDetailDTO;
import org.cmda.management.dtos.member.MemberArchiveRequest;
import org.cmda.management.dtos.member.MemberCreateRequest;
import org.cmda.management.dtos.member.MemberStatusUpdateRequest;
import org.cmda.management.dtos.member.MemberUpdateRequest;
import org.cmda.management.entities.*;
import org.cmda.management.enums.MemberStatus;
import org.cmda.management.enums.Role;
import org.cmda.management.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberSecureApiServiceTests {

    @Mock private CmdaMemberRepository memberRepository;
    @Mock private FraternityRepository fraternityRepository;
    @Mock private JourneyStageRepository journeyStageRepository;
    @Mock private LifeStateRepository lifeStateRepository;
    @Mock private ArchiveReasonRepository archiveReasonRepository;
    @Mock private MemberJourneyHistoryRepository journeyHistoryRepository;
    @Mock private CommunityGroupRepository communityGroupRepository;
    @Mock private CommunityServiceRepository communityServiceRepository;
    @Mock private MemberGroupAssignmentRepository memberGroupAssignmentRepository;
    @Mock private MemberServiceAssignmentRepository memberServiceAssignmentRepository;
    @Mock private MemberResponsibilityRepository memberResponsibilityRepository;
    @Mock private CurrentUserService currentUserService;

    private MemberSecureApiService service;
    private CmdaMember member;
    private Province province;
    private Region region;
    private Fraternity fraternity;

    @BeforeEach
    void setUp() {
        service = new MemberSecureApiService(
                memberRepository,
                fraternityRepository,
                journeyStageRepository,
                lifeStateRepository,
                archiveReasonRepository,
                journeyHistoryRepository,
                communityGroupRepository,
                communityServiceRepository,
                memberGroupAssignmentRepository,
                memberServiceAssignmentRepository,
                memberResponsibilityRepository,
                currentUserService
        );

        lenient().when(memberGroupAssignmentRepository.findByMemberIdOrderByStartDateAsc(any())).thenReturn(List.of());
        lenient().when(memberServiceAssignmentRepository.findByMemberIdOrderByStartDateAsc(any())).thenReturn(List.of());
        lenient().when(memberResponsibilityRepository.findByMemberIdOrderByStartDateAsc(any())).thenReturn(List.of());

        province = new Province();
        province.setId(1L);
        province.setName("Province Europe");

        region = new Region();
        region.setId(2L);
        region.setName("Region Nord");
        region.setProvince(province);

        fraternity = new Fraternity();
        fraternity.setId(3L);
        fraternity.setName("Fraternite Saint Paul");
        fraternity.setRegion(region);

        member = new CmdaMember();
        member.setId(4L);
        member.setFirstName("Jean");
        member.setLastName("Test");
        member.setEmail("jean.test@example.com");
        member.setPhoneNumber("0600000000");
        member.setStatus(MemberStatus.ACTIVE);
        member.setFraternity(fraternity);
        member.setAddressLine1("10 rue de la Paix");
        member.setPostalCode("75000");
        member.setCity("Paris");
        member.setCountryCode("FR");
        member.setInternalNotes("Note reservee");
        member.setBirthday(java.time.LocalDate.of(1990, 2, 3));
        member.setBaptismDate(java.time.LocalDate.of(1990, 5, 6));
    }

    @Test
    void bergerSeesCityButNotCompleteAddressOrInternalNotes() {
        User berger = user(Role.BERGER);
        berger.setFraternity(fraternity);

        when(currentUserService.getCurrentUser()).thenReturn(berger);
        when(memberRepository.findByIdAndFraternityId(4L, 3L)).thenReturn(Optional.of(member));
        when(journeyHistoryRepository.findByMemberIdOrderByStartDateAsc(4L)).thenReturn(List.of());

        MemberDetailDTO detail = service.getMemberDetail(4L);

        assertThat(detail.city()).isEqualTo("Paris");
        assertThat(detail.addressLine1()).isNull();
        assertThat(detail.postalCode()).isNull();
        assertThat(detail.countryCode()).isNull();
        assertThat(detail.internalNotes()).isNull();
        assertThat(detail.birthday()).isNull();
        assertThat(detail.baptismDate()).isEqualTo(java.time.LocalDate.of(1990, 5, 6));
    }

    @Test
    void adminSeesCompleteAddress() {
        User admin = user(Role.ADMIN);

        when(currentUserService.getCurrentUser()).thenReturn(admin);
        when(memberRepository.findById(4L)).thenReturn(Optional.of(member));
        when(journeyHistoryRepository.findByMemberIdOrderByStartDateAsc(4L)).thenReturn(List.of());

        MemberDetailDTO detail = service.getMemberDetail(4L);

        assertThat(detail.addressLine1()).isEqualTo("10 rue de la Paix");
        assertThat(detail.postalCode()).isEqualTo("75000");
        assertThat(detail.countryCode()).isEqualTo("FR");
        assertThat(detail.internalNotes()).isEqualTo("Note reservee");
        assertThat(detail.birthday()).isEqualTo(java.time.LocalDate.of(1990, 2, 3));
        assertThat(detail.baptismDate()).isEqualTo(java.time.LocalDate.of(1990, 5, 6));
    }

    @Test
    void bergerCannotUpdateCompleteAddress() {
        User berger = user(Role.BERGER);
        berger.setFraternity(fraternity);

        when(currentUserService.getCurrentUser()).thenReturn(berger);
        when(memberRepository.findByIdAndFraternityId(4L, 3L)).thenReturn(Optional.of(member));

        MemberUpdateRequest request = new MemberUpdateRequest(
                null, null, null, null, null, null, null, null, null, null,
                null, null, "20 rue Interdite", null, null, "Lyon", null, "FR", null
        );

        assertThatThrownBy(() -> service.updateMember(4L, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode().value()).isEqualTo(403));
    }

    @Test
    void bergerCannotUpdateCommunityJourneyDates() {
        User berger = user(Role.BERGER);
        berger.setFraternity(fraternity);

        when(currentUserService.getCurrentUser()).thenReturn(berger);
        when(memberRepository.findByIdAndFraternityId(4L, 3L)).thenReturn(Optional.of(member));

        MemberUpdateRequest request = new MemberUpdateRequest(
                null, null, null, null, null, null, null, null, null, null,
                java.time.LocalDate.of(2024, 1, 1), null, null, null, null, null, null, null, null
        );

        assertThatThrownBy(() -> service.updateMember(4L, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode().value()).isEqualTo(403));
    }

    @Test
    void bergerCanSetMemberInactiveInsideFraternity() {
        User berger = user(Role.BERGER);
        berger.setFraternity(fraternity);

        when(currentUserService.getCurrentUser()).thenReturn(berger);
        when(memberRepository.findByIdAndFraternityId(4L, 3L)).thenReturn(Optional.of(member));
        when(memberRepository.save(member)).thenReturn(member);
        when(journeyHistoryRepository.findByMemberIdOrderByStartDateAsc(4L)).thenReturn(List.of());

        MemberDetailDTO detail = service.updateStatus(4L, new MemberStatusUpdateRequest("INACTIVE"));

        assertThat(detail.status()).isEqualTo("INACTIVE");
    }

    @Test
    void bergerCannotArchiveMember() {
        User berger = user(Role.BERGER);
        berger.setFraternity(fraternity);
        when(currentUserService.getCurrentUser()).thenReturn(berger);

        assertThatThrownBy(() -> service.archiveMember(4L, new MemberArchiveRequest(1L, null)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode().value()).isEqualTo(403));
    }

    @Test
    void adminArchivesMemberWithReasonAndCanRestoreIt() {
        User admin = user(Role.ADMIN);
        ArchiveReason reason = new ArchiveReason();
        reason.setCode("DEPART_COMMUNAUTE");
        reason.setLabel("Depart de la communaute");

        when(currentUserService.getCurrentUser()).thenReturn(admin);
        when(memberRepository.findById(4L)).thenReturn(Optional.of(member));
        when(archiveReasonRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(reason));
        when(memberRepository.save(any(CmdaMember.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(journeyHistoryRepository.findByMemberIdOrderByStartDateAsc(4L)).thenReturn(List.of());

        MemberDetailDTO archived = service.archiveMember(4L, new MemberArchiveRequest(1L, "Depart confirme"));
        assertThat(archived.status()).isEqualTo("ARCHIVED");
        assertThat(archived.archiveReason().code()).isEqualTo("DEPART_COMMUNAUTE");
        assertThat(archived.archivedAt()).isNotNull();

        MemberDetailDTO restored = service.restoreMember(4L);
        assertThat(restored.status()).isEqualTo("ACTIVE");
        assertThat(restored.archiveReason()).isNull();
        assertThat(restored.archivedAt()).isNull();
    }

    @Test
    void adminCanAssignGroupToMember() {
        User admin = user(Role.ADMIN);
        CommunityGroup group = org.mockito.Mockito.mock(CommunityGroup.class);
        when(group.getId()).thenReturn(20L);

        when(currentUserService.getCurrentUser()).thenReturn(admin);
        when(memberRepository.findById(4L)).thenReturn(Optional.of(member));
        when(communityGroupRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(group));
        when(memberGroupAssignmentRepository.existsByMemberIdAndGroupIdAndEndDateIsNull(4L, 20L)).thenReturn(false);
        when(memberGroupAssignmentRepository.save(any(MemberGroupAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(journeyHistoryRepository.findByMemberIdOrderByStartDateAsc(4L)).thenReturn(List.of());

        MemberDetailDTO detail = service.assignGroup(4L, new org.cmda.management.dtos.member.MemberAffiliationRequest(20L, null));

        assertThat(detail.id()).isEqualTo(4L);
    }

    @Test
    void bergerCannotAssignGroupToMember() {
        User berger = user(Role.BERGER);
        berger.setFraternity(fraternity);
        when(currentUserService.getCurrentUser()).thenReturn(berger);

        assertThatThrownBy(() -> service.assignGroup(4L, new org.cmda.management.dtos.member.MemberAffiliationRequest(20L, null)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode().value()).isEqualTo(403));
    }

    @Test
    void bergerCreatesMemberInsideOwnFraternityWhenFraternityIdIsNotProvided() {
        User berger = user(Role.BERGER);
        berger.setFraternity(fraternity);

        when(currentUserService.getCurrentUser()).thenReturn(berger);
        when(fraternityRepository.findById(3L)).thenReturn(Optional.of(fraternity));
        when(memberRepository.save(any(CmdaMember.class))).thenAnswer(invocation -> {
            CmdaMember saved = invocation.getArgument(0);
            saved.setId(40L);
            return saved;
        });
        when(journeyHistoryRepository.findByMemberIdOrderByStartDateAsc(40L)).thenReturn(List.of());

        MemberDetailDTO detail = service.createMember(new MemberCreateRequest(
                "Marie", "Kouassi", "marie@example.com", "0700000000",
                null, null, "Comptable", "Chant", null, null, null
        ));

        assertThat(detail.fraternityId()).isEqualTo(3L);
        assertThat(detail.provinceName()).isEqualTo("Province Europe");
        verify(memberRepository).save(any(CmdaMember.class));
    }

    @Test
    void regionalCannotCreateMemberInFraternityOutsideOwnRegion() {
        User regional = user(Role.REGIONAL);
        regional.setRegion(region);

        when(currentUserService.getCurrentUser()).thenReturn(regional);
        when(fraternityRepository.findByIdAndRegionIdAndArchivedFalse(99L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createMember(new MemberCreateRequest(
                "Paul", "Horsregion", "paul@example.com", "0700000001",
                null, null, null, null, 99L, null, null
        )))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode().value()).isEqualTo(404));
    }

    @Test
    void provincialCanUpdateInternalNotesInsideProvince() {
        User provincial = user(Role.PROVINCIAL);
        provincial.setProvince(province);

        when(currentUserService.getCurrentUser()).thenReturn(provincial);
        when(memberRepository.findByIdAndFraternityRegionProvinceId(4L, 1L)).thenReturn(Optional.of(member));
        when(memberRepository.save(member)).thenReturn(member);
        when(journeyHistoryRepository.findByMemberIdOrderByStartDateAsc(4L)).thenReturn(List.of());

        MemberDetailDTO detail = service.updateMember(4L, new MemberUpdateRequest(
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, "Suivi provincial"
        ));

        assertThat(detail.internalNotes()).isEqualTo("Suivi provincial");
        assertThat(member.getInternalNotes()).isEqualTo("Suivi provincial");
    }

    @Test
    void regionalCannotTransitionJourneyStage() {
        User regional = user(Role.REGIONAL);
        regional.setRegion(region);
        when(currentUserService.getCurrentUser()).thenReturn(regional);

        assertThatThrownBy(() -> service.transitionJourney(
                4L,
                new org.cmda.management.dtos.member.MemberJourneyTransitionRequest(7L, java.time.LocalDate.of(2026, 1, 1))
        ))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode().value()).isEqualTo(403));
    }

    @Test
    void provincialCanTransitionJourneyStageInsideProvince() {
        User provincial = user(Role.PROVINCIAL);
        provincial.setProvince(province);
        JourneyStage stage = org.mockito.Mockito.mock(JourneyStage.class);
        when(stage.getId()).thenReturn(7L);
        when(stage.getCode()).thenReturn("STAGIAIRE");
        when(stage.getLabel()).thenReturn("Stagiaire");

        when(currentUserService.getCurrentUser()).thenReturn(provincial);
        when(memberRepository.findByIdAndFraternityRegionProvinceId(4L, 1L)).thenReturn(Optional.of(member));
        when(journeyStageRepository.findByIdAndActiveTrue(7L)).thenReturn(Optional.of(stage));
        when(journeyHistoryRepository.findByMemberIdAndEndDateIsNull(4L)).thenReturn(Optional.empty());
        when(journeyHistoryRepository.save(any(MemberJourneyHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(memberRepository.save(member)).thenReturn(member);
        when(journeyHistoryRepository.findByMemberIdOrderByStartDateAsc(4L)).thenReturn(List.of());

        MemberDetailDTO detail = service.transitionJourney(
                4L,
                new org.cmda.management.dtos.member.MemberJourneyTransitionRequest(7L, java.time.LocalDate.of(2026, 1, 1))
        );

        assertThat(detail.currentJourneyStage().label()).isEqualTo("Stagiaire");
        assertThat(member.getCurrentJourneyStage()).isEqualTo(stage);
    }

    @Test
    void adminCannotRestoreNonArchivedMember() {
        User admin = user(Role.ADMIN);
        member.setStatus(MemberStatus.ACTIVE);

        when(currentUserService.getCurrentUser()).thenReturn(admin);
        when(memberRepository.findById(4L)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> service.restoreMember(4L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode().value()).isEqualTo(400));
    }

    private User user(Role role) {
        User user = new User();
        user.setId(10L);
        user.setRole(role);
        return user;
    }
}
