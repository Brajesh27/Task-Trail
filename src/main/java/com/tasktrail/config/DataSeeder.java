package com.tasktrail.config;

import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.tasktrail.model.Priority;
import com.tasktrail.model.Role;
import com.tasktrail.model.Task;
import com.tasktrail.model.User;
import com.tasktrail.repository.TaskRepository;
import com.tasktrail.repository.UserRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository users;
    private final TaskRepository tasks;
    private final PasswordEncoder encoder;

    public DataSeeder(UserRepository users, TaskRepository tasks, PasswordEncoder encoder) {
        this.users = users;
        this.tasks = tasks;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // With MySQL the data survives restarts, so only seed an empty database
        if (users.count() > 0) {
            System.out.println("Database already has data, skipping sample data");
            return;
        }

        User boss = users.save(new User("Priya Manager", "boss@tasktrail.com", encoder.encode("boss123"), Role.EMPLOYER));
        User ravi = users.save(new User("Ravi Kumar", "ravi@tasktrail.com", encoder.encode("ravi123"), Role.EMPLOYEE));
        User anu = users.save(new User("Anu Sharma", "anu@tasktrail.com", encoder.encode("anu123"), Role.EMPLOYEE));

        seedTask("Prepare weekly report", "Sales numbers for last week", LocalDate.now(), Priority.HIGH, ravi, boss);
        seedTask("Fix login bug", "Users get logged out early", LocalDate.now().plusDays(2), Priority.MEDIUM, ravi, boss);
        seedTask("Update client list", "Add the 5 new clients", LocalDate.now().minusDays(1), Priority.LOW, anu, boss);

        System.out.println("Seeded " + users.count() + " users and " + tasks.count() + " tasks");
    }

    private void seedTask(String title, String description, LocalDate due, Priority priority, User assignee, User boss) {
        Task task = new Task(title, description, due, assignee, boss);
        task.setPriority(priority);
        task.addEvent(boss.getName(), "Created and assigned to " + assignee.getName() + ", due " + due);
        tasks.save(task);
    }
}
