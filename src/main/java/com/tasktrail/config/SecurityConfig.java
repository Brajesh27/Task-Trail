package com.tasktrail.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableMethodSecurity // switches on @PreAuthorize in ApiTaskController
public class SecurityConfig {

    // Matches every REST API URL
    private static final RequestMatcher API = request -> request.getRequestURI().startsWith("/api/");

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/error", "/css/**", "/js/**", "/h2-console/**").permitAll()
                .requestMatchers("/employer/**").hasRole("EMPLOYER")
                .requestMatchers("/employee/**").hasRole("EMPLOYEE")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler((request, response, auth) -> {
                    boolean isEmployer = auth.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYER"));
                    boolean employerScreen = "employer".equals(request.getParameter("as"));

                    // Right password, wrong screen: log them straight back out
                    if (isEmployer != employerScreen) {
                        new SecurityContextLogoutHandler().logout(request, response, auth);
                        response.sendRedirect("/login?as=" + (employerScreen ? "employer" : "employee") + "&wrongRole");
                        return;
                    }
                    response.sendRedirect(isEmployer ? "/employer/dashboard" : "/employee/dashboard");
                })
                .failureHandler((request, response, ex) -> {
                    String as = "employer".equals(request.getParameter("as")) ? "employer" : "employee";
                    response.sendRedirect("/login?as=" + as + "&error");
                })
            )
            // The REST API logs in with a username and password on every request (HTTP Basic)
            .httpBasic(Customizer.withDefaults())
            // Not logged in: /api/** answers 401, every other page redirects to the login page
            .exceptionHandling(ex -> ex
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), API)
                .defaultAuthenticationEntryPointFor(new LoginUrlAuthenticationEntryPoint("/login"), new NegatedRequestMatcher(API)))
            .logout(logout -> logout.logoutSuccessUrl("/"))
            // The H2 console needs these two exceptions to work; API clients send no CSRF token
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/api/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}
