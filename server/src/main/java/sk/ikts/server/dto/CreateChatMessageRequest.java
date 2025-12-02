package sk.ikts.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a chat message
 */
public class CreateChatMessageRequest {

    @NotNull(message = "Group ID is required")
    private Long groupId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Message is required")
    private String message;

    // Constructors
    public CreateChatMessageRequest() {
    }

    public CreateChatMessageRequest(Long groupId, Long userId, String message) {
        this.groupId = groupId;
        this.userId = userId;
        this.message = message;
    }

    // Getters and Setters
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}




