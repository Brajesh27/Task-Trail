package com.tasktrail.controller;

import java.net.URI;
import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tasktrail.dto.StatusUpdate;
import com.tasktrail.dto.TaskFilter;
import com.tasktrail.dto.TaskForm;
import com.tasktrail.dto.TaskResponse;
import com.tasktrail.service.TaskService;

import jakarta.validation.Valid;

/**
 * JSON version of the task features, for scripts and other apps.
 * @RestController = @Controller + every method's return value is written as JSON instead of naming a template.
 * Log in with HTTP Basic, e.g.  curl -u boss@tasktrail.com:boss123 http://localhost:8080/api/tasks
 */
@RestController
@RequestMapping("/api/tasks")
public class ApiTaskController {

    private final TaskService taskService;

    public ApiTaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // GET /api/tasks?status=TODO&priority=HIGH&q=report  (employers see all tasks, employees their own)
    @GetMapping
    public List<TaskResponse> list(TaskFilter filter, Principal principal) {
        return taskService.search(filter, principal.getName()).stream().map(TaskResponse::from).toList();
    }

    @GetMapping("/{id}")
    public TaskResponse get(@PathVariable Long id, Principal principal) {
        return TaskResponse.from(taskService.viewTask(id, principal.getName()));
    }

    // @PreAuthorize checks the role before the method runs; it needs @EnableMethodSecurity (SecurityConfig)
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskForm form, Principal principal) {
        TaskResponse created = TaskResponse.from(taskService.assignTask(form, principal.getName()));
        return ResponseEntity.created(URI.create("/api/tasks/" + created.id())).body(created); // 201 Created
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public TaskResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdate update, Principal principal) {
        return TaskResponse.from(taskService.updateStatus(id, update.status(), principal.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
