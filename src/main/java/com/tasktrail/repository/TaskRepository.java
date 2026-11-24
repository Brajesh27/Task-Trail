package com.tasktrail.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tasktrail.model.Task;
import com.tasktrail.model.TaskStatus;
import com.tasktrail.model.User;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByAssignee(User assignee);

    // Unfinished tasks whose due date has passed
    List<Task> findByStatusNotAndDueDateBefore(TaskStatus status, LocalDate date);

    // Unfinished tasks due today or earlier (for reminder emails)
    List<Task> findByStatusNotAndDueDateLessThanEqual(TaskStatus status, LocalDate date);

    // Unfinished tasks that have been carried at least once, most-carried first
    List<Task> findByStatusNotAndCarryForwardCountGreaterThanOrderByCarryForwardCountDesc(TaskStatus status, int count);
}
