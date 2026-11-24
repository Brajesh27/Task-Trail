package com.tasktrail;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityRulesTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void homeIsPublic() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
    }

    @Test
    void employerPagesNeedLogin() throws Exception {
        mvc.perform(get("/employer/dashboard")).andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void employeeCannotOpenEmployerPages() throws Exception {
        mvc.perform(get("/employer/dashboard")).andExpect(status().isForbidden());
    }

    @Test
    void seededUserCanLogInWithRightPassword() throws Exception {
        mvc.perform(post("/login").with(csrf()).param("username", "boss@tasktrail.com").param("password", "boss123").param("as", "employer")).andExpect(authenticated());
        mvc.perform(formLogin().user("boss@tasktrail.com").password("wrong")).andExpect(unauthenticated());
    }

    @Test
    void eachRoleLandsOnItsOwnDashboard() throws Exception {
        mvc.perform(post("/login").with(csrf()).param("username", "boss@tasktrail.com").param("password", "boss123").param("as", "employer"))
           .andExpect(redirectedUrl("/employer/dashboard"));
        mvc.perform(post("/login").with(csrf()).param("username", "ravi@tasktrail.com").param("password", "ravi123").param("as", "employee"))
           .andExpect(redirectedUrl("/employee/dashboard"));
    }

    @Test
    void employeeOnEmployerScreenIsRejectedAndLoggedOut() throws Exception {
        mvc.perform(post("/login").with(csrf()).param("username", "ravi@tasktrail.com").param("password", "ravi123").param("as", "employer"))
           .andExpect(redirectedUrl("/login?as=employer&wrongRole"))
           .andExpect(unauthenticated());
    }

    @Test
    void employerOnEmployeeScreenIsRejectedAndLoggedOut() throws Exception {
        mvc.perform(post("/login").with(csrf()).param("username", "boss@tasktrail.com").param("password", "boss123").param("as", "employee"))
           .andExpect(redirectedUrl("/login?as=employee&wrongRole"))
           .andExpect(unauthenticated());
    }

    @Test
    void wrongPasswordGoesBackToTheSameLoginScreen() throws Exception {
        mvc.perform(formLogin().user("boss@tasktrail.com").password("nope"))
           .andExpect(redirectedUrl("/login?as=employee&error"));
    }

    @Test
    @WithMockUser(username = "ravi@tasktrail.com", roles = "EMPLOYEE")
    void employeeSeesOnlyTheirOwnTasks() throws Exception {
        mvc.perform(get("/employee/dashboard"))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("Prepare weekly report")))
           .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Update client list"))));
    }

    @Test
    @WithMockUser(username = "boss@tasktrail.com", roles = "EMPLOYER")
    void employerSeesAllTasks() throws Exception {
        mvc.perform(get("/employer/dashboard"))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString("Update client list")))
           .andExpect(content().string(containsString("Ravi Kumar")));
    }
}
