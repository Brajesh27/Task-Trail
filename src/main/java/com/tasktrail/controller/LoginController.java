package com.tasktrail.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    // /login?as=employer or /login?as=employee
    @GetMapping("/login")
    public String login(@RequestParam(defaultValue = "employee") String as, Model model) {
        boolean employer = as.equals("employer");
        model.addAttribute("as", employer ? "employer" : "employee");
        model.addAttribute("title", employer ? "Employer login" : "Employee login");
        return "login";
    }
}
