package com.tasktrail;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import com.tasktrail.service.ReminderService;
import com.tasktrail.service.ReminderService.Reminder;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReminderTest {

    @Autowired private ReminderService reminders;

    @Test
    void oneReminderPerEmployeeWithTasksDueToday() {
        // Ravi's weekly report is due today; Anu's client list was carried forward to today
        List<Reminder> list = reminders.buildReminders(LocalDate.now());

        assertThat(list).extracting(Reminder::to)
                .containsExactlyInAnyOrder("ravi@tasktrail.com", "anu@tasktrail.com");

        Reminder ravi = list.stream().filter(r -> r.to().startsWith("ravi")).findFirst().orElseThrow();
        assertThat(ravi.subject()).isEqualTo("TaskTrail: 1 task(s) due today");
        assertThat(ravi.body()).contains("Prepare weekly report").doesNotContain("Fix login bug");

        Reminder anu = list.stream().filter(r -> r.to().startsWith("anu")).findFirst().orElseThrow();
        assertThat(anu.body()).contains("carried forward 1x");
    }

    @Test
    void withMailOffRemindersAreOnlyLogged() {
        assertThat(reminders.sendReminders(LocalDate.now())).isEqualTo(2);
    }
}
