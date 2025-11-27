package sk.ikts.server.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.ikts.server.dto.LoginRequest;
import sk.ikts.server.dto.LoginResponse;
import sk.ikts.server.dto.RegisterRequest;
import sk.ikts.server.dto.UserDTO;
import sk.ikts.server.service.UserService;

import java.util.Map;

/**
 * REST Controller for user operations
 * Handles registration, login, and user management endpoints
 * Added by Cursor AI - REST API controller for user operations
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Register a new user
     * POST /api/users/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            UserDTO user = userService.register(request);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Email already exists");
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Login user
     * POST /api/users/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * Get user by ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable("id") Long id) {
        UserDTO user = userService.getUserById(id);
        
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(user);
    }

    /**
     * Update user profile
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable("id") Long id, @RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String password = request.get("password");
            
            UserDTO updatedUser = userService.updateUser(id, name, password);
            
            if (updatedUser == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update user: " + e.getMessage());
        }
    }
}

