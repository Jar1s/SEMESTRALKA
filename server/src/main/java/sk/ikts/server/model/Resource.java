package sk.ikts.server.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a shared resource in a group
 * Resources can be files or URLs
 */
@Entity
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ResourceType type;

    @Column(name = "path_or_url", nullable = false)
    private String pathOrUrl;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    // Enum for resource types
    public enum ResourceType {
        FILE,
        URL
    }

    // Constructors
    public Resource() {
        this.uploadedAt = LocalDateTime.now();
    }

    public Resource(Long groupId, Long uploadedBy, String title, ResourceType type, String pathOrUrl) {
        this.groupId = groupId;
        this.uploadedBy = uploadedBy;
        this.title = title;
        this.type = type;
        this.pathOrUrl = pathOrUrl;
        this.uploadedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(Long uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    public String getPathOrUrl() {
        return pathOrUrl;
    }

    public void setPathOrUrl(String pathOrUrl) {
        this.pathOrUrl = pathOrUrl;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}

