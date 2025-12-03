package sk.ikts.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import sk.ikts.server.dto.LoginRequest;
import sk.ikts.server.dto.RegisterRequest;
import sk.ikts.server.dto.UserDTO;
import sk.ikts.server.model.AuthProvider;
import sk.ikts.server.model.User;
import sk.ikts.server.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit testy pre UserService
 * Testuje registráciu, prihlásenie a správu používateľov
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityLogService activityLogService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setPasswordHash("$2a$10$hashedPassword");
        testUser.setAuthProvider(AuthProvider.LOCAL);
        testUser.setCreatedAt(LocalDateTime.now());

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setName("New User");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void testRegister_Success() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(1L);
            return user;
        });
        doNothing().when(activityLogService).logActivity(anyLong(), anyString(), anyString());

        // Act
        UserDTO result = userService.register(registerRequest);

        // Assert
        assertNotNull(result);
        assertEquals(registerRequest.getEmail(), result.getEmail());
        assertEquals(registerRequest.getName(), result.getName());
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act
        UserDTO result = userService.register(registerRequest);

        // Assert
        assertNull(result);
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        // Arrange
        // Create a user with properly hashed password
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode("password123");
        testUser.setPasswordHash(hashedPassword);
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        doNothing().when(activityLogService).logActivity(anyLong(), anyString(), anyString());

        // Act
        var result = userService.login(loginRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getUser());
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(activityLogService).logActivity(anyLong(), eq("LOGIN"), anyString());
    }

    @Test
    void testLogin_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act
        var result = userService.login(loginRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        verify(userRepository).findByEmail(loginRequest.getEmail());
    }

    @Test
    void testGetAllUsers() {
        // Arrange
        User user1 = new User();
        user1.setUserId(1L);
        user1.setEmail("user1@example.com");
        user1.setName("User 1");

        User user2 = new User();
        user2.setUserId(2L);
        user2.setEmail("user2@example.com");
        user2.setName("User 2");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user1@example.com", result.get(0).getEmail());
        assertEquals("user2@example.com", result.get(1).getEmail());
        verify(userRepository).findAll();
    }

    @Test
    void testUpdateUser_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserDTO result = userService.updateUser(1L, "Updated Name", "newpassword");

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUser_UserNotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        UserDTO result = userService.updateUser(1L, "Updated Name", "newpassword");

        // Assert
        assertNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }
}

