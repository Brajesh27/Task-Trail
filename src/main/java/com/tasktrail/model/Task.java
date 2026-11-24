package com.tasktrail.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private LocalDate dueDate;

    // The due date the employer first set; dueDate moves, this never does
    private LocalDate originalDueDate;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;

    // How many times this task was moved forward because it wasn't finished
    private int carryForwardCount = 0;

    // Many tasks belong to one employee
    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private User assignee;

    // Many tasks are created by one employer
    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    // One task has many comments and history events.
    // cascade + orphanRemoval: deleting the task deletes them too.
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("happenedAt ASC")
    private List<TaskEvent> events = new ArrayList<>();

    protected Task() {}

    public Task(String title, String description, LocalDate dueDate, User assignee, User createdBy) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.originalDueDate = dueDate;
        this.assignee = assignee;
        this.createdBy = createdBy;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getOriginalDueDate() { return originalDueDate; }
    public TaskStatus getStatus() { return status; }
    public Priority getPriority() { return priority; }
    public int getCarryForwardCount() { return carryForwardCount; }
    public User getAssignee() { return assignee; }
    public User getCreatedBy() { return createdBy; }
    public List<Comment> getComments() { return comments; }
    public List<TaskEvent> getEvents() { return events; }

    public void setStatus(TaskStatus status) { this.status = status; }
    public void setPriority(Priority priority) { this.priority = priority; }

    /** Used by the employer's Edit form. A new due date also resets the carry-forward history. */
    public void update(String title, String description, LocalDate dueDate, User assignee) {
        this.title = title;
        this.description = description;
        this.assignee = assignee;
        if (!dueDate.equals(this.dueDate)) {
            this.dueDate = dueDate;
            this.originalDueDate = dueDate;
            this.carryForwardCount = 0;
        }
    }

    /** Still open, and either past its due date or already carried forward. */
    public boolean isLate(LocalDate today) {
        return status != TaskStatus.DONE && (dueDate.isBefore(today) || carryForwardCount > 0);
    }

    /** Moves an unfinished task to a new due date and counts the carry. */
    public void carryForwardTo(LocalDate newDueDate) {
        this.dueDate = newDueDate;
        this.carryForwardCount++;
    }

    public void addComment(User author, String text) {
        comments.add(new Comment(this, author, text));
    }

    public void addEvent(String actor, String description) {
        events.add(new TaskEvent(this, actor, description));
    }
}
