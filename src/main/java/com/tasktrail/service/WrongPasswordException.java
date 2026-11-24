package com.tasktrail.service;

public class WrongPasswordException extends RuntimeException {

    public WrongPasswordException() {
        super("Current password is wrong");
    }
}
