package com.tasktrail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point. @SpringBootApplication tells Spring to scan this package (and sub-packages)
 * for classes like controllers, then start an embedded web server on port 8080.
 */
@SpringBootApplication
public class TaskTrailApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskTrailApplication.class, args);
    }
}
