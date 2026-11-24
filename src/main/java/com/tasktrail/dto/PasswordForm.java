package com.tasktrail.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** The "Change password" form. */
public class PasswordForm {

    @NotBlank(message = "Enter your current password")
    private String currentPassword;

    @Size(min = 6, max = 64, message = "New password must be 6 to 64 characters")
    private String newPassword;

    private String confirmPassword;

    // A boolean getter named isXxx can carry a validation rule that compares two fields
    @AssertTrue(message = "The two new passwords do not match")
    public boolean isConfirmed() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
