package com.example.backend.task;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByOrderByCreatedAtDesc();

    List<Task> findAllByStatusOrderByCreatedAtDesc(TaskStatus status);

    List<Task> findAllByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title);

    List<Task> findAllByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
            TaskStatus status, String title);
}
