package org.cmda.management.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.cmda.management.dtos.member.*;
import org.cmda.management.services.MemberSecureApiService;
import org.cmda.management.services.MemberSecureExportService;
import org.cmda.management.services.MemberPhotoService;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/members")
@Tag(name = "04 - MEMBERS - API SECURISEE", description = "API membres MVP : listes, detail, creation, modification, cycle de vie, photo et exports.")
public class MemberSecureApiController {

    private final MemberSecureApiService memberSecureApiService;
    private final MemberSecureExportService memberSecureExportService;
    private final MemberPhotoService memberPhotoService;

    public MemberSecureApiController(
            MemberSecureApiService memberSecureApiService,
            MemberSecureExportService memberSecureExportService,
            MemberPhotoService memberPhotoService
    ) {
        this.memberSecureApiService = memberSecureApiService;
        this.memberSecureExportService = memberSecureExportService;
        this.memberPhotoService = memberPhotoService;
    }

    @GetMapping
    public ResponseEntity<Page<MemberListDTO>> getVisibleMembers(Pageable pageable) {
        return ResponseEntity.ok(memberSecureApiService.getVisibleMembers(pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/archived")
    public ResponseEntity<Page<MemberListDTO>> getArchivedMembers(Pageable pageable) {
        return ResponseEntity.ok(memberSecureApiService.getArchivedMembers(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<MemberListDTO>> searchVisibleMembers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long fraternityId,
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) Long provinceId,
            @RequestParam(required = false) String profession,
            @RequestParam(required = false) String talentsAndSkills,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String missing,
            Pageable pageable
    ) {
        return ResponseEntity.ok(memberSecureApiService.searchVisibleMembers(
                keyword, fraternityId, regionId, provinceId, profession, talentsAndSkills, status, missing, pageable));
    }

    @GetMapping("/export/csv")
    public ResponseEntity<String> exportCsv(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long fraternityId,
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) Long provinceId,
            @RequestParam(required = false) String profession,
            @RequestParam(required = false) String talentsAndSkills,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String missing
    ) {
        String content = memberSecureExportService.exportCsv(keyword, fraternityId, regionId, provinceId, profession, talentsAndSkills, status, missing);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cmda_membres.csv");
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long fraternityId,
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) Long provinceId,
            @RequestParam(required = false) String profession,
            @RequestParam(required = false) String talentsAndSkills,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String missing
    ) {
        byte[] content = memberSecureExportService.exportExcel(keyword, fraternityId, regionId, provinceId, profession, talentsAndSkills, status, missing);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cmda_membres.xlsx");
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long fraternityId,
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) Long provinceId,
            @RequestParam(required = false) String profession,
            @RequestParam(required = false) String talentsAndSkills,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String missing
    ) {
        byte[] content = memberSecureExportService.exportPdf(keyword, fraternityId, regionId, provinceId, profession, talentsAndSkills, status, missing);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cmda_membres.pdf");
        headers.setContentType(MediaType.APPLICATION_PDF);
        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberDetailDTO> getMemberDetail(@PathVariable Long id) {
        return ResponseEntity.ok(memberSecureApiService.getMemberDetail(id));
    }

    @PostMapping
    public ResponseEntity<MemberDetailDTO> createMember(@RequestBody MemberCreateRequest request) {
        return new ResponseEntity<>(memberSecureApiService.createMember(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberDetailDTO> updateMember(
            @PathVariable Long id,
            @RequestBody MemberUpdateRequest request
    ) {
        return ResponseEntity.ok(memberSecureApiService.updateMember(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<MemberDetailDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody MemberStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(memberSecureApiService.updateStatus(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/archive")
    public ResponseEntity<MemberDetailDTO> archiveMember(
            @PathVariable Long id,
            @RequestBody MemberArchiveRequest request
    ) {
        return ResponseEntity.ok(memberSecureApiService.archiveMember(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/restore")
    public ResponseEntity<MemberDetailDTO> restoreMember(@PathVariable Long id) {
        return ResponseEntity.ok(memberSecureApiService.restoreMember(id));
    }

    @GetMapping("/references")
    public ResponseEntity<MemberReferenceDataDTO> getReferences() {
        return ResponseEntity.ok(memberSecureApiService.getReferenceData());
    }

    @PreAuthorize("hasAnyRole('PROVINCIAL', 'ADMIN')")
    @PostMapping("/{id}/journey")
    public ResponseEntity<MemberDetailDTO> transitionJourney(
            @PathVariable Long id,
            @RequestBody MemberJourneyTransitionRequest request
    ) {
        return ResponseEntity.ok(memberSecureApiService.transitionJourney(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/journey/{historyId}")
    public ResponseEntity<MemberDetailDTO> correctJourneyHistory(
            @PathVariable Long id,
            @PathVariable Long historyId,
            @RequestBody MemberJourneyCorrectionRequest request
    ) {
        return ResponseEntity.ok(memberSecureApiService.correctJourneyHistory(id, historyId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/groups")
    public ResponseEntity<MemberDetailDTO> assignGroup(@PathVariable Long id, @RequestBody MemberAffiliationRequest request) {
        return ResponseEntity.ok(memberSecureApiService.assignGroup(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/groups/{assignmentId}/end")
    public ResponseEntity<MemberDetailDTO> endGroupAssignment(
            @PathVariable Long id,
            @PathVariable Long assignmentId,
            @RequestBody MemberEndDateRequest request
    ) {
        return ResponseEntity.ok(memberSecureApiService.endGroupAssignment(id, assignmentId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/services")
    public ResponseEntity<MemberDetailDTO> assignService(@PathVariable Long id, @RequestBody MemberAffiliationRequest request) {
        return ResponseEntity.ok(memberSecureApiService.assignService(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/services/{assignmentId}/end")
    public ResponseEntity<MemberDetailDTO> endServiceAssignment(
            @PathVariable Long id,
            @PathVariable Long assignmentId,
            @RequestBody MemberEndDateRequest request
    ) {
        return ResponseEntity.ok(memberSecureApiService.endServiceAssignment(id, assignmentId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/responsibilities")
    public ResponseEntity<MemberDetailDTO> addResponsibility(@PathVariable Long id, @RequestBody MemberResponsibilityRequest request) {
        return ResponseEntity.ok(memberSecureApiService.addResponsibility(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/responsibilities/{responsibilityId}/end")
    public ResponseEntity<MemberDetailDTO> endResponsibility(
            @PathVariable Long id,
            @PathVariable Long responsibilityId,
            @RequestBody MemberEndDateRequest request
    ) {
        return ResponseEntity.ok(memberSecureApiService.endResponsibility(id, responsibilityId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MemberPhotoDTO> uploadPhoto(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(memberPhotoService.upload(id, file));
    }

    @GetMapping("/{id}/photo")
    public ResponseEntity<Resource> getPhoto(@PathVariable Long id) {
        MemberPhotoService.PhotoResource photo = memberPhotoService.load(id);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.parseMediaType(photo.contentType()))
                .body(photo.resource());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/photo")
    public ResponseEntity<Void> removePhoto(@PathVariable Long id) {
        memberPhotoService.remove(id);
        return ResponseEntity.noContent().build();
    }
}
