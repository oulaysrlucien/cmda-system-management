package org.cmda.management.services;

import org.cmda.management.entities.CmdaMember;
import org.cmda.management.entities.User;
import org.cmda.management.enums.MemberStatus;
import org.cmda.management.enums.Role;
import org.cmda.management.repositories.CmdaMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberPhotoServiceTests {

    @TempDir Path tempDirectory;
    @Mock private CmdaMemberRepository memberRepository;
    @Mock private CurrentUserService currentUserService;

    private MemberPhotoService service;
    private CmdaMember member;

    @BeforeEach
    void setUp() {
        MemberPhotoStorageService storageService = new MemberPhotoStorageService(tempDirectory.toString());
        service = new MemberPhotoService(memberRepository, currentUserService, storageService);

        member = new CmdaMember();
        member.setId(4L);
        member.setStatus(MemberStatus.ACTIVE);
    }

    @Test
    void adminCanUploadAndReplacePhoto() throws Exception {
        member.setPhotoReference("old-photo.jpg");
        Files.writeString(tempDirectory.resolve("old-photo.jpg"), "old");
        when(currentUserService.getCurrentUser()).thenReturn(user(Role.ADMIN));
        when(memberRepository.findById(4L)).thenReturn(Optional.of(member));

        var result = service.upload(4L, jpegPhoto());

        assertThat(result.photoReference()).endsWith(".jpg");
        assertThat(Files.exists(tempDirectory.resolve(result.photoReference()))).isTrue();
        assertThat(Files.exists(tempDirectory.resolve("old-photo.jpg"))).isFalse();
        verify(memberRepository).save(member);
    }

    @Test
    void bergerCannotUploadPhoto() {
        when(currentUserService.getCurrentUser()).thenReturn(user(Role.BERGER));

        assertThatThrownBy(() -> service.upload(4L, jpegPhoto()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode().value()).isEqualTo(403));
    }

    @Test
    void invalidFormatIsRejected() {
        when(currentUserService.getCurrentUser()).thenReturn(user(Role.ADMIN));
        when(memberRepository.findById(4L)).thenReturn(Optional.of(member));
        var invalid = new MockMultipartFile("file", "member.gif", "image/gif", "gif".getBytes());

        assertThatThrownBy(() -> service.upload(4L, invalid))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode().value()).isEqualTo(400));
    }

    @Test
    void spoofedJpegIsRejected() {
        when(currentUserService.getCurrentUser()).thenReturn(user(Role.ADMIN));
        when(memberRepository.findById(4L)).thenReturn(Optional.of(member));
        var invalid = new MockMultipartFile("file", "member.jpg", "image/jpeg", "not-a-jpeg".getBytes());

        assertThatThrownBy(() -> service.upload(4L, invalid))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode().value()).isEqualTo(400));
    }

    @Test
    void adminCanRemovePhoto() throws Exception {
        member.setPhotoReference("member.jpg");
        Files.writeString(tempDirectory.resolve("member.jpg"), "photo");
        when(currentUserService.getCurrentUser()).thenReturn(user(Role.ADMIN));
        when(memberRepository.findById(4L)).thenReturn(Optional.of(member));

        service.remove(4L);

        assertThat(member.getPhotoReference()).isNull();
        assertThat(Files.exists(tempDirectory.resolve("member.jpg"))).isFalse();
        verify(memberRepository).save(member);
    }

    private MockMultipartFile jpegPhoto() {
        return new MockMultipartFile("file", "member.jpg", "image/jpeg",
                new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0x00});
    }

    private User user(Role role) {
        User user = new User();
        user.setId(10L);
        user.setRole(role);
        return user;
    }
}
