package com.tasktrail.dto;

import com.tasktrail.model.User;

/** One row of the team table: how one employee is doing. */
public record EmployeeStats(User employee, long total, long todo, long inProgress, long done, long carriedOpen) {

    public int completionPercent() {
        return total == 0 ? 0 : (int) Math.round(done * 100.0 / total);
    }
}
