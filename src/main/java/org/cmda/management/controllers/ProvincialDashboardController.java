package org.cmda.management.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/provincial")
public class ProvincialDashboardController {

    @GetMapping("/dashboard")
    public String provincialDashboard() {
        return "provincial/dashboard";
    }

    // Autres méthodes spécifiques au tableau de bord provincial peuvent être ajoutées ici
}
