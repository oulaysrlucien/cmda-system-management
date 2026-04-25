package org.cmda.management.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/bergerFraternity")
public class BergerDashboardController {

    @GetMapping("/dashboard")
    public String bergerDashboard() {
        return "bergerFraternity/dashboard"; // Correspond au fichier resources/templates/bergerFraternity/dashboard.html
    }
}
