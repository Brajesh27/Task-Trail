package com.tasktrail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tasktrail.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
