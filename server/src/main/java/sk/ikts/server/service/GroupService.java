package sk.ikts.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.ikts.server.dto.CreateGroupRequest;
import sk.ikts.server.dto.GroupDTO;
import sk.ikts.server.dto.UserDTO;
import sk.ikts.server.model.Group;
import sk.ikts.server.model.Membership;
import sk.ikts.server.repository.GroupRepository;
import sk.ikts.server.repository.MembershipRepository;
import sk.ikts.server.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for group management operations
 * Handles group creation, retrieval, and membership management
 * Added by Cursor AI - service layer for group business logic
 */
@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Create a new group
     * @param request group creation request
     * @return GroupDTO of created group or null if user doesn't exist
     */
    public GroupDTO createGroup(CreateGroupRequest request) {
        try {
            // Verify user exists
            if (!userRepository.existsById(request.getCreatedBy())) {
                System.err.println("User not found: " + request.getCreatedBy());
                return null;
            }

            // Create group
            Group group = new Group(request.getName(), request.getDescription(), request.getCreatedBy());
            group = groupRepository.save(group);
            
            if (group.getGroupId() == null) {
                System.err.println("Failed to save group - groupId is null");
                return null;
            }

            // Create membership for creator as ADMIN
            Membership membership = new Membership(request.getCreatedBy(), group.getGroupId(), Membership.Role.ADMIN);
            membership = membershipRepository.save(membership);

            // Send notification about new group
            try {
                notificationService.notifyNewGroup(group.getGroupId(), group.getName());
            } catch (Exception e) {
                // Don't fail if notification fails
                System.err.println("Failed to send notification: " + e.getMessage());
            }

            return convertToDTO(group);
        } catch (Exception e) {
            System.err.println("Error in createGroup service: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to be handled by controller
        }
    }

    /**
     * Get all groups
     * @return List of all groups (no filtering - shows all groups regardless of creator)
     */
    public List<GroupDTO> getAllGroups() {
        List<Group> allGroups = groupRepository.findAll();
        System.out.println("Getting all groups - total count: " + allGroups.size());
        List<GroupDTO> result = allGroups.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        System.out.println("Returning " + result.size() + " groups");
        return result;
    }

    /**
     * Get group by ID
     * @param groupId group ID
     * @return GroupDTO or null if not found
     */
    public GroupDTO getGroupById(Long groupId) {
        Optional<Group> groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            return null;
        }
        return convertToDTO(groupOpt.get());
    }

    /**
     * Update group information
     * @param groupId group ID
     * @param request updated group data
     * @return GroupDTO or null if group doesn't exist
     */
    public GroupDTO updateGroup(Long groupId, CreateGroupRequest request) {
        Optional<Group> groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            return null;
        }

        Group group = groupOpt.get();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group = groupRepository.save(group);

        return convertToDTO(group);
    }

    /**
     * Delete group
     * @param groupId group ID
     * @return true if deleted, false if not found
     */
    public boolean deleteGroup(Long groupId) {
        if (!groupRepository.existsById(groupId)) {
            return false;
        }
        groupRepository.deleteById(groupId);
        return true;
    }

    /**
     * Get all groups for a user
     * @param userId user ID
     * @return List of groups the user is member of
     */
    public List<GroupDTO> getGroupsForUser(Long userId) {
        try {
            List<Membership> memberships = membershipRepository.findByUserId(userId);
            
            if (memberships == null || memberships.isEmpty()) {
                return new java.util.ArrayList<>();
            }
            
            List<Long> groupIds = memberships.stream()
                    .map(Membership::getGroupId)
                    .filter(id -> id != null)
                    .collect(Collectors.toList());

            if (groupIds.isEmpty()) {
                return new java.util.ArrayList<>();
            }

            return groupRepository.findAllById(groupIds).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error in getGroupsForUser: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Get all members of a group with their names
     * @param groupId group ID
     * @return List of UserDTOs representing group members
     */
    public List<UserDTO> getGroupMembers(Long groupId) {
        try {
            List<Membership> memberships = membershipRepository.findByGroupId(groupId);
            
            if (memberships == null || memberships.isEmpty()) {
                return new java.util.ArrayList<>();
            }
            
            List<Long> userIds = memberships.stream()
                    .map(Membership::getUserId)
                    .filter(id -> id != null)
                    .collect(Collectors.toList());

            if (userIds.isEmpty()) {
                return new java.util.ArrayList<>();
            }

            return userRepository.findAllById(userIds).stream()
                    .map(user -> new UserDTO(user.getUserId(), user.getEmail(), user.getName()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error in getGroupMembers: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Convert Group entity to DTO
     */
    private GroupDTO convertToDTO(Group group) {
        return new GroupDTO(
                group.getGroupId(),
                group.getName(),
                group.getDescription(),
                group.getCreatedBy(),
                group.getCreatedAt()
        );
    }
}

