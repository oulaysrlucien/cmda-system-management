package org.cmda.management.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class UserLoginController {

    private static final Logger logger = LoggerFactory.getLogger(UserLoginController.class);

    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // Vue pour afficher le formulaire de connexion
    }

    @PostMapping("/login")
    public ModelAndView processLogin(@RequestParam String username, @RequestParam String password) {
        // Logique de traitement de la connexion, à adapter selon votre application
        logger.info("Tentative de connexion avec l'utilisateur: {}", username);
        ModelAndView modelAndView = new ModelAndView("dashboard");
        modelAndView.addObject("username", username);
        return modelAndView;
    }
}
