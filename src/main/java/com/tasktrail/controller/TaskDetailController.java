package com.tasktrail.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tasktrail.model.Role;
import com.tasktrail.repository.UserRepository;
import com.tasktrail.service.TaskService;

/** One task's page: details, comments and history. Open to the employer and the assigned employee. */
@Controller
public class TaskDetailController {

    private final TaskService taskService;
    private final UserRepository users;

    public TaskDetailController(TaskService taskService, UserRepository users) {
        this.taskService = taskService;
        this.users = users;
    }

    @GetMapping("/tasks/{id}")
    public String showTask(@PathVariable Long id, Principal principal, Model model) {
        boolean employer = users.findByEmail(principal.getName()).orElseThrow().getRole() == Role.EMPLOYER;
        model.addAttribute("task", taskService.viewTask(id, principal.getName()));
        model.addAttribute("employer", employer);
        model.addAttribute("dashboard", employer ? "/employer/dashboard" : "/employee/dashboard");
        return "task-detail";
    }

    @PostMapping("/tasks/{id}/comments")
    public String addComment(@PathVariable Long id, @RequestParam String text, Principal principal) {
        if (text.isBlank() || text.length() > 500) {
            return "redirect:/tasks/" + id + "?commentError";
        }
        taskService.addComment(id, text, principal.getName());
        return "redirect:/tasks/" + id + "#comments";
    }
}
