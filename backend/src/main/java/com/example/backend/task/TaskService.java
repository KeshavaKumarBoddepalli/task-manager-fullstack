package com.example.backend.task;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasks(TaskStatus status, String search) {
        String normalizedSearch = search == null ? "" : search.trim();
        boolean hasSearch = !normalizedSearch.isEmpty();

        List<Task> tasks;
        if (status != null && hasSearch) {
            tasks = taskRepository
                    .findAllByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
                            status, normalizedSearch);
        } else if (status != null) {
            tasks = taskRepository.findAllByStatusOrderByCreatedAtDesc(status);
        } else if (hasSearch) {
            tasks = taskRepository
                    .findAllByTitleContainingIgnoreCaseOrderByCreatedAtDesc(normalizedSearch);
        } else {
            tasks = taskRepository.findAllByOrderByCreatedAtDesc();
        }

        return tasks.stream().map(TaskResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long id) {
        return TaskResponse.from(findTask(id));
    }

    public TaskResponse createTask(TaskRequest request) {
        TaskStatus status = request.status() == null ? TaskStatus.PENDING : request.status();
        Task task = new Task(request.title().trim(), request.description(), status);
        return TaskResponse.from(taskRepository.save(task));
    }

    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = findTask(id);
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        return TaskResponse.from(taskRepository.save(task));
    }

    public TaskResponse completeTask(Long id) {
        Task task = findTask(id);
        task.setStatus(TaskStatus.COMPLETED);
        return TaskResponse.from(taskRepository.save(task));
    }

    public void deleteTask(Long id) {
        taskRepository.delete(findTask(id));
    }

    private Task findTask(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
    }
}
