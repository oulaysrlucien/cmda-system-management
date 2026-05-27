package org.cmda.management.controllers;

import org.cmda.management.dtos.CmdaMemberDTO;
import org.cmda.management.services.CmdaMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.cmda.management.dtos.CmdaMemberWithFraternityDTO;

import io.swagger.v3.oas.annotations.tags.Tag;

// import PreAuthorize
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.cmda.management.services.CmdaMemberCSVexportService;
import org.cmda.management.services.CmdaMemberExcelExportService;
import org.cmda.management.services.CmdaMemberPdfExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.data.domain.PageRequest;


import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/members")
@Tag(name = "Members", description = "Gestion Métier des Membres")
public class CmdaMemberController {

    @Autowired
    private CmdaMemberService cmdaMemberService;



    public CmdaMemberController(CmdaMemberService cmdaMemberService) {
        this.cmdaMemberService = cmdaMemberService;
    }

    // Endpoint pour obtenir des membres filtrés avec pagination
    @GetMapping("/fraternity/{fraternityId}")
    public ResponseEntity<Page<CmdaMemberDTO>> getFilteredMembers(
            @PathVariable Long fraternityId,
            @RequestParam(defaultValue = "") String firstName,
            @RequestParam(defaultValue = "") String lastName,
            @RequestParam(defaultValue = "") String profession,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<CmdaMemberDTO> members = cmdaMemberService.getFilteredMembers(fraternityId, firstName, lastName, profession, pageable);
        return new ResponseEntity<>(members, HttpStatus.OK);
    }


    /*
     * MISE A JOUR
     * Nouvelle route principale pour recuperer les membres visibles
     * par l'utilisateur connecte.
     */
    @GetMapping
    public ResponseEntity<Page<CmdaMemberDTO>> getMembersForCurrentUser(Pageable pageable) {
        Page<CmdaMemberDTO> members = cmdaMemberService.getMembersForCurrentUser(pageable);
        return ResponseEntity.ok(members);
    }



