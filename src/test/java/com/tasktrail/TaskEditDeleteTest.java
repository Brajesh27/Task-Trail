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

import com.tasktrail.model.Priority;
import com.tasktrail.model.User;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WithMockUser(username = "boss@tasktrail.com", roles = "EMPLOYER")
class TaskEditDeleteTest {

    @Autowired private MockMvc mvc;
    @Autowired private UserRepository users;
    @Autowired private TaskRepository tasks;

    private Task task(String title) {
        return tasks.findAll().stream().filter(t -> t.getTitle().equals(title)).findFirst().orElseThrow();
    }

    @Test
    void editFormIsFilledIn() throws Exception {
        mvc.perform(get("/employer/tasks/" + task("Fix login bug").getId() + "/edit"))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("Edit task")))
           .andExpect(content().string(containsString("Users get logged out early")));
    }

    @Test
    void editSavesChangesAndRecordsHistory() throws Exception {
        Task t = task("Fix login bug");
        User anu = users.findByEmail("anu@tasktrail.com").orElseThrow();

        mvc.perform(post("/employer/tasks/" + t.getId()).with(csrf())
                .param("title", "Fix login bug today")
                .param("description", "Urgent")
                .param("dueDate", LocalDate.now().toString())
                .param("priority", "HIGH")
                .param("assigneeId", anu.getId().toString()))
           .andExpect(redirectedUrl("/employer/dashboard?updated"));

        mvc.perform(get("/tasks/" + t.getId()))
           .andExpect(content().string(containsString("Fix login bug today")))
           .andExpect(content().string(containsString("Anu Sharma")))
           .andExpect(content().string(containsString("Edited the task")));
        assertThat(tasks.findById(t.getId()).orElseThrow().getPriority()).isEqualTo(Priority.HIGH);
    }

    @Test
    void deleteRemovesTask() throws Exception {
        Task t = task("Fix login bug");
        mvc.perform(post("/employer/tasks/" + t.getId() + "/delete").with(csrf()))
           .andExpect(redirectedUrl("/employer/dashboard?deleted"));
        assertThat(tasks.findById(t.getId())).isEmpty();
    }

    @Test
    @WithMockUser(username = "ravi@tasktrail.com", roles = "EMPLOYEE")
    void employeeCannotEditOrDelete() throws Exception {
        Task t = task("Fix login bug");
        mvc.perform(get("/employer/tasks/" + t.getId() + "/edit")).andExpect(status().isForbidden());
        mvc.perform(post("/employer/tasks/" + t.getId() + "/delete").with(csrf())).andExpect(status().isForbidden());
        assertThat(tasks.findById(t.getId())).isPresent();
    }
}
