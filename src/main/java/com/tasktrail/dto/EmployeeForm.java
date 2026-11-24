package com.tasktrail.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Holds what the employer typed into the "Add employee" form. */
public class EmployeeForm {

    @NotBlank(message = "Name is required")
    @Size(max = 80, message = "Name must be 80 characters or less")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;

    @Size(min = 6, max = 64, message = "Password must be 6 to 64 characters")
    private String password;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
