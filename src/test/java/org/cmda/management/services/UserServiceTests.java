package org.cmda.management.services;

import org.cmda.management.dtos.UserCreationDTO;
import org.cmda.management.entities.CmdaMember;
import org.cmda.management.entities.Fraternity;
import org.cmda.management.entities.Province;
import org.cmda.management.entities.Region;
import org.cmda.management.entities.User;
import org.cmda.management.enums.MemberStatus;
import org.cmda.management.repositories.CmdaMemberRepository;
import org.cmda.management.repositories.FraternityRepository;
import org.cmda.management.repositories.ProvinceRepository;
import org.cmda.management.repositories.RegionRepository;
import org.cmda.management.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserRepository userRepository;
    @Mock private ProvinceRepository provinceRepository;
    @Mock private RegionRepository regionRepository;
    @Mock private FraternityRepository fraternityRepository;
    @Mock private CmdaMemberRepository memberRepository;

    private UserService service;
    private Province provinceA;
    private Province provinceB;
    private Region regionA;
    private Region regionB;
    private Fraternity fraternityA;
    private Fraternity fraternityB;

    @BeforeEach
    void setUp() {
        service = new UserService(
                passwordEncoder,
                userRepository,
                provinceRepository,
                regionRepository,
                fraternityRepository,
                memberRepository
        );

        provinceA = province(1L, "Province A");
        provinceB = province(2L, "Province B");
        regionA = region(10L, "Region A", provinceA);
        regionB = region(20L, "Region B", provinceB);
        fraternityA = fraternity(100L, "Fraternite A", regionA);
        fraternityB = fraternity(200L, "Fraternite B", regionB);

        when(passwordEncoder.encode("password123")).thenReturn("encoded");
    }

    @Test
    void saveBergerRejectsMemberFromAnotherFraternity() {
        when(fraternityRepository.findById(100L)).thenReturn(Optional.of(fraternityA));
        when(memberRepository.findById(99L)).thenReturn(Optional.of(member(99L, fraternityB)));

        UserCreationDTO request = request("berger@test.com", "BERGER", 99L);
        request.setFraternityId(100L);

        assertThatThrownBy(() -> service.saveUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Un Berger doit etre lie a un membre de sa fraternite");
    }

    @Test
    void saveRegionalRejectsMemberFromAnotherRegion() {
        when(regionRepository.findById(10L)).thenReturn(Optional.of(regionA));
        when(memberRepository.findById(99L)).thenReturn(Optional.of(member(99L, fraternityB)));

        UserCreationDTO request = request("regional@test.com", "REGIONAL", 99L);
        request.setRegionId(10L);

        assertThatThrownBy(() -> service.saveUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Un Regional doit etre lie a un membre de sa region");
    }

    @Test
    void saveProvincialRejectsMemberFromAnotherProvince() {
        when(provinceRepository.findById(1L)).thenReturn(Optional.of(provinceA));
        when(memberRepository.findById(99L)).thenReturn(Optional.of(member(99L, fraternityB)));

        UserCreationDTO request = request("provincial@test.com", "PROVINCIAL", 99L);
        request.setProvinceId(1L);

        assertThatThrownBy(() -> service.saveUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Un Provincial doit etre lie a un membre de sa province");
    }

    @Test
    void saveProvincialAcceptsMemberFromSameProvince() {
        when(provinceRepository.findById(1L)).thenReturn(Optional.of(provinceA));
        when(memberRepository.findById(99L)).thenReturn(Optional.of(member(99L, fraternityA)));

        UserCreationDTO request = request("provincial@test.com", "PROVINCIAL", 99L);
        request.setProvinceId(1L);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = service.saveUser(request);

        assertThat(saved.getProvince()).isEqualTo(provinceA);
        assertThat(saved.getMember().getId()).isEqualTo(99L);
        verify(userRepository).save(saved);
    }

    private UserCreationDTO request(String username, String role, Long memberId) {
        UserCreationDTO request = new UserCreationDTO();
        request.setUsername(username);
        request.setPassword("password123");
        request.setRole(role);
        request.setMemberId(memberId);
        request.setEnabled(true);
        return request;
    }

    private Province province(Long id, String name) {
        Province province = new Province();
        province.setId(id);
        province.setName(name);
        return province;
    }

    private Region region(Long id, String name, Province province) {
        Region region = new Region();
        region.setId(id);
        region.setName(name);
        region.setProvince(province);
        return region;
    }

    private Fraternity fraternity(Long id, String name, Region region) {
        Fraternity fraternity = new Fraternity();
        fraternity.setId(id);
        fraternity.setName(name);
        fraternity.setRegion(region);
        return fraternity;
    }

    private CmdaMember member(Long id, Fraternity fraternity) {
        CmdaMember member = new CmdaMember();
        member.setId(id);
        member.setFirstName("Nael");
        member.setLastName("Yao");
        member.setEmail("nael.yao@example.com");
        member.setPhoneNumber("0600000000");
        member.setStatus(MemberStatus.ACTIVE);
        member.setFraternity(fraternity);
        return member;
    }
}
