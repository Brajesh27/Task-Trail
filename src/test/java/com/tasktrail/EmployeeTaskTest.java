package com.tasktrail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.tasktrail.model.Task;
import com.tasktrail.model.TaskStatus;
import com.tasktrail.repository.TaskRepository;
import com.tasktrail.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EmployeeTaskTest {

    @Autowired private MockMvc mvc;
    @Autowired private UserRepository users;
    @Autowired private TaskRepository tasks;

    private Task firstTaskOf(String email) {
        return tasks.findByAssignee(users.findByEmail(email).orElseThrow()).get(0);
    }

    @Test
    @WithMockUser(username = "ravi@tasktrail.com", roles = "EMPLOYEE")
    void employeeCanUpdateOwnTask() throws Exception {
        Task task = firstTaskOf("ravi@tasktrail.com");

        mvc.perform(post("/employee/tasks/" + task.getId() + "/status").with(csrf()).param("status", "DONE"))
           .andExpect(redirectedUrl("/employee/dashboard?updated"));

        assertThat(tasks.findById(task.getId()).orElseThrow().getStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    @WithMockUser(username = "ravi@tasktrail.com", roles = "EMPLOYEE")
    void employeeCannotUpdateSomeoneElsesTask() throws Exception {
        Task anusTask = firstTaskOf("anu@tasktrail.com");

        mvc.perform(post("/employee/tasks/" + anusTask.getId() + "/status").with(csrf()).param("status", "DONE"))
           .andExpect(status().isForbidden());

        assertThat(tasks.findById(anusTask.getId()).orElseThrow().getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    @WithMockUser(username = "boss@tasktrail.com", roles = "EMPLOYER")
    void employerCannotUseEmployeeStatusUrl() throws Exception {
        Task task = firstTaskOf("ravi@tasktrail.com");
        mvc.perform(post("/employee/tasks/" + task.getId() + "/status").with(csrf()).param("status", "DONE"))
           .andExpect(status().isForbidden());
    }

    @Test
    void loginIgnoresEmailCapitals() throws Exception {
        mvc.perform(formLogin().user("Ravi@TaskTrail.com").password("ravi123")).andExpect(authenticated());
    }
}
