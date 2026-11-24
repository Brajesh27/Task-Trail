package com.tasktrail.dto;

import java.time.LocalDate;

import com.tasktrail.model.Priority;
import com.tasktrail.model.Task;
import com.tasktrail.model.TaskStatus;

/**
 * What the REST API sends back for a task, as JSON.
 * We never return the Task entity itself: it would leak the assignee's password hash.
 */
public record TaskResponse(Long id, String title, String description, LocalDate dueDate,
                           LocalDate originalDueDate, TaskStatus status, Priority priority,
                           int carryForwardCount, String assigneeEmail, String assigneeName) {

    public static TaskResponse from(Task t) {
        return new TaskResponse(t.getId(), t.getTitle(), t.getDescription(), t.getDueDate(),
                t.getOriginalDueDate(), t.getStatus(), t.getPriority(), t.getCarryForwardCount(),
                t.getAssignee().getEmail(), t.getAssignee().getName());
    }
}
