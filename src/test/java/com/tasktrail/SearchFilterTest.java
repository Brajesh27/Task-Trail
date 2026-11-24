package com.tasktrail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.tasktrail.model.Task;
import com.tasktrail.repository.TaskRepository;
import com.tasktrail.repository.UserRepository;

import java.util.List;

import com.tasktrail.dto.TaskFilter;
import com.tasktrail.model.Priority;
import com.tasktrail.model.TaskStatus;
import com.tasktrail.service.TaskService;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SearchFilterTest {

    @Autowired private MockMvc mvc;
    @Autowired private TaskService taskService;
    @Autowired private UserRepository users;
    @Autowired private TaskRepository tasks;

    private List<String> titles(TaskFilter filter, String email) {
        return taskService.search(filter, email).stream().map(Task::getTitle).toList();
    }

    @Test
    void employerSeesAllTasksMostUrgentFirst() {
        // Seed priorities: weekly report HIGH, login bug MEDIUM, client list LOW
        assertThat(titles(new TaskFilter(), "boss@tasktrail.com"))
                .containsExactly("Prepare weekly report", "Fix login bug", "Update client list");
    }

    @Test
    void employeeOnlySeesOwnTasks() {
        assertThat(titles(new TaskFilter(), "anu@tasktrail.com")).containsExactly("Update client list");
    }

    @Test
    void filtersCombine() {
        TaskFilter byText = new TaskFilter();
        byText.setQ("LOGGED OUT"); // searches the description too, ignoring case
        assertThat(titles(byText, "boss@tasktrail.com")).containsExactly("Fix login bug");

        TaskFilter byPriority = new TaskFilter();
        byPriority.setPriority(Priority.HIGH);
        assertThat(titles(byPriority, "boss@tasktrail.com")).containsExactly("Prepare weekly report");

        TaskFilter late = new TaskFilter();
        late.setLate(true); // the client list was carried forward at startup
        assertThat(titles(late, "boss@tasktrail.com")).containsExactly("Update client list");

        TaskFilter byPerson = new TaskFilter();
        byPerson.setAssigneeId(users.findByEmail("ravi@tasktrail.com").orElseThrow().getId());
        byPerson.setStatus(TaskStatus.DONE);
        assertThat(titles(byPerson, "boss@tasktrail.com")).isEmpty();
    }

    @Test
    @WithMockUser(username = "boss@tasktrail.com", roles = "EMPLOYER")
    void dashboardFiltersFromTheUrl() throws Exception {
        mvc.perform(get("/employer/dashboard").param("priority", "LOW"))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("Update client list")))
           .andExpect(content().string(not(containsString(">Fix login bug<"))))
           .andExpect(content().string(containsString("teamChart")));
    }

    @Test
    @WithMockUser(username = "ravi@tasktrail.com", roles = "EMPLOYEE")
    void employeeDashboardSearch() throws Exception {
        mvc.perform(get("/employee/dashboard").param("q", "report"))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("Prepare weekly report")))
           .andExpect(content().string(not(containsString(">Fix login bug<"))));
    }
}
