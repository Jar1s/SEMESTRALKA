package sk.ikts.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sk.ikts.server.model.Resource;
import sk.ikts.server.repository.ResourceRepository;
import sk.ikts.server.service.ActivityLogService;
import sk.ikts.server.service.NotificationService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for resource operations
 * Handles file uploads and URL sharing
 */
@RestController
@RequestMapping("/api/resources")
@CrossOrigin(origins = "*")
public class ResourceController {

    @Autowired
    private ResourceRepository resourceRepository;
    
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ActivityLogService activityLogService;

    private static final String UPLOAD_DIR = "uploads/";

    @PostConstruct
    public void init() {
        // Create upload directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create upload directory: " + e.getMessage());
        }
    }

    /**
     * Get resources for a group
     * GET /api/resources/group/{groupId}
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Resource>> getResourcesByGroup(@PathVariable("groupId") Long groupId) {
        List<Resource> resources = resourceRepository.findByGroupId(groupId);
        return ResponseEntity.ok(resources);
    }

    /**
     * Upload a file
     * POST /api/resources/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("groupId") Long groupId,
            @RequestParam("uploadedBy") Long uploadedBy,
            @RequestParam("title") String title) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(UPLOAD_DIR + filename);

            // Save file
            Files.write(filePath, file.getBytes());

            // Create resource record
            Resource resource = new Resource();
            resource.setGroupId(groupId);
            resource.setUploadedBy(uploadedBy);
            resource.setTitle(title);
            resource.setType(Resource.ResourceType.FILE);
            resource.setPathOrUrl(filename);
            resource.setUploadedAt(LocalDateTime.now());

            resource = resourceRepository.save(resource);
            
            // Log activity
            activityLogService.logActivity(uploadedBy, "UPLOAD_RESOURCE", 
                    "Uploaded file: " + title + " (ID: " + resource.getResourceId() + ") to group " + groupId);
            
            // Send notification
            try {
                notificationService.notifyGroup(groupId, 
                    new sk.ikts.server.dto.NotificationDTO(
                        "NEW_RESOURCE",
                        "New file uploaded: " + title,
                        groupId
                    ));
            } catch (Exception e) {
                // Don't fail if notification fails
            }
            
            return ResponseEntity.ok(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload file: " + e.getMessage());
        }
    }

    /**
     * Share a URL
     * POST /api/resources/url
     */
    @PostMapping("/url")
    public ResponseEntity<?> shareUrl(@RequestBody Map<String, Object> request) {
        try {
            Long groupId = Long.valueOf(request.get("groupId").toString());
            Long uploadedBy = Long.valueOf(request.get("uploadedBy").toString());
            String title = request.get("title").toString();
            String url = request.get("url").toString();

            Resource resource = new Resource();
            resource.setGroupId(groupId);
            resource.setUploadedBy(uploadedBy);
            resource.setTitle(title);
            resource.setType(Resource.ResourceType.URL);
            resource.setPathOrUrl(url);
            resource.setUploadedAt(LocalDateTime.now());

            resource = resourceRepository.save(resource);
            
            // Log activity
            activityLogService.logActivity(uploadedBy, "SHARE_URL", 
                    "Shared URL: " + title + " (ID: " + resource.getResourceId() + ") in group " + groupId);
            
            // Send notification
            try {
                notificationService.notifyGroup(groupId, 
                    new sk.ikts.server.dto.NotificationDTO(
                        "NEW_RESOURCE",
                        "New URL shared: " + title,
                        groupId
                    ));
            } catch (Exception e) {
                // Don't fail if notification fails
            }
            
            return ResponseEntity.ok(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to share URL: " + e.getMessage());
        }
    }

    /**
     * Download a file
     * GET /api/resources/{id}/download
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadFile(@PathVariable("id") Long id) {
        Resource resource = resourceRepository.findById(id).orElse(null);
        if (resource == null || resource.getType() != Resource.ResourceType.FILE) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path filePath = Paths.get(UPLOAD_DIR + resource.getPathOrUrl());
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent = Files.readAllBytes(filePath);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + resource.getTitle() + "\"")
                    .header("Content-Type", "application/octet-stream")
                    .body(fileContent);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to download file: " + e.getMessage());
        }
    }

    /**
     * Delete a resource
     * DELETE /api/resources/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteResource(@PathVariable("id") Long id) {
        Resource resource = resourceRepository.findById(id).orElse(null);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        // Delete file if it's a file resource
        if (resource.getType() == Resource.ResourceType.FILE) {
            try {
                Path filePath = Paths.get(UPLOAD_DIR + resource.getPathOrUrl());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("Failed to delete file: " + e.getMessage());
            }
        }

        // Log activity before deletion
        activityLogService.logActivity(resource.getUploadedBy(), "DELETE_RESOURCE", 
                "Deleted resource: " + resource.getTitle() + " (ID: " + id + ")");
        
        resourceRepository.delete(resource);
        return ResponseEntity.noContent().build();
    }
}

