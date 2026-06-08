package org.cmda.management.services;

import org.cmda.management.dtos.member.MemberPhotoDTO;
import org.cmda.management.entities.CmdaMember;
import org.cmda.management.entities.User;
import org.cmda.management.enums.MemberStatus;
import org.cmda.management.enums.Role;
import org.cmda.management.repositories.CmdaMemberRepository;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MemberPhotoService {

    private final CmdaMemberRepository memberRepository;
    private final CurrentUserService currentUserService;
    private final MemberPhotoStorageService storageService;

    public MemberPhotoService(
            CmdaMemberRepository memberRepository,
            CurrentUserService currentUserService,
            MemberPhotoStorageService storageService
    ) {
        this.memberRepository = memberRepository;
        this.currentUserService = currentUserService;
        this.storageService = storageService;
    }

    @Transactional
    public MemberPhotoDTO upload(Long memberId, MultipartFile file) {
        User user = requireAdmin();
        CmdaMember member = findMemberInScope(memberId, user);
        if (member.getStatus() == MemberStatus.ARCHIVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Archived members cannot be modified.");
        }

        String previousReference = member.getPhotoReference();
        String newReference = storageService.store(file);
        try {
            member.setPhotoReference(newReference);
            memberRepository.save(member);
        } catch (RuntimeException exception) {
            storageService.deleteIfPresent(newReference);
            throw exception;
        }
        storageService.deleteIfPresent(previousReference);
        return new MemberPhotoDTO(newReference);
    }

    @Transactional(readOnly = true)
    public PhotoResource load(Long memberId) {
        User user = currentUserService.getCurrentUser();
        CmdaMember member = findMemberInScope(memberId, user);
        String reference = member.getPhotoReference();
        return new PhotoResource(storageService.load(reference), storageService.contentType(reference));
    }

    @Transactional
    public void remove(Long memberId) {
        User user = requireAdmin();
        CmdaMember member = findMemberInScope(memberId, user);
        if (member.getStatus() == MemberStatus.ARCHIVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Archived members cannot be modified.");
        }
        String reference = member.getPhotoReference();
        member.setPhotoReference(null);
        memberRepository.save(member);
        storageService.deleteIfPresent(reference);
    }

    private User requireAdmin() {
        User user = currentUserService.getCurrentUser();
        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN can manage member photos.");
        }
        return user;
    }

    private CmdaMember findMemberInScope(Long memberId, User user) {
        return switch (user.getRole()) {
            case ADMIN -> memberRepository.findById(memberId).orElseThrow(this::notFound);
            case PROVINCIAL -> memberRepository.findByIdAndFraternityRegionProvinceId(memberId, requiredProvinceId(user))
                    .orElseThrow(this::notFound);
            case REGIONAL -> memberRepository.findByIdAndFraternityRegionId(memberId, requiredRegionId(user))
                    .orElseThrow(this::notFound);
            case BERGER -> memberRepository.findByIdAndFraternityId(memberId, requiredFraternityId(user))
                    .orElseThrow(this::notFound);
        };
    }

    private Long requiredProvinceId(User user) {
        if (user.getProvince() == null) throw notFound();
        return user.getProvince().getId();
    }

    private Long requiredRegionId(User user) {
        if (user.getRegion() == null) throw notFound();
        return user.getRegion().getId();
    }

    private Long requiredFraternityId(User user) {
        if (user.getFraternity() == null) throw notFound();
        return user.getFraternity().getId();
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Member photo not found.");
    }

    public record PhotoResource(Resource resource, String contentType) {
    }
}
