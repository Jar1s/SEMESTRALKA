package sk.ikts.server.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a task in a study group
 * Tasks have status: OPEN, IN_PROGRESS, or DONE
 */
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Enum for task status
    public enum TaskStatus {
        OPEN,
        IN_PROGRESS,
        DONE
    }

    // Constructors
    public Task() {
        this.status = TaskStatus.OPEN;
        this.createdAt = LocalDateTime.now();
    }

    public Task(Long groupId, Long createdBy, String title, String description) {
        this.groupId = groupId;
        this.createdBy = createdBy;
        this.title = title;
        this.description = description;
        this.status = TaskStatus.OPEN;
        this.createdAt = LocalDateTime.now();
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

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
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
}

