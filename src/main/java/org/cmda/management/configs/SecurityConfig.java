package org.cmda.management.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.web.SecurityFilterChain;


import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.web.AuthenticationEntryPoint;

import org.cmda.management.filters.JwtAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletResponse;
// CustomUserDetailsService
import org.cmda.management.services.CustomUserDetailsService;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final CustomUserDetailsService customUserDetailsService;
    public SecurityConfig(CustomUserDetailsService customUserDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Autowired
    @Lazy
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    //@Autowired
    //public void setJwtAuthenticationFilter(JwtAuthenticationFilter jwtAuthenticationFilter) {
     //   this.jwtAuthenticationFilter = jwtAuthenticationFilter;
   // }


    @Autowired
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());
    }

    // Bean pour gérer le hashage des mots de passe
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }




    //  Configuration de la sécurité avec une page de Spring Security


    /*
    // Configuration de la sécurité avec une page de login personnalisée
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                //.csrf(csrf -> csrf.disable())  // Désactiver CSRF pour Postman
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/**").hasRole("ADMIN")  // Accès réservé aux administrateurs
                        .anyRequest().authenticated()  // Authentification requise pour toute autre requête
                )
                .formLogin(login -> login
                        .loginPage("/login")  // Page de login personnalisée
                        .permitAll()  // Permet l'accès à tous pour le login
                )
                .logout(logout -> logout.permitAll())  // Permet la déconnexion
                .httpBasic(withDefaults());  // Activer l'authentification basique
        return http.build();
    }

    */



    // Configuration de la sécurité avec une page de login personnalisée étendue à tous les Rôles
    /*
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
               .csrf(csrf -> csrf.disable())  // Désactiver CSRF pour Postman
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/provincial/**").hasRole("PROVINCIAL")
                        .requestMatchers("/regional/**").hasRole("REGIONAL")
                        .requestMatchers("/bergerFraternity/**").hasRole("BERGER")
                        .anyRequest().authenticated()
                )
                // On retire formLogin() et on remplace par l'ajout du filtre JWT
                 .formLogin(form -> form
                        .loginPage("/login").permitAll()
                        .defaultSuccessUrl("/default", true)  // Rediriger vers un chemin par défaut
                )
                .logout(logout -> logout.permitAll())
                //.httpBasic(withDefaults());

               // Ajout du filtre JWT
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint())

               // tester en Désactivant l'authentification '
                 //.anyRequest().permitAll()  // Permet l'accès à toutes les requêtes sans authentification

                );


        logger.info("Configuration de sécurité appliquée avec succès.");
        return http.build();
    }
    */


    // Configuration de la sécurité avec une page de login personnalisée étendue à tous les Rôles
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Désactiver CSRF pour Postman
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/authenticate").permitAll()  // Accès public pour l'authentification JWT
                        .requestMatchers("/members/all").permitAll()  // Accès public pour l'endpoint de la liste des membres
                        .requestMatchers("/api/provinces/**").permitAll()  // Accès public pour les provinces
                        //.requestMatchers("/api/regions/**").permitAll()  // Accès public pour les régions
                        //.requestMatchers("/api/fraternities/**").permitAll()  // Accès public pour les fraternities
                        .requestMatchers("/api/users").authenticated()  // Requiert une authentification pour accéder aux utilisateurs
                        //.requestMatchers("/provinces/all").authenticated()  // Authentification requise pour accéder aux provinces
                        .requestMatchers("/regions/**").authenticated()  // Authentification requise pour accéder aux régions
                        .requestMatchers("/fraternities/**").authenticated()  // Authentification requise pour accéder aux fraternités
                        //.anyRequest().authenticated()  // Autoriser l'accès à toute autre requête authentifiée
                        // Autorisations par rôle
                        .requestMatchers("/admin/**").hasRole("ADMIN")  // Accès réservé à l'ADMIN
                        .requestMatchers("/provincial/**").hasRole("PROVINCIAL")  // Accès réservé au PROVINCIAL
                        .requestMatchers("/regional/**").hasRole("REGIONAL")  // Accès réservé au REGIONAL
                        .requestMatchers("/bergerFraternity/**").hasRole("BERGER")  // Accès réservé au BERGER

                )/*
                .formLogin(form -> form
                        .loginPage("/login")  // Page de login personnalisée
                        .permitAll()  // Autoriser l'accès à tous pour la page de login
                        .defaultSuccessUrl("/default", true)  // Rediriger vers une URL par défaut après succès
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")  // Rediriger vers la page de login après déconnexion
                        .permitAll()
                )*/
                // Ajouter le filtre JWT avant l'authentification par formulaire
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // Gestion des exceptions
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint())
                );

        logger.info("Configuration de sécurité appliquée avec succès.");
        return http.build();
    }



    //
    /*
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Désactiver CSRF pour Postman
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // Autoriser toutes les requêtes sans authentification
                );
        return http.build();
    }

     */






    // Configuration de l'entrée de point d'authentification personnalisée
    /*
    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            logger.error("Unauthorized request - {}", authException.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: " + authException.getMessage());
        };
    }*/

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            logger.error("Unauthorized request - {}", authException.getMessage());
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getOutputStream().println("{ \"error\": \"Unauthorized - " + authException.getMessage() + "\" }");
        };
    }




    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
                return authenticationManagerBuilder.build();
    }








}