package org.cmda.management.filters;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import java.io.IOException;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // Logique personnalisée pour l'authentification
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Vous pouvez ajouter une logique personnalisée ici, par exemple pour logger les tentatives d'authentification

        return super.attemptAuthentication(request, response);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        // Logique personnalisée pour gérer une authentification réussie
        super.successfulAuthentication(request, response, chain, authResult);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        // Logique personnalisée pour gérer une authentification échouée
        super.unsuccessfulAuthentication(request, response, failed);
    }
}
