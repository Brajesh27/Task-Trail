package com.tasktrail.controller;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * A controller receives browser requests and decides which page to show.
 * "Model" is a bag of values the HTML template can read.
 */
@Controller
public class HomeController {

    // Handles GET http://localhost:8080/
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("today", LocalDate.now());
        return "home"; // renders src/main/resources/templates/home.html
    }
}
