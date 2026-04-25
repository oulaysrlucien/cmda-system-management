package org.cmda.management.controllers;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class RoleBasedRedirectController {

    @GetMapping("/default")
    public String defaultAfterLogin(Authentication authentication) {
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PROVINCIAL"))) {
            return "redirect:/provincial/dashboard";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_REGIONAL"))) {
            return "redirect:/regional/dashboard";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_BERGER"))) {
            return "redirect:/bergerFraternity/dashboard";
        }
        return "redirect:/";
    }
}
