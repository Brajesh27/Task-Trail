package com.tasktrail.dto;

import com.tasktrail.model.TaskStatus;

import jakarta.validation.constraints.NotNull;

/** JSON body for PATCH /api/tasks/{id}/status, e.g. {"status": "DONE"} */
public record StatusUpdate(@NotNull TaskStatus status) {
}
