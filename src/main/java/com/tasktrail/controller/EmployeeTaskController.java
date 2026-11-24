package com.tasktrail.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tasktrail.model.TaskStatus;
import com.tasktrail.service.TaskService;

@Controller
public class EmployeeTaskController {

    private final TaskService taskService;

    public EmployeeTaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // POST /employee/tasks/5/status with status=DONE
    @PostMapping("/employee/tasks/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam TaskStatus status,
                               Principal principal) {
        taskService.updateStatus(id, status, principal.getName());
        return "redirect:/employee/dashboard?updated";
    }
}
