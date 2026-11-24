package com.tasktrail.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tasktrail.dto.EmployeeStats;
import com.tasktrail.dto.TeamSummary;
import com.tasktrail.model.Role;
import com.tasktrail.model.Task;
import com.tasktrail.model.TaskStatus;
import com.tasktrail.model.User;
import com.tasktrail.repository.TaskRepository;
import com.tasktrail.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final UserRepository users;
    private final TaskRepository tasks;

    public DashboardService(UserRepository users, TaskRepository tasks) {
        this.users = users;
        this.tasks = tasks;
    }

    public List<EmployeeStats> teamStats() {
        return users.findByRole(Role.EMPLOYEE).stream()
                .map(this::statsFor)
                .toList();
    }

    private EmployeeStats statsFor(User employee) {
        List<Task> mine = tasks.findByAssignee(employee);
        return new EmployeeStats(
                employee,
                mine.size(),
                count(mine, TaskStatus.TODO),
                count(mine, TaskStatus.IN_PROGRESS),
                count(mine, TaskStatus.DONE),
                mine.stream().filter(t -> t.getStatus() != TaskStatus.DONE && t.getCarryForwardCount() > 0).count());
    }

    private long count(List<Task> list, TaskStatus status) {
        return list.stream().filter(t -> t.getStatus() == status).count();
    }

    public TeamSummary summary() {
        List<Task> all = tasks.findAll();
        long done = count(all, TaskStatus.DONE);
        long carried = all.stream().filter(t -> t.getStatus() != TaskStatus.DONE && t.getCarryForwardCount() > 0).count();
        return new TeamSummary(all.size(), done, all.size() - done, carried);
    }

    public List<Task> needsAttention() {
        return tasks.findByStatusNotAndCarryForwardCountGreaterThanOrderByCarryForwardCountDesc(TaskStatus.DONE, 0);
    }
}
