package com.tasktrail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.tasktrail.dto.EmployeeStats;
import com.tasktrail.dto.TeamSummary;
import com.tasktrail.model.Task;
import com.tasktrail.model.TaskStatus;
import com.tasktrail.repository.TaskRepository;
import com.tasktrail.service.DashboardService;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DashboardStatsTest {

    @Autowired private DashboardService dashboard;
    @Autowired private TaskRepository tasks;
    @Autowired private MockMvc mvc;

    private EmployeeStats statsFor(String email) {
        return dashboard.teamStats().stream()
                .filter(s -> s.employee().getEmail().equals(email)).findFirst().orElseThrow();
    }

    @Test
    void statsReflectSeedData() {
        // Ravi has 2 open tasks; Anu has 1, carried forward at startup
        assertThat(statsFor("ravi@tasktrail.com").total()).isEqualTo(2);
        assertThat(statsFor("anu@tasktrail.com").carriedOpen()).isEqualTo(1);

        TeamSummary summary = dashboard.summary();
        assertThat(summary.total()).isEqualTo(3);
        assertThat(summary.pending()).isEqualTo(3);
        assertThat(summary.carriedOpen()).isEqualTo(1);

        assertThat(dashboard.needsAttention()).extracting(Task::getTitle).containsExactly("Update client list");
    }

    @Test
    void completingATaskUpdatesTheNumbers() {
        Task raviTask = tasks.findAll().stream()
                .filter(t -> t.getTitle().equals("Prepare weekly report")).findFirst().orElseThrow();
        raviTask.setStatus(TaskStatus.DONE);
        tasks.save(raviTask);

        EmployeeStats ravi = statsFor("ravi@tasktrail.com");
        assertThat(ravi.done()).isEqualTo(1);
        assertThat(ravi.completionPercent()).isEqualTo(50);
        assertThat(dashboard.summary().done()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "boss@tasktrail.com", roles = "EMPLOYER")
    void dashboardShowsStats() throws Exception {
        mvc.perform(get("/employer/dashboard"))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("Needs attention")))
           .andExpect(content().string(containsString("carried 1x")))
           .andExpect(content().string(containsString("0%")));
    }
}
