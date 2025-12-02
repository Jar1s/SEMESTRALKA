package sk.ikts.server.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Group
 * Added by Cursor AI - DTO for group data transfer
 */
public class GroupDTO {

    private Long groupId;
    private String name;
    private String description;
    private Long createdBy;
    private String ownerName; // Name of the group owner
    private LocalDateTime createdAt;

    // Constructors
    public GroupDTO() {
    }

    public GroupDTO(Long groupId, String name, String description, Long createdBy, LocalDateTime createdAt) {
        this.groupId = groupId;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public GroupDTO(Long groupId, String name, String description, Long createdBy, String ownerName, LocalDateTime createdAt) {
        this.groupId = groupId;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.ownerName = ownerName;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}










