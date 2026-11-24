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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WithMockUser(username = "ravi@tasktrail.com", roles = "EMPLOYEE")
class PasswordChangeTest {

    @Autowired private MockMvc mvc;

    @Test
    void changedPasswordWorksForLogin() throws Exception {
        mvc.perform(post("/account/password").with(csrf())
                .param("currentPassword", "ravi123")
                .param("newPassword", "newpass1")
                .param("confirmPassword", "newpass1"))
           .andExpect(redirectedUrl("/employee/dashboard?passwordChanged"));

        mvc.perform(formLogin().user("ravi@tasktrail.com").password("newpass1"))
           .andExpect(redirectedUrl("/employee/dashboard"));
        mvc.perform(formLogin().user("ravi@tasktrail.com").password("ravi123"))
           .andExpect(redirectedUrl("/login?as=employee&error"));
    }

    @Test
    void wrongCurrentPasswordIsRejected() throws Exception {
        mvc.perform(post("/account/password").with(csrf())
                .param("currentPassword", "nope")
                .param("newPassword", "newpass1")
                .param("confirmPassword", "newpass1"))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("Current password is wrong")));
    }

    @Test
    void newPasswordsMustMatch() throws Exception {
        mvc.perform(post("/account/password").with(csrf())
                .param("currentPassword", "ravi123")
                .param("newPassword", "newpass1")
                .param("confirmPassword", "newpass2"))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("The two new passwords do not match")));
    }
}
