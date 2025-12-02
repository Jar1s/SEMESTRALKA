package sk.ikts.server.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.ikts.server.dto.CreateGroupRequest;
import sk.ikts.server.dto.GroupDTO;
import sk.ikts.server.dto.UserDTO;
import sk.ikts.server.service.GroupService;

import java.util.List;

/**
 * REST Controller for group operations
 * Handles group CRUD operations
 * Added by Cursor AI - REST API controller for group operations
 */
@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "*")
public class GroupController {

    @Autowired
    private GroupService groupService;

    /**
     * Get all groups - returns ALL groups regardless of creator
     * GET /api/groups
     */
    @GetMapping
    public ResponseEntity<List<GroupDTO>> getAllGroups() {
        List<GroupDTO> groups = groupService.getAllGroups();
        System.out.println("GET /api/groups - returning " + groups.size() + " groups");
        return ResponseEntity.ok(groups);
    }

    /**
     * Create a new group
     * POST /api/groups
     */
    @PostMapping
    public ResponseEntity<?> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        try {
            GroupDTO group = groupService.createGroup(request);
            
            if (group == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("User not found");
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(group);
        } catch (Exception e) {
            System.err.println("Error creating group: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating group: " + e.getMessage());
        }
    }

    /**
     * Get group by ID
     * GET /api/groups/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<GroupDTO> getGroup(@PathVariable("id") Long id) {
        GroupDTO group = groupService.getGroupById(id);
        
        if (group == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(group);
    }

    /**
     * Update group
     * PUT /api/groups/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGroup(@PathVariable("id") Long id, @Valid @RequestBody CreateGroupRequest request) {
        GroupDTO group = groupService.updateGroup(id, request);
        
        if (group == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(group);
    }

    /**
     * Delete group
     * DELETE /api/groups/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable("id") Long id) {
        boolean deleted = groupService.deleteGroup(id);
        
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Get groups for a user
     * GET /api/groups/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GroupDTO>> getGroupsForUser(@PathVariable("userId") Long userId) {
        try {
            List<GroupDTO> groups = groupService.getGroupsForUser(userId);
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            System.err.println("Error in getGroupsForUser controller: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new java.util.ArrayList<>());
        }
    }

    /**
     * Get all members of a group
     * GET /api/groups/{groupId}/members
     */
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<UserDTO>> getGroupMembers(@PathVariable("groupId") Long groupId) {
        try {
            List<UserDTO> members = groupService.getGroupMembers(groupId);
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            System.err.println("Error in getGroupMembers controller: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new java.util.ArrayList<>());
        }
    }
}

