package org.cmda.management.services;

import org.cmda.management.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    @Lazy
    private CustomUserDetailsService customUserDetailsService;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Log pour le suivi des tentatives de connexion
        logger.info("Tentative de chargement de l'utilisateur : {}", username);

        // Récupérer l'utilisateur depuis UserService
        org.cmda.management.entities.User appUser = userService.findByUsername(username);
        if (appUser == null) {
            throw new UsernameNotFoundException("User not found");
        }

        logger.info("Utilisateur trouvé : {} avec le rôle : {}", appUser.getUsername(), appUser.getRole());

        // Créer un objet UserDetails à partir de l'entité User
        return new org.springframework.security.core.userdetails.User(
                appUser.getUsername(),
                appUser.getPassword(),
                //List.of(new SimpleGrantedAuthority("ROLE_" + appUser.getRole()))
                //List.of(new SimpleGrantedAuthority(appUser.getRole().name()))
                List.of(new SimpleGrantedAuthority("ROLE_" + appUser.getRole().name()))
        );
    }
}
