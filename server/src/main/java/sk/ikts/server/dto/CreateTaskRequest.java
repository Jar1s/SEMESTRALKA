package sk.ikts.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Request DTO for creating a task
 */
public class CreateTaskRequest {

    @NotNull(message = "Group ID is required")
    private Long groupId;

    @NotNull(message = "Created by user ID is required")
    private Long createdBy;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private LocalDateTime deadline;
    
    private String reminders; // JSON array of hours before deadline, e.g., "[24, 6, 1]"

    // Constructors
    public CreateTaskRequest() {
    }

    public CreateTaskRequest(Long groupId, Long createdBy, String title, String description, LocalDateTime deadline) {
        this.groupId = groupId;
        this.createdBy = createdBy;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
    }

    // Getters and Setters
    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

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

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public String getReminders() {
        return reminders;
    }

    public void setReminders(String reminders) {
        this.reminders = reminders;
    }
}










