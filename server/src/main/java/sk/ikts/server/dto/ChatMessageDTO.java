package sk.ikts.server.dto;

import java.time.LocalDateTime;

/**
 * DTO for chat messages
 */
public class ChatMessageDTO {
    private Long messageId;
    private Long groupId;
    private Long userId;
    private String userName;
    private String message;
    private LocalDateTime sentAt;

    public ChatMessageDTO() {
    }

    public ChatMessageDTO(Long messageId, Long groupId, Long userId, String userName, 
                         String message, LocalDateTime sentAt) {
        this.messageId = messageId;
        this.groupId = groupId;
        this.userId = userId;
        this.userName = userName;
        this.message = message;
        this.sentAt = sentAt;
    }

    // Getters and Setters
    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}

