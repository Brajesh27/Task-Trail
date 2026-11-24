package com.tasktrail.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tasktrail.dto.EmployeeForm;
import com.tasktrail.service.EmailAlreadyUsedException;
import com.tasktrail.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/employer/employees")
public class EmployerEmployeeController {

    private final UserService userService;

    public EmployerEmployeeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/new")
    public String newEmployeeForm(Model model) {
        model.addAttribute("employeeForm", new EmployeeForm());
        return "employee-form";
    }

    @PostMapping
    public String createEmployee(@Valid @ModelAttribute("employeeForm") EmployeeForm form,
                                 BindingResult result) {
        if (result.hasErrors()) {
            return "employee-form";
        }
        try {
            userService.createEmployee(form);
        } catch (EmailAlreadyUsedException e) {
            result.rejectValue("email", "duplicate", "That email is already in use");
            return "employee-form";
        }
        return "redirect:/employer/dashboard?added";
    }
}
