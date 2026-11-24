package com.tasktrail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
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

import com.tasktrail.model.User;
import com.tasktrail.repository.TaskRepository;
import com.tasktrail.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WithMockUser(username = "boss@tasktrail.com", roles = "EMPLOYER")
class EmployerTaskTest {

    @Autowired private MockMvc mvc;
    @Autowired private UserRepository users;
    @Autowired private TaskRepository tasks;

    @Test
    void formShowsEmployeesToChooseFrom() throws Exception {
        mvc.perform(get("/employer/tasks/new"))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("Anu Sharma")));
    }

    @Test
    void validFormCreatesTaskForEmployee() throws Exception {
        User anu = users.findByEmail("anu@tasktrail.com").orElseThrow();
        long before = tasks.count();

        mvc.perform(post("/employer/tasks").with(csrf())
                .param("title", "Call supplier")
                .param("description", "Ask about delivery")
                .param("dueDate", LocalDate.now().plusDays(1).toString())
                .param("assigneeId", anu.getId().toString()))
           .andExpect(redirectedUrl("/employer/dashboard?assigned"));

        assertThat(tasks.count()).isEqualTo(before + 1);
        assertThat(tasks.findByAssignee(anu)).extracting("title").contains("Call supplier");
    }

    @Test
    void missingTitleAndPastDateShowErrors() throws Exception {
        long before = tasks.count();

        mvc.perform(post("/employer/tasks").with(csrf())
                .param("title", "")
                .param("dueDate", LocalDate.now().minusDays(1).toString()))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("Title is required")))
           .andExpect(content().string(containsString("Due date cannot be in the past")))
           .andExpect(content().string(containsString("Pick an employee")));

        assertThat(tasks.count()).isEqualTo(before);
    }

    @Test
    @WithMockUser(username = "ravi@tasktrail.com", roles = "EMPLOYEE")
    void employeeCannotAssignTasks() throws Exception {
        mvc.perform(get("/employer/tasks/new")).andExpect(status().isForbidden());
    }
}
