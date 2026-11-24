package com.tasktrail.service;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tasktrail.model.Task;
import com.tasktrail.model.TaskStatus;
import com.tasktrail.repository.TaskRepository;

@Service
public class CarryForwardService {

    private static final Logger log = LoggerFactory.getLogger(CarryForwardService.class);

    private final TaskRepository tasks;

    public CarryForwardService(TaskRepository tasks) {
        this.tasks = tasks;
    }

    /**
     * Moves every unfinished task that is past its due date to {@code today}.
     * Returns how many tasks were carried forward.
     */
    @Transactional
    public int carryForwardOverdueTasks(LocalDate today) {
        List<Task> overdue = tasks.findByStatusNotAndDueDateBefore(TaskStatus.DONE, today);
        for (Task task : overdue) {
            task.addEvent("System", "Not finished by " + task.getDueDate() + ", carried forward to " + today);
            task.carryForwardTo(today);
        }
        log.info("Carried forward {} unfinished task(s) to {}", overdue.size(), today);
        return overdue.size();
    }
}
