package com.tasktrail.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.tasktrail.model.Priority;
import com.tasktrail.model.Task;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Holds what the employer typed into the "Assign task" or "Edit task" form. */
public class TaskForm {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be 100 characters or less")
    private String title;

    @Size(max = 500, message = "Description must be 500 characters or less")
    private String description;

    @NotNull(message = "Pick a due date")
    @FutureOrPresent(message = "Due date cannot be in the past")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDate;

    @NotNull(message = "Pick an employee")
    private Long assigneeId;

    @NotNull(message = "Pick a priority")
    private Priority priority = Priority.MEDIUM;

    /** Pre-fills the Edit form with a task's current values. */
    public static TaskForm from(Task task) {
        TaskForm form = new TaskForm();
        form.title = task.getTitle();
        form.description = task.getDescription();
        form.dueDate = task.getDueDate();
        form.assigneeId = task.getAssignee().getId();
        form.priority = task.getPriority();
        return form;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
}
