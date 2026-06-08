package org.cmda.management.configs;

import org.cmda.management.filters.JwtAuthenticationFilter;
import org.cmda.management.services.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;


    // Injection des dépendances pour le filtre d'authentification JWT et le service de détails utilisateur personnalisé, qui sont utilisés pour gérer l'authentification et l'autorisation dans l'application
    // Le JwtAuthenticationFilter est responsable de l'extraction et de la validation du token JWT dans les requêtes entrantes, tandis que le CustomUserDetailsService fournit les détails de l'utilisateur nécessaires pour l'authentification
    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomUserDetailsService customUserDetailsService
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customUserDetailsService = customUserDetailsService;
    }


    // Configuration de la chaîne de filtres de sécurité pour gérer l'authentification et l'autorisation des requêtes HTTP
    // Cette configuration inclut la désactivation de CSRF, la gestion des sessions en mode stateless, l'ajout du filtre d'authentification JWT, et la définition des règles d'autorisation pour les différentes routes de l'API
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/authenticate").permitAll()

                        .requestMatchers("/api/me/**").authenticated()

                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        .requestMatchers("/members/**").authenticated()

                        .requestMatchers("/provinces/**").hasRole("ADMIN")
                        .requestMatchers("/regions/**").hasRole("ADMIN")
                        .requestMatchers("/fraternities/**").hasRole("ADMIN")

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/error"
                        ).permitAll()


                        .anyRequest().authenticated()


                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint())
                        .accessDeniedHandler(customAccessDeniedHandler())
                );

        logger.info("Configuration de securite JWT appliquee.");
        return http.build();
    }


    // Configuration de l'AuthenticationProvider pour utiliser le service de détails utilisateur personnalisé et le hachage des mots de passe avec BCrypt, ce qui permet à Spring Security de gérer l'authentification des utilisateurs en vérifiant les informations d'identification fournies lors de la connexion
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }


    // Utilisation de BCrypt pour le hachage des mots de passe, ce qui est une pratique recommandée pour la sécurité
    // BCrypt est un algorithme de hachage adaptatif qui rend les attaques par force brute plus difficiles en augmentant le temps de calcul nécessaire pour chaque tentative de mot de passe
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    // Gestionnaire d'entrée d'authentification personnalisé pour les requêtes non authentifiées
    // Ce gestionnaire renvoie une réponse JSON avec un message d'erreur clair au lieu de rediriger vers une page de connexion
    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            logger.error("Unauthorized request - {}", authException.getMessage());
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getOutputStream().println("{ \"error\": \"Unauthorized - " + authException.getMessage() + "\" }");
        };
    }



    // Gestionnaire d'accès refusé personnalisé pour les utilisateurs authentifiés mais sans les autorisations nécessaires
    // Ce gestionnaire renvoie une réponse JSON avec un message d'erreur clair au lieu de rediriger vers une page d'erreur
    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            logger.error("Forbidden request - {}", accessDeniedException.getMessage());
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getOutputStream().println("{ \"error\": \"Forbidden - Access denied\" }");
        };
    }



    // Configuration CORS pour permettre les requêtes depuis le frontend Angular
    // Cette configuration permet les requêtes depuis http://localhost:4200, ce qui est l'URL par défaut pour les applications Angular en développement
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
