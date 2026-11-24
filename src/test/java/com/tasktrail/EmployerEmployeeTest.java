package com.tasktrail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.tasktrail.model.Role;
import com.tasktrail.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WithMockUser(username = "boss@tasktrail.com", roles = "EMPLOYER")
class EmployerEmployeeTest {

    @Autowired private MockMvc mvc;
    @Autowired private UserRepository users;

    @Test
    void newEmployeeIsSavedAndCanLogIn() throws Exception {
        mvc.perform(post("/employer/employees").with(csrf())
                .param("name", "Kiran Rao")
                .param("email", "Kiran@TaskTrail.com")
                .param("password", "kiran123"))
           .andExpect(redirectedUrl("/employer/dashboard?added"));

        var kiran = users.findByEmail("kiran@tasktrail.com").orElseThrow();
        assertThat(kiran.getRole()).isEqualTo(Role.EMPLOYEE);
        assertThat(kiran.getPassword()).startsWith("$2a$");

        mvc.perform(formLogin().user("kiran@tasktrail.com").password("kiran123"))
           .andExpect(authenticated());
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        long before = users.count();
        mvc.perform(post("/employer/employees").with(csrf())
                .param("name", "Fake Ravi")
                .param("email", "ravi@tasktrail.com")
                .param("password", "secret1"))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("That email is already in use")));
        assertThat(users.count()).isEqualTo(before);
    }

    @Test
    void badInputShowsErrors() throws Exception {
        mvc.perform(post("/employer/employees").with(csrf())
                .param("name", "")
                .param("email", "not-an-email")
                .param("password", "123"))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("Name is required")))
           .andExpect(content().string(containsString("Enter a valid email")))
           .andExpect(content().string(containsString("Password must be 6 to 64 characters")));
    }
}
