package org.cmda.management.services;


/*
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class JwtTokenService {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenService.class);

    @Value("${jwt.secret-key}")
    private String SECRET_KEY;

    // Générer un token JWT
    public String generateToken(String username) {
        logger.info("Génération du token pour l'utilisateur : {}", username);
        try {
            String token = Jwts.builder()
                    .setSubject(username)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))  // 10 heures
                    .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                    .compact();
            logger.info("Token généré avec succès pour l'utilisateur : {}", username);
            return token;
        } catch (Exception e) {
            logger.error("Erreur lors de la génération du token pour l'utilisateur : {}", username, e);
            throw e;
        }
    }

    // Valider un token JWT
    public boolean validateToken(String token, String username) {
        logger.info("Validation du token pour l'utilisateur : {}", username);
        final String extractedUsername = extractUsername(token);
        boolean isValid = (extractedUsername.equals(username) && !isTokenExpired(token));
        if (isValid) {
            logger.info("Token valide pour l'utilisateur : {}", username);
        } else {
            logger.warn("Token invalide ou expiré pour l'utilisateur : {}", username);
        }
        return isValid;
    }

    // Extraire le nom d'utilisateur du token
    public String extractUsername(String token) {
        logger.info("Extraction du nom d'utilisateur à partir du token");
        try {
            String username = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody().getSubject();
            logger.info("Nom d'utilisateur extrait : {}", username);
            return username;
        } catch (Exception e) {
            logger.error("Erreur lors de l'extraction du nom d'utilisateur", e);
            throw e;
        }
    }

    // Vérifier si le token a expiré
    private boolean isTokenExpired(String token) {
        logger.info("Vérification si le token a expiré");
        try {
            boolean expired = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody().getExpiration().before(new Date());
            if (expired) {
                logger.warn("Le token a expiré");
            } else {
                logger.info("Le token est encore valide");
            }
            return expired;
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification de l'expiration du token", e);
            throw e;
        }
    }
}
*/


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;



import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JwtTokenService {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenService.class);

    @Value("${jwt.secret-key}")
    private String SECRET_KEY;

    // Générer un token JWT en incluant les rôles
    /*
    public String generateToken(String username, List<GrantedAuthority> authorities) {
        logger.info("Génération du token pour l'utilisateur : {}", username);
        try {
            String token = Jwts.builder()
                    .setSubject(username)
                    .claim("roles", authorities.stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList()))  // Ajouter les rôles
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))  // 10 heures
                    .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                    .compact();
            logger.info("Token généré avec succès pour l'utilisateur : {}", username);
            return token;
        } catch (Exception e) {
            logger.error("Erreur lors de la génération du token pour l'utilisateur : {}", username, e);
            throw e;
        }
    }*/

    // Dans JwtTokenService, lors de la génération du token
    public String generateToken(String username, List<GrantedAuthority> roles) {
        String roleString = roles.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roleString) // Stocke les rôles sous forme de chaîne
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 heures
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }






    // Valider un token JWT
    public boolean validateToken(String token, String username) {
        logger.info("Validation du token pour l'utilisateur : {}", username);
        final String extractedUsername = extractUsername(token);
        boolean isValid = (extractedUsername.equals(username) && !isTokenExpired(token));
        if (isValid) {
            logger.info("Token valide pour l'utilisateur : {}", username);
        } else {
            logger.warn("Token invalide ou expiré pour l'utilisateur : {}", username);
        }
        return isValid;
    }

    // Extraire le nom d'utilisateur du token
    public String extractUsername(String token) {
        logger.info("Extraction du nom d'utilisateur à partir du token");
        try {
            String username = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody().getSubject();
            logger.info("Nom d'utilisateur extrait : {}", username);
            return username;
        } catch (Exception e) {
            logger.error("Erreur lors de l'extraction du nom d'utilisateur", e);
            throw e;
        }
    }

    // Extraire les rôles du token
    public List<GrantedAuthority> extractRoles(String token) {
        String rolesString = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .get("roles", String.class);

        return Arrays.stream(rolesString.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }





    // Vérifier si le token a expiré
    private boolean isTokenExpired(String token) {
        logger.info("Vérification si le token a expiré");
        try {
            boolean expired = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody().getExpiration().before(new Date());
            if (expired) {
                logger.warn("Le token a expiré");
            } else {
                logger.info("Le token est encore valide");
            }
            return expired;
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification de l'expiration du token", e);
            throw e;
        }
    }
}
