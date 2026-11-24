package com.tasktrail.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tasktrail.dto.EmployeeForm;
import com.tasktrail.dto.PasswordForm;
import com.tasktrail.model.Role;
import com.tasktrail.model.User;
import com.tasktrail.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional
    public User createEmployee(EmployeeForm form) {
        String email = form.getEmail().trim().toLowerCase();
        if (users.findByEmail(email).isPresent()) {
            throw new EmailAlreadyUsedException(email);
        }
        User employee = new User(form.getName().trim(), email, encoder.encode(form.getPassword()), Role.EMPLOYEE);
        return users.save(employee);
    }

    @Transactional
    public void changePassword(String email, PasswordForm form) {
        User user = users.findByEmail(email).orElseThrow();
        // matches() hashes the typed password the same way and compares; we can never "decrypt" the stored one
        if (!encoder.matches(form.getCurrentPassword(), user.getPassword())) {
            throw new WrongPasswordException();
        }
        user.setPassword(encoder.encode(form.getNewPassword()));
    }
}
