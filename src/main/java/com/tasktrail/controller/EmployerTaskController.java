package com.tasktrail.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tasktrail.dto.TaskForm;
import com.tasktrail.model.Priority;
import com.tasktrail.model.Role;
import com.tasktrail.repository.UserRepository;
import com.tasktrail.service.TaskService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/employer/tasks")
public class EmployerTaskController {

    private final TaskService taskService;
    private final UserRepository users;

    public EmployerTaskController(TaskService taskService, UserRepository users) {
        this.taskService = taskService;
        this.users = users;
    }

    // Show the empty form
    @GetMapping("/new")
    public String newTaskForm(Model model) {
        model.addAttribute("taskForm", new TaskForm());
        return showForm(model, null);
    }

    // Handle the submitted form
    @PostMapping
    public String createTask(@Valid @ModelAttribute("taskForm") TaskForm form,
                             BindingResult result,
                             Principal principal,
                             Model model) {
        if (result.hasErrors()) {
            return showForm(model, null); // show the form again, with error messages
        }
        taskService.assignTask(form, principal.getName());
        return "redirect:/employer/dashboard?assigned";
    }

    // Same form, filled with the task's current values
    @GetMapping("/{id}/edit")
    public String editTaskForm(@PathVariable Long id, Principal principal, Model model) {
        model.addAttribute("taskForm", TaskForm.from(taskService.viewTask(id, principal.getName())));
        return showForm(model, id);
    }

    @PostMapping("/{id}")
    public String updateTask(@PathVariable Long id,
                             @Valid @ModelAttribute("taskForm") TaskForm form,
                             BindingResult result,
                             Principal principal,
                             Model model) {
        if (result.hasErrors()) {
            return showForm(model, id);
        }
        taskService.updateTask(id, form, principal.getName());
        return "redirect:/employer/dashboard?updated";
    }

    @PostMapping("/{id}/delete")
    public String deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return "redirect:/employer/dashboard?deleted";
    }

    // taskId is null for a new task, so the template knows whether it is creating or editing
    private String showForm(Model model, Long taskId) {
        model.addAttribute("taskId", taskId);
        model.addAttribute("employees", users.findByRole(Role.EMPLOYEE));
        model.addAttribute("priorities", Priority.values());
        return "task-form";
    }
}
