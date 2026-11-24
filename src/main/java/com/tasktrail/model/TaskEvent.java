package com.tasktrail.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

/** One line of a task's history: who did what, and when. */
@Entity
public class TaskEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "task_id")
    private Task task;

    // A name, not a User link: "System" does the nightly carry-forward
    private String actor;

    private String description;

    private LocalDateTime happenedAt;

    protected TaskEvent() {}

    public TaskEvent(Task task, String actor, String description) {
        this.task = task;
        this.actor = actor;
        this.description = description;
        this.happenedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Task getTask() { return task; }
    public String getActor() { return actor; }
    public String getDescription() { return description; }
    public LocalDateTime getHappenedAt() { return happenedAt; }
}
