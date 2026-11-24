package com.tasktrail.config;

import java.time.LocalDate;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tasktrail.service.CarryForwardService;

@Component
@EnableScheduling
public class CarryForwardJob {

    private final CarryForwardService carryForwardService;

    public CarryForwardJob(CarryForwardService carryForwardService) {
        this.carryForwardService = carryForwardService;
    }

    // Every day at 00:05 (second minute hour day month weekday)
    @Scheduled(cron = "${tasktrail.carry-forward.cron:0 5 0 * * *}")
    public void runDaily() {
        carryForwardService.carryForwardOverdueTasks(LocalDate.now());
    }

    // Also run once at startup, in case the app was off at midnight
    @EventListener(ApplicationReadyEvent.class)
    public void runOnStartup() {
        carryForwardService.carryForwardOverdueTasks(LocalDate.now());
    }
}
