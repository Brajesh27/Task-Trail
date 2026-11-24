package com.tasktrail.dto;

import com.tasktrail.model.Priority;
import com.tasktrail.model.TaskStatus;

/**
 * The search box and dropdowns above a task table. Every field is optional;
 * Spring fills it from the URL, e.g. /employer/dashboard?q=report&status=TODO
 */
public class TaskFilter {

    private String q;
    private Long assigneeId;
    private TaskStatus status;
    private Priority priority;
    // Still open and either past due or already carried forward at least once
    private boolean late;

    public boolean isActive() {
        return (q != null && !q.isBlank()) || assigneeId != null || status != null || priority != null || late;
    }

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }

    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public boolean isLate() { return late; }
    public void setLate(boolean late) { this.late = late; }
}
