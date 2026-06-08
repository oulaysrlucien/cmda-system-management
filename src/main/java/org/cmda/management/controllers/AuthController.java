package org.cmda.management.controllers;


import io.swagger.v3.oas.annotations.tags.Tag;
import org.cmda.management.dtos.AuthRequestDTO;
import org.cmda.management.dtos.AuthResponseDTO;
import org.cmda.management.services.JwtTokenService;
import org.cmda.management.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import java.util.List;


import org.cmda.management.dtos.LoginRequest;
import org.cmda.management.dtos.JwtResponse;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/api")
@Tag(name = "01 - AUTH", description = "Connexion JWT et recuperation du token.")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private UserService userService;


    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        logger.info("Authenticating user: {}", loginRequest.getUsername());
        try {
            // Authentification de l'utilisateur
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            // Configure le contexte de sécurité
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Récupère le nom d'utilisateur et les rôles pour générer le token
            String username = authentication.getName();
            List<GrantedAuthority> roles = (List<GrantedAuthority>) authentication.getAuthorities();

            // Génère le token JWT avec les rôles de l'utilisateur
            String token = jwtTokenService.generateToken(username, roles);
            userService.recordSuccessfulLogin(username);

            // Renvoie la réponse avec le token
            return ResponseEntity.ok(new JwtResponse(token));

        } catch (AuthenticationException e) {
            // Gérer l'exception si l'authentification échoue
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Échec de l'authentification : nom d'utilisateur ou mot de passe incorrect.");
        }
    }




}