    /*
     * ADMINISTRATION METIER
     * Retourne tous les membres pour l'administration.
     * Reserve a ADMIN.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<CmdaMemberDTO>> getAllMembersForAdministration() {
        List<CmdaMemberDTO> members = cmdaMemberService.getAllMembersForAdministration();
        return ResponseEntity.ok(members);
    }




    /*
     * MISE A JOUR
     * Archive le membre au lieu de le supprimer physiquement.
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        cmdaMemberService.archiveCmdaMember(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    // Endpoint pour créer un nouveau membre
    @PostMapping("/create")
    public ResponseEntity<CmdaMemberDTO> createMember(@RequestBody CmdaMemberDTO cmdaMemberDTO) {
        CmdaMemberDTO createdMemberDTO = cmdaMemberService.saveCmdaMember(cmdaMemberDTO); // Retourne déjà un DTO
        return new ResponseEntity<>(createdMemberDTO, HttpStatus.CREATED);
    }




    // Endpoint pour récupérer les 10 premiers membres
    @GetMapping("/list")
    public ResponseEntity<List<CmdaMemberDTO>> getFirst10Members() {
        List<CmdaMemberDTO> members = cmdaMemberService.getFirst10Members();
        return new ResponseEntity<>(members, HttpStatus.OK);
    }


    /*
     * MISE A JOUR
     * Recherche securisee des membres selon le perimetre
     * de l'utilisateur connecte.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<CmdaMemberDTO>> searchMembersForCurrentUser(
            @RequestParam(required = false) Long fraternityId,
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) Long provinceId,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String profession,
            @RequestParam(required = false) String status,
            Pageable pageable
    ) {
        Page<CmdaMemberDTO> members = cmdaMemberService.searchMembers(
                fraternityId,
                regionId,
                provinceId,
                firstName,
                lastName,
                profession,
                status,
                pageable
        );

        return ResponseEntity.ok(members);
    }





    /*
     * ADMINISTRATION METIER
     * Retourne les membres archives.
     * Reserve a ADMIN.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/archived")
    public ResponseEntity<List<CmdaMemberDTO>> getArchivedMembers() {
        List<CmdaMemberDTO> members = cmdaMemberService.getArchivedMembersForAdministration();
        return ResponseEntity.ok(members);
    }





    /*
     * ADMINISTRATION METIER
     * Retourne les membres inactifs.
     * Reserve a ADMIN.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/inactive")
    public ResponseEntity<List<CmdaMemberDTO>> getInactiveMembers() {
        List<CmdaMemberDTO> members = cmdaMemberService.getInactiveMembersForAdministration();
        return ResponseEntity.ok(members);
    }




    /*
     * CRUD METIER
     * Met a jour uniquement le statut d'un membre.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<CmdaMemberDTO> updateMemberStatus(
            @PathVariable Long id,
            @RequestBody CmdaMemberDTO cmdaMemberDTO
    ) {
        CmdaMemberDTO updatedMember = cmdaMemberService.updateMemberStatus(id, cmdaMemberDTO.getStatus());
        return ResponseEntity.ok(updatedMember);
    }




    /*
     * CRUD METIER
     * Restaure un membre archive.
     * Reserve a ADMIN.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/restore")
    public ResponseEntity<CmdaMemberDTO> restoreMember(@PathVariable Long id) {
        CmdaMemberDTO restoredMember = cmdaMemberService.restoreMember(id);
        return ResponseEntity.ok(restoredMember);
    }




    /*
     * CRUD METIER
     * Archive un membre sans suppression physique.
     */
    @PatchMapping("/{id}/archive")
    public ResponseEntity<Void> archiveMember(@PathVariable Long id) {
        cmdaMemberService.archiveCmdaMember(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }







    // Endpoint pour récupérer un membre par son ID
    @GetMapping("/{id}")
    public ResponseEntity<CmdaMemberDTO> getMemberById(@PathVariable Long id) {
        Optional<CmdaMemberDTO> member = cmdaMemberService.getMemberByIdForCurrentUser(id);
        return member.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    // Endpoint pour mettre à jour un membre
    @PutMapping("/update/{id}")
    public ResponseEntity<CmdaMemberDTO> updateMember(@PathVariable Long id, @RequestBody CmdaMemberDTO cmdaMemberDTO) {
        CmdaMemberDTO updatedMemberDTO = cmdaMemberService.updateCmdaMember(id, cmdaMemberDTO); // Utilise le DTO mis à jour
        return new ResponseEntity<>(updatedMemberDTO, HttpStatus.OK);
    }




    // Endpoint pour récupérer les membres d'une fraternité avec pagination et fraternityName
    /*
    @GetMapping("/fraternity/{fraternityId}")
    public ResponseEntity<Page<CmdaMemberDTO>> getMembersByFraternity(
            @PathVariable Long fraternityId, Pageable pageable) {
        Page<CmdaMemberDTO> members = cmdaMemberService.getMembersByFraternity(fraternityId, pageable);
        return new ResponseEntity<>(members, HttpStatus.OK);
    }
    */



    @Autowired
    private CmdaMemberCSVexportService cmdaMemberCSVexportService;

    @Autowired
    private CmdaMemberExcelExportService cmdaMemberExcelExportService;

    @Autowired
    private CmdaMemberPdfExportService cmdaMemberPdfExportService;

    // Endpoint pour exporter les membres en CSV
    @GetMapping("/export/csv")
    public ResponseEntity<String> exportMembersToCSV(
            @RequestParam(required = false) Long fraternityId,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String profession) {

        String csvContent = cmdaMemberCSVexportService.exportMembersToCSV(fraternityId, firstName, lastName, profession);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Disposition", "attachment; filename=members.csv");

        return new ResponseEntity<>(csvContent, headers, HttpStatus.OK);
    }


    // Endpoint pour exporter les membres en EXCEL
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportMembersToExcel(
            @RequestParam(required = false) Long fraternityId,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String profession) {

        byte[] excelContent = cmdaMemberExcelExportService.exportMembersToExcel(
                fraternityId,
                firstName,
                lastName,
                profession
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=members.xlsx");
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);
    }


    // Endpoint pour exporter les membres en PDF
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportMembersToPdf(
            @RequestParam(required = false) Long fraternityId,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String profession) {

        byte[] pdfContent = cmdaMemberPdfExportService.exportMembersToPdf(
                fraternityId,
                firstName,
                lastName,
                profession
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=members.pdf");
        headers.setContentType(MediaType.APPLICATION_PDF);

        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }





}
