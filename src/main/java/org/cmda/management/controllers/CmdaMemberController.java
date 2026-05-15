package org.cmda.management.controllers;

import org.cmda.management.dtos.CmdaMemberDTO;
import org.cmda.management.services.CmdaMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.cmda.management.dtos.CmdaMemberWithFraternityDTO;




import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.cmda.management.services.CmdaMemberCSVexportService;
import org.springframework.http.HttpHeaders;

import org.springframework.data.domain.PageRequest;


import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/members")
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



    // Endpoint pour récupérer les membres avec leur fraternité
    @GetMapping("/all")
    public ResponseEntity<List<CmdaMemberWithFraternityDTO>> getAllMembers() {
        List<CmdaMemberWithFraternityDTO> members = cmdaMemberService.getAllMembersWithFraternity();
        return ResponseEntity.ok(members);
    }


    // Endpoint pour supprimer un membre
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        cmdaMemberService.deleteCmdaMember(id);
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


    // Endpoint pour exporter les membres en PDF





}
