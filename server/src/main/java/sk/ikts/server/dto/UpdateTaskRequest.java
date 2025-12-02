package sk.ikts.server.dto;

import sk.ikts.server.model.Task;
import java.time.LocalDateTime;

/**
 * Request DTO for updating a task
 */
public class UpdateTaskRequest {

    private String title;
    private String description;
    private Task.TaskStatus status;
    private LocalDateTime deadline;

    // Constructors
    public UpdateTaskRequest() {
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Task.TaskStatus getStatus() {
        return status;
    }

    public void setStatus(Task.TaskStatus status) {
        this.status = status;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }
}

