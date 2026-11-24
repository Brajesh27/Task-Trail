package com.tasktrail.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.tasktrail.dto.PasswordForm;
import com.tasktrail.model.Role;
import com.tasktrail.repository.UserRepository;
import com.tasktrail.service.UserService;
import com.tasktrail.service.WrongPasswordException;

import jakarta.validation.Valid;

@Controller
public class AccountController {

    private final UserService userService;
    private final UserRepository users;

    public AccountController(UserService userService, UserRepository users) {
        this.userService = userService;
        this.users = users;
    }

    @GetMapping("/account/password")
    public String passwordForm(Principal principal, Model model) {
        model.addAttribute("passwordForm", new PasswordForm());
        model.addAttribute("dashboard", dashboardFor(principal));
        return "password-form";
    }

    @PostMapping("/account/password")
    public String changePassword(@Valid @ModelAttribute("passwordForm") PasswordForm form,
                                 BindingResult result,
                                 Principal principal,
                                 Model model) {
        model.addAttribute("dashboard", dashboardFor(principal));
        if (result.hasErrors()) {
            return "password-form";
        }
        try {
            userService.changePassword(principal.getName(), form);
        } catch (WrongPasswordException e) {
            result.rejectValue("currentPassword", "wrong", "Current password is wrong");
            return "password-form";
        }
        return "redirect:" + dashboardFor(principal) + "?passwordChanged";
    }

    private String dashboardFor(Principal principal) {
        Role role = users.findByEmail(principal.getName()).orElseThrow().getRole();
        return role == Role.EMPLOYER ? "/employer/dashboard" : "/employee/dashboard";
    }
}
