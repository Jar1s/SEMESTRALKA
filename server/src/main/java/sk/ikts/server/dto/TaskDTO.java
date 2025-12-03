package sk.ikts.server.dto;

import sk.ikts.server.model.Task;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Task
 */
public class TaskDTO {

    private Long taskId;
    private Long groupId;
    private Long createdBy;
    private String title;
    private String description;
    private Task.TaskStatus status;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private String reminders; // JSON array of hours before deadline

    // Constructors
    public TaskDTO() {
    }

    public TaskDTO(Long taskId, Long groupId, Long createdBy, String title, String description,
                   Task.TaskStatus status, LocalDateTime deadline, LocalDateTime createdAt, String reminders) {
        this.taskId = taskId;
        this.groupId = groupId;
        this.createdBy = createdBy;
        this.title = title;
        this.description = description;
        this.status = status;
        this.deadline = deadline;
        this.createdAt = createdAt;
        this.reminders = reminders;
    }

    // Getters and Setters
    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getReminders() {
        return reminders;
    }

    public void setReminders(String reminders) {
        this.reminders = reminders;
    }
}










