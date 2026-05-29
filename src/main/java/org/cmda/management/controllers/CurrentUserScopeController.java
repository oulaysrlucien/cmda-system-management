package org.cmda.management.controllers;

import org.cmda.management.dtos.CurrentUserScopeDTO;
import org.cmda.management.services.CurrentUserScopeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class CurrentUserScopeController {

    private final CurrentUserScopeService currentUserScopeService;

    public CurrentUserScopeController(CurrentUserScopeService currentUserScopeService) {
        this.currentUserScopeService = currentUserScopeService;
    }

    @GetMapping("/scope")
    public ResponseEntity<CurrentUserScopeDTO> getCurrentScope() {
        return ResponseEntity.ok(currentUserScopeService.getCurrentScope());
    }
}
