package com.tasktrail;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import com.tasktrail.model.Task;
import com.tasktrail.model.TaskStatus;
import com.tasktrail.repository.TaskRepository;
import com.tasktrail.repository.UserRepository;
import com.tasktrail.service.CarryForwardService;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CarryForwardTest {

    @Autowired private CarryForwardService service;
    @Autowired private TaskRepository tasks;
    @Autowired private UserRepository users;

    private Task taskTitled(String title) {
        return tasks.findAll().stream().filter(t -> t.getTitle().equals(title)).findFirst().orElseThrow();
    }

    @Test
    void overdueSeedTaskWasCarriedAtStartup() {
        Task task = taskTitled("Update client list"); // seeded as due yesterday
        assertThat(task.getDueDate()).isEqualTo(LocalDate.now());
        assertThat(task.getOriginalDueDate()).isEqualTo(LocalDate.now().minusDays(1));
        assertThat(task.getCarryForwardCount()).isEqualTo(1);
    }

    @Test
    void unfinishedTasksMoveAndDoneTasksStay() {
        Task report = taskTitled("Prepare weekly report"); // due today
        report.setStatus(TaskStatus.DONE);
        tasks.save(report);

        LocalDate threeDaysLater = LocalDate.now().plusDays(3);
        int moved = service.carryForwardOverdueTasks(threeDaysLater);

        // "Fix login bug" (due in 2 days) and "Update client list" (due today) are overdue by then
        assertThat(moved).isEqualTo(2);
        assertThat(taskTitled("Fix login bug").getDueDate()).isEqualTo(threeDaysLater);
        assertThat(taskTitled("Update client list").getCarryForwardCount()).isEqualTo(2);

        Task doneTask = taskTitled("Prepare weekly report");
        assertThat(doneTask.getDueDate()).isEqualTo(LocalDate.now());
        assertThat(doneTask.getCarryForwardCount()).isZero();
    }

    @Test
    void runningTwiceOnSameDayDoesNothingTheSecondTime() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        service.carryForwardOverdueTasks(tomorrow);
        assertThat(service.carryForwardOverdueTasks(tomorrow)).isZero();
    }
}
