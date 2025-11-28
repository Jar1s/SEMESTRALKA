package sk.ikts.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import sk.ikts.server.dto.LoginRequest;
import sk.ikts.server.dto.LoginResponse;
import sk.ikts.server.dto.RegisterRequest;
import sk.ikts.server.dto.UserDTO;
import sk.ikts.server.model.User;
import sk.ikts.server.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for user management operations
 * Handles registration, login, and user operations
 * Added by Cursor AI - service layer for user business logic
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Register a new user
     * @param request registration request with user data
     * @return UserDTO of created user or null if email already exists
     */
    public UserDTO register(RegisterRequest request) {
        try {
            // Check if email already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                return null;
            }

            // Hash password
            String hashedPassword = passwordEncoder.encode(request.getPassword());

            // Create and save user
            User user = new User(request.getEmail(), request.getName(), hashedPassword);
            user = userRepository.save(user);

            // Return DTO
            return new UserDTO(user.getUserId(), user.getEmail(), user.getName());
        } catch (Exception e) {
            System.err.println("Error in register service: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to be handled by controller
        }
    }

    /**
     * Authenticate user login
     * @param request login request with email and password
     * @return LoginResponse with success status and user data
     */
    public LoginResponse login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            return new LoginResponse(false, "Invalid email or password", null);
        }

        User user = userOpt.get();

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return new LoginResponse(false, "Invalid email or password", null);
        }

        // Return success response
        UserDTO userDTO = new UserDTO(user.getUserId(), user.getEmail(), user.getName());
        return new LoginResponse(true, "Login successful", userDTO);
    }

    /**
     * Get all users
     * @return List of UserDTO
     */
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserDTO(user.getUserId(), user.getEmail(), user.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Get user by ID
     * @param userId user ID
     * @return UserDTO or null if not found
     */
    public UserDTO getUserById(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return null;
        }
        User user = userOpt.get();
        return new UserDTO(user.getUserId(), user.getEmail(), user.getName());
    }

    /**
     * Update user profile
     * @param userId user ID
     * @param name new name (can be null to keep current)
     * @param password new password (can be null to keep current)
     * @return updated UserDTO or null if user not found
     */
    public UserDTO updateUser(Long userId, String name, String password) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return null;
        }

        User user = userOpt.get();
        
        if (name != null && !name.trim().isEmpty()) {
            user.setName(name.trim());
        }
        
        if (password != null && !password.isEmpty()) {
            String hashedPassword = passwordEncoder.encode(password);
            user.setPasswordHash(hashedPassword);
        }
        
        user = userRepository.save(user);
        return new UserDTO(user.getUserId(), user.getEmail(), user.getName());
    }
}

