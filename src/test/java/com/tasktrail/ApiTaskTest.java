package com.tasktrail;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.tasktrail.repository.TaskRepository;
import com.tasktrail.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ApiTaskTest {

    @Autowired private MockMvc mvc;
    @Autowired private UserRepository users;
    @Autowired private TaskRepository tasks;

    private Long firstTaskOf(String email) {
        return tasks.findByAssignee(users.findByEmail(email).orElseThrow()).get(0).getId();
    }

    @Test
    void notLoggedInGets401() throws Exception {
        mvc.perform(get("/api/tasks")).andExpect(status().isUnauthorized());
    }

    @Test
    void employerListsAllTasksAsJson() throws Exception {
        mvc.perform(get("/api/tasks").with(httpBasic("boss@tasktrail.com", "boss123")))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$", hasSize(3)))
           .andExpect(jsonPath("$[0].title").value("Prepare weekly report"))
           .andExpect(jsonPath("$[0].priority").value("HIGH"))
           .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void employeeListsOnlyOwnTasks() throws Exception {
        mvc.perform(get("/api/tasks").param("status", "TODO").with(httpBasic("anu@tasktrail.com", "anu123")))
           .andExpect(jsonPath("$", hasSize(1)))
           .andExpect(jsonPath("$[0].assigneeEmail").value("anu@tasktrail.com"));
    }

    @Test
    void employerCreatesTask() throws Exception {
        Long anuId = users.findByEmail("anu@tasktrail.com").orElseThrow().getId();
        String json = """
                {"title": "Book meeting room", "dueDate": "%s", "priority": "LOW", "assigneeId": %d}
                """.formatted(LocalDate.now().plusDays(1), anuId);

        mvc.perform(post("/api/tasks").with(httpBasic("boss@tasktrail.com", "boss123"))
                .contentType(MediaType.APPLICATION_JSON).content(json))
           .andExpect(status().isCreated())
           .andExpect(header().exists("Location"))
           .andExpect(jsonPath("$.title").value("Book meeting room"))
           .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void invalidTaskGets400() throws Exception {
        mvc.perform(post("/api/tasks").with(httpBasic("boss@tasktrail.com", "boss123"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"title\": \"\"}"))
           .andExpect(status().isBadRequest());
    }

    @Test
    void employeeCannotCreateOrDelete() throws Exception {
        Long raviId = users.findByEmail("ravi@tasktrail.com").orElseThrow().getId();
        String json = """
                {"title": "Give myself a task", "dueDate": "%s", "priority": "LOW", "assigneeId": %d}
                """.formatted(LocalDate.now().plusDays(1), raviId);
        mvc.perform(post("/api/tasks").with(httpBasic("ravi@tasktrail.com", "ravi123"))
                .contentType(MediaType.APPLICATION_JSON).content(json))
           .andExpect(status().isForbidden());
        mvc.perform(delete("/api/tasks/" + firstTaskOf("ravi@tasktrail.com")).with(httpBasic("ravi@tasktrail.com", "ravi123")))
           .andExpect(status().isForbidden());
    }

    @Test
    void employeeUpdatesOwnStatusButNotOthers() throws Exception {
        mvc.perform(patch("/api/tasks/" + firstTaskOf("ravi@tasktrail.com") + "/status")
                .with(httpBasic("ravi@tasktrail.com", "ravi123"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"DONE\"}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.status").value("DONE"));

        mvc.perform(patch("/api/tasks/" + firstTaskOf("anu@tasktrail.com") + "/status")
                .with(httpBasic("ravi@tasktrail.com", "ravi123"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"DONE\"}"))
           .andExpect(status().isForbidden());
    }

    @Test
    void employerDeletesAndMissingTaskIs404() throws Exception {
        Long id = firstTaskOf("ravi@tasktrail.com");
        mvc.perform(delete("/api/tasks/" + id).with(httpBasic("boss@tasktrail.com", "boss123")))
           .andExpect(status().isNoContent());
        mvc.perform(get("/api/tasks/" + id).with(httpBasic("boss@tasktrail.com", "boss123")))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.error").value("No task with id " + id));
    }
}
