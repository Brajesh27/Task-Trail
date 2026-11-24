package com.tasktrail.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.tasktrail.dto.EmployeeStats;
import com.tasktrail.dto.TaskFilter;
import com.tasktrail.model.Priority;
import com.tasktrail.model.Role;
import com.tasktrail.model.TaskStatus;
import com.tasktrail.model.User;
import com.tasktrail.repository.UserRepository;
import com.tasktrail.service.DashboardService;
import com.tasktrail.service.TaskService;

@Controller
public class DashboardController {

    private final UserRepository users;
    private final TaskService taskService;
    private final DashboardService dashboard;

    public DashboardController(UserRepository users, TaskService taskService, DashboardService dashboard) {
        this.users = users;
        this.taskService = taskService;
        this.dashboard = dashboard;
    }

    // @ModelAttribute("filter") fills a TaskFilter from the URL (?q=...&status=...) and also puts it in the Model
    @GetMapping("/employer/dashboard")
    public String employerDashboard(@ModelAttribute("filter") TaskFilter filter, Principal principal, Model model) {
        List<EmployeeStats> teamStats = dashboard.teamStats();
        model.addAttribute("me", currentUser(principal));
        model.addAttribute("summary", dashboard.summary());
        model.addAttribute("teamStats", teamStats);
        model.addAttribute("chart", chartData(teamStats));
        model.addAttribute("attention", dashboard.needsAttention());
        model.addAttribute("tasks", taskService.search(filter, principal.getName()));
        model.addAttribute("employees", users.findByRole(Role.EMPLOYEE));
        addChoices(model);
        return "employer-dashboard";
    }

    @GetMapping("/employee/dashboard")
    public String employeeDashboard(@ModelAttribute("filter") TaskFilter filter, Principal principal, Model model) {
        model.addAttribute("me", currentUser(principal));
        model.addAttribute("tasks", taskService.search(filter, principal.getName()));
        addChoices(model);
        return "employee-dashboard";
    }

    // The numbers behind the charts; Thymeleaf turns this Map into JavaScript for Chart.js
    private Map<String, Object> chartData(List<EmployeeStats> stats) {
        return Map.of(
                "names", stats.stream().map(s -> s.employee().getName()).toList(),
                "todo", stats.stream().map(EmployeeStats::todo).toList(),
                "inProgress", stats.stream().map(EmployeeStats::inProgress).toList(),
                "done", stats.stream().map(EmployeeStats::done).toList());
    }

    private void addChoices(Model model) {
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("priorities", Priority.values());
    }

    // Principal is the logged-in user; getName() returns the username, which is the email
    private User currentUser(Principal principal) {
        return users.findByEmail(principal.getName()).orElseThrow();
    }
}
