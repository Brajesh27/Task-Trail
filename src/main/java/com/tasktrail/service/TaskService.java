package com.tasktrail.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tasktrail.dto.TaskFilter;
import com.tasktrail.dto.TaskForm;
import com.tasktrail.model.Role;
import com.tasktrail.model.Task;
import com.tasktrail.model.TaskStatus;
import com.tasktrail.model.User;
import com.tasktrail.repository.TaskRepository;
import com.tasktrail.repository.UserRepository;

@Service
public class TaskService {

    // Most urgent first: high priority, then the earliest due date
    public static final Comparator<Task> BY_URGENCY =
            Comparator.comparing(Task::getPriority).reversed().thenComparing(Task::getDueDate);

    private final TaskRepository tasks;
    private final UserRepository users;

    public TaskService(TaskRepository tasks, UserRepository users) {
        this.tasks = tasks;
        this.users = users;
    }

    @Transactional
    public Task assignTask(TaskForm form, String employerEmail) {
        User employer = user(employerEmail);
        User assignee = employee(form.getAssigneeId());

        Task task = new Task(form.getTitle(), form.getDescription(), form.getDueDate(), assignee, employer);
        task.setPriority(form.getPriority());
        task.addEvent(employer.getName(), "Created and assigned to " + assignee.getName() + ", due " + form.getDueDate());
        return tasks.save(task);
    }

    @Transactional
    public Task updateTask(Long taskId, TaskForm form, String employerEmail) {
        Task task = find(taskId);
        User assignee = employee(form.getAssigneeId());

        task.update(form.getTitle(), form.getDescription(), form.getDueDate(), assignee);
        task.setPriority(form.getPriority());
        task.addEvent(user(employerEmail).getName(), "Edited the task (due " + form.getDueDate() + ", "
                + form.getPriority() + " priority, assigned to " + assignee.getName() + ")");
        return task;
    }

    @Transactional
    public void deleteTask(Long taskId) {
        tasks.delete(find(taskId)); // comments and history go with it (cascade)
    }

    @Transactional
    public Task updateStatus(Long taskId, TaskStatus status, String employeeEmail) {
        Task task = find(taskId);

        // An employee may only change tasks assigned to them
        if (!task.getAssignee().getEmail().equals(employeeEmail)) {
            throw new AccessDeniedException("Task " + taskId + " is not assigned to you");
        }

        if (task.getStatus() != status) {
            task.addEvent(task.getAssignee().getName(), "Changed status from " + task.getStatus() + " to " + status);
            task.setStatus(status);
        }
        return task; // no save() needed: changes to a loaded entity are saved when the transaction ends
    }

    /** A task the given user may look at: employers see every task, employees only their own. */
    @Transactional(readOnly = true)
    public Task viewTask(Long taskId, String email) {
        Task task = find(taskId);
        User viewer = user(email);
        if (viewer.getRole() != Role.EMPLOYER && !task.getAssignee().equals(viewer)) {
            throw new AccessDeniedException("Task " + taskId + " is not assigned to you");
        }
        task.getComments().size(); // load the lists now, while the database session is open
        task.getEvents().size();
        return task;
    }

    @Transactional
    public void addComment(Long taskId, String text, String email) {
        Task task = viewTask(taskId, email); // same access rule as viewing
        task.addComment(user(email), text.trim());
    }

    /** Tasks the user may see that match the filter, most urgent first. */
    @Transactional(readOnly = true)
    public List<Task> search(TaskFilter filter, String email) {
        User viewer = user(email);
        List<Task> visible = viewer.getRole() == Role.EMPLOYER ? tasks.findAll() : tasks.findByAssignee(viewer);
        LocalDate today = LocalDate.now();
        String q = filter.getQ() == null ? "" : filter.getQ().trim().toLowerCase();

        return visible.stream()
                .filter(t -> q.isEmpty()
                        || t.getTitle().toLowerCase().contains(q)
                        || (t.getDescription() != null && t.getDescription().toLowerCase().contains(q)))
                .filter(t -> filter.getAssigneeId() == null || t.getAssignee().getId().equals(filter.getAssigneeId()))
                .filter(t -> filter.getStatus() == null || t.getStatus() == filter.getStatus())
                .filter(t -> filter.getPriority() == null || t.getPriority() == filter.getPriority())
                .filter(t -> !filter.isLate() || t.isLate(today))
                .sorted(BY_URGENCY)
                .toList();
    }

    private Task find(Long taskId) {
        return tasks.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    private User user(String email) {
        return users.findByEmail(email).orElseThrow();
    }

    private User employee(Long id) {
        return users.findById(id)
                .filter(u -> u.getRole() == Role.EMPLOYEE)
                .orElseThrow(() -> new IllegalArgumentException("Not an employee: " + id));
    }
}
