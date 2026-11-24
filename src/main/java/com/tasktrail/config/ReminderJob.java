package com.tasktrail.config;

import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tasktrail.service.ReminderService;

@Component
public class ReminderJob {

    private final ReminderService reminderService;

    public ReminderJob(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    // Every day at 08:00, after the 00:05 carry-forward has run.
    // Scheduling is already switched on by @EnableScheduling in CarryForwardJob.
    @Scheduled(cron = "${tasktrail.reminders.cron:0 0 8 * * *}")
    public void runDaily() {
        reminderService.sendReminders(LocalDate.now());
    }
}
