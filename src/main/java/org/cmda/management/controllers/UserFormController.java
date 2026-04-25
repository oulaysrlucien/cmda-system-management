package org.cmda.management.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserFormController {

    @GetMapping("/createUser")
    public String showCreateUserForm() {
        return "createUser";
    }
}
