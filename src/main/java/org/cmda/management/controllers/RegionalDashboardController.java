package org.cmda.management.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/regional")
public class RegionalDashboardController {

    @GetMapping("/dashboard")
    public String regionalDashboard() {
        return "regional/dashboard"; // Correspond au fichier resources/templates/regional/dashboard.html
    }
}
