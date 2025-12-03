package sk.ikts.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.ikts.server.dto.CreateGroupRequest;
import sk.ikts.server.dto.GroupDTO;
import sk.ikts.server.model.Group;
import sk.ikts.server.model.Membership;
import sk.ikts.server.model.User;
import sk.ikts.server.repository.GroupRepository;
import sk.ikts.server.repository.MembershipRepository;
import sk.ikts.server.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit testy pre GroupService
 * Testuje vytváranie skupín, pridávanie členov a správu skupín
 */
@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private ActivityLogService activityLogService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private GroupService groupService;

    private User testUser;
    private Group testGroup;
    private CreateGroupRequest createGroupRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        testGroup = new Group();
        testGroup.setGroupId(1L);
        testGroup.setName("Test Group");
        testGroup.setDescription("Test Description");
        testGroup.setCreatedBy(1L);
        testGroup.setCreatedAt(LocalDateTime.now());

        createGroupRequest = new CreateGroupRequest();
        createGroupRequest.setName("New Group");
        createGroupRequest.setDescription("New Description");
    }

    @Test
    void testCreateGroup_Success() {
        // Arrange
        createGroupRequest.setCreatedBy(1L);
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> {
            Group group = invocation.getArgument(0);
            group.setGroupId(1L);
            return group;
        });
        when(membershipRepository.save(any(Membership.class))).thenAnswer(invocation -> {
            Membership membership = invocation.getArgument(0);
            return membership;
        });
        doNothing().when(activityLogService).logActivity(anyLong(), anyString(), anyString());
        doNothing().when(notificationService).notifyNewGroup(anyLong(), anyString());

        // Act
        GroupDTO result = groupService.createGroup(createGroupRequest);

        // Assert
        assertNotNull(result);
        assertEquals(createGroupRequest.getName(), result.getName());
        assertEquals(createGroupRequest.getDescription(), result.getDescription());
        verify(userRepository).existsById(1L);
        verify(groupRepository).save(any(Group.class));
        verify(membershipRepository).save(any(Membership.class));
    }

    @Test
    void testCreateGroup_UserNotFound() {
        // Arrange
        createGroupRequest.setCreatedBy(1L);
        when(userRepository.existsById(anyLong())).thenReturn(false);

        // Act
        GroupDTO result = groupService.createGroup(createGroupRequest);

        // Assert
        assertNull(result);
        verify(userRepository).existsById(1L);
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void testGetAllGroups() {
        // Arrange
        Group group1 = new Group();
        group1.setGroupId(1L);
        group1.setName("Group 1");

        Group group2 = new Group();
        group2.setGroupId(2L);
        group2.setName("Group 2");

        when(groupRepository.findAll()).thenReturn(Arrays.asList(group1, group2));

        // Act
        List<GroupDTO> result = groupService.getAllGroups();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(groupRepository).findAll();
    }

    @Test
    void testGetGroupById_Success() {
        // Arrange
        when(groupRepository.findById(anyLong())).thenReturn(Optional.of(testGroup));

        // Act
        GroupDTO result = groupService.getGroupById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testGroup.getName(), result.getName());
        verify(groupRepository).findById(1L);
    }

    @Test
    void testGetGroupById_NotFound() {
        // Arrange
        when(groupRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        GroupDTO result = groupService.getGroupById(1L);

        // Assert
        assertNull(result);
        verify(groupRepository).findById(1L);
    }
}

