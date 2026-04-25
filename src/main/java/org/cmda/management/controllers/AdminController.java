package org.cmda.management.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    // Ajoutez des méthodes pour gérer les provinces, régions, utilisateurs
}
