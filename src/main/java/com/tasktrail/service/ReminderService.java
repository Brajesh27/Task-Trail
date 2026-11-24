package com.tasktrail.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tasktrail.model.Task;
import com.tasktrail.model.TaskStatus;
import com.tasktrail.model.User;
import com.tasktrail.repository.TaskRepository;

@Service
public class ReminderService {

    private static final Logger log = LoggerFactory.getLogger(ReminderService.class);

    /** One email: who it goes to and what it says. */
    public record Reminder(String to, String subject, String body) {}

    private final TaskRepository tasks;
    // ObjectProvider: Spring only creates a JavaMailSender when spring.mail.host is set,
    // so we ask for it "if available" instead of requiring it
    private final ObjectProvider<JavaMailSender> mailSender;
    private final boolean mailEnabled;
    private final String from;

    public ReminderService(TaskRepository tasks,
                           ObjectProvider<JavaMailSender> mailSender,
                           @Value("${tasktrail.mail.enabled:false}") boolean mailEnabled,
                           @Value("${tasktrail.mail.from:tasktrail@example.com}") String from) {
        this.tasks = tasks;
        this.mailSender = mailSender;
        this.mailEnabled = mailEnabled;
        this.from = from;
    }

    /** Builds one reminder per employee who has open tasks due on or before {@code today}. */
    @Transactional(readOnly = true)
    public List<Reminder> buildReminders(LocalDate today) {
        Map<User, List<Task>> dueByEmployee = tasks.findByStatusNotAndDueDateLessThanEqual(TaskStatus.DONE, today)
                .stream()
                .sorted(TaskService.BY_URGENCY)
                .collect(Collectors.groupingBy(Task::getAssignee));

        return dueByEmployee.entrySet().stream()
                .map(e -> toReminder(e.getKey(), e.getValue()))
                .toList();
    }

    private Reminder toReminder(User employee, List<Task> due) {
        StringBuilder body = new StringBuilder("Hi " + employee.getName() + ",\n\nThese tasks are due today:\n\n");
        for (Task t : due) {
            body.append("- ").append(t.getTitle()).append(" (").append(t.getPriority()).append(" priority");
            if (t.getCarryForwardCount() > 0) {
                body.append(", carried forward ").append(t.getCarryForwardCount()).append("x");
            }
            body.append(")\n");
        }
        body.append("\nLog in to TaskTrail to update them.\n");
        String subject = "TaskTrail: " + due.size() + " task(s) due today";
        return new Reminder(employee.getEmail(), subject, body.toString());
    }

    /** Sends today's reminders by email, or just logs them when mail is not set up. Returns how many. */
    public int sendReminders(LocalDate today) {
        List<Reminder> reminders = buildReminders(today);
        JavaMailSender sender = mailEnabled ? mailSender.getIfAvailable() : null;

        for (Reminder r : reminders) {
            if (sender == null) {
                log.info("Email (not sent, mail is off) to {}: {}\n{}", r.to(), r.subject(), r.body());
                continue;
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(r.to());
            message.setSubject(r.subject());
            message.setText(r.body());
            sender.send(message);
        }
        log.info("Prepared {} reminder email(s) for {}", reminders.size(), today);
        return reminders.size();
    }
}
