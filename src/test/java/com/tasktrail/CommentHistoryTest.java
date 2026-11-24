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


@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CommentHistoryTest {

    @Autowired private MockMvc mvc;
    @Autowired private UserRepository users;
    @Autowired private TaskRepository tasks;

    private Task firstTaskOf(String email) {
        return tasks.findByAssignee(users.findByEmail(email).orElseThrow()).get(0);
    }

    @Test
    @WithMockUser(username = "ravi@tasktrail.com", roles = "EMPLOYEE")
    void employeeCommentsOnOwnTask() throws Exception {
        Task t = firstTaskOf("ravi@tasktrail.com");
        mvc.perform(post("/tasks/" + t.getId() + "/comments").with(csrf()).param("text", "Waiting on sales data"))
           .andExpect(redirectedUrl("/tasks/" + t.getId() + "#comments"));

        mvc.perform(get("/tasks/" + t.getId()))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("Waiting on sales data")))
           .andExpect(content().string(containsString("Ravi Kumar")));
    }

    @Test
    @WithMockUser(username = "ravi@tasktrail.com", roles = "EMPLOYEE")
    void employeeCannotSeeOrCommentOnOthersTasks() throws Exception {
        Task anus = firstTaskOf("anu@tasktrail.com");
        mvc.perform(get("/tasks/" + anus.getId())).andExpect(status().isForbidden());
        mvc.perform(post("/tasks/" + anus.getId() + "/comments").with(csrf()).param("text", "hi"))
           .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "ravi@tasktrail.com", roles = "EMPLOYEE")
    void blankCommentIsRejected() throws Exception {
        Task t = firstTaskOf("ravi@tasktrail.com");
        mvc.perform(post("/tasks/" + t.getId() + "/comments").with(csrf()).param("text", "   "))
           .andExpect(redirectedUrl("/tasks/" + t.getId() + "?commentError"));
    }

    @Test
    @WithMockUser(username = "ravi@tasktrail.com", roles = "EMPLOYEE")
    void statusChangeIsRecordedInHistory() throws Exception {
        Task t = firstTaskOf("ravi@tasktrail.com");
        mvc.perform(post("/employee/tasks/" + t.getId() + "/status").with(csrf()).param("status", "IN_PROGRESS"));

        mvc.perform(get("/tasks/" + t.getId()))
           .andExpect(content().string(containsString("Created and assigned to Ravi Kumar")))
           .andExpect(content().string(containsString("Changed status from TODO to IN_PROGRESS")));
    }

    @Test
    @WithMockUser(username = "boss@tasktrail.com", roles = "EMPLOYER")
    void employerSeesCarryForwardInHistory() throws Exception {
        Task anus = firstTaskOf("anu@tasktrail.com");
        mvc.perform(get("/tasks/" + anus.getId()))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("System")))
           .andExpect(content().string(containsString("carried forward to " + LocalDate.now())));
    }

    @Test
    @WithMockUser(username = "boss@tasktrail.com", roles = "EMPLOYER")
    void missingTaskIs404() throws Exception {
        mvc.perform(get("/tasks/9999")).andExpect(status().isNotFound());
    }
}
