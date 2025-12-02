package sk.ikts.server.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for notifications
 * Used for WebSocket real-time notifications
 */
public class NotificationDTO {

    private String type;
    private String message;
    private Long groupId;
    private Long taskId;
    private Long userId;
    private LocalDateTime timestamp;

    // Constructors
    public NotificationDTO() {
        this.timestamp = LocalDateTime.now();
    }

    public NotificationDTO(String type, String message) {
        this.type = type;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public NotificationDTO(String type, String message, Long groupId) {
        this.type = type;
        this.message = message;
        this.groupId = groupId;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}










