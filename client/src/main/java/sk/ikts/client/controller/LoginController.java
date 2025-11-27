package sk.ikts.client.controller;

import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import sk.ikts.client.model.User;
import sk.ikts.client.util.ApiClient;
import sk.ikts.client.util.SceneManager;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for login/register view
 * Handles user authentication and registration
 * Added by Cursor AI - controller for login/register functionality
 */
public class LoginController implements Initializable {

    @FXML
    private TabPane tabPane;
    
    @FXML
    private Tab loginTab;
    
    @FXML
    private Tab registerTab;
    
    // Login fields
    @FXML
    private TextField loginEmail;
    
    @FXML
    private PasswordField loginPassword;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Label loginErrorLabel;
    
    // Register fields
    @FXML
    private TextField registerName;
    
    @FXML
    private TextField registerEmail;
    
    @FXML
    private PasswordField registerPassword;
    
    @FXML
    private Button registerButton;
    
    @FXML
    private Label registerErrorLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loginErrorLabel.setVisible(false);
        registerErrorLabel.setVisible(false);
        
        loginButton.setOnAction(e -> handleLogin());
        registerButton.setOnAction(e -> handleRegister());
    }

    private void handleLogin() {
        String email = loginEmail.getText().trim();
        String password = loginPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showLoginError("Please fill in all fields");
            return;
        }

        try {
            Map<String, String> request = new HashMap<>();
            request.put("email", email);
            request.put("password", password);

            String response = ApiClient.post("/users/login", request);
            Gson gson = ApiClient.getGson();
            
            @SuppressWarnings("unchecked")
            Map<String, Object> result = gson.fromJson(response, Map.class);
            
            // Handle both Boolean and Number types for success field
            Boolean success = false;
            Object successObj = result.get("success");
            if (successObj instanceof Boolean) {
                success = (Boolean) successObj;
            } else if (successObj instanceof Number) {
                success = ((Number) successObj).intValue() == 1;
            }
            
            if (success) {
                @SuppressWarnings("unchecked")
                Map<String, Object> userMap = (Map<String, Object>) result.get("user");
                Long userId = ((Number) userMap.get("userId")).longValue();
                SceneManager.showDashboardScene(userId);
            } else {
                Object messageObj = result.get("message");
                String message = messageObj != null ? messageObj.toString() : "Login failed";
                showLoginError(message);
            }
        } catch (Exception e) {
            showLoginError("Connection error: " + e.getMessage());
        }
    }

    private void handleRegister() {
        String name = registerName.getText().trim();
        String email = registerEmail.getText().trim();
        String password = registerPassword.getText();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showRegisterError("Please fill in all fields");
            return;
        }

        if (password.length() < 6) {
            showRegisterError("Password must be at least 6 characters");
            return;
        }

        try {
            Map<String, String> request = new HashMap<>();
            request.put("name", name);
            request.put("email", email);
            request.put("password", password);

            String response = ApiClient.post("/users/register", request);
            Gson gson = ApiClient.getGson();
            
            try {
                // Try to parse as User object (successful registration)
                User user = gson.fromJson(response, User.class);
                if (user != null && user.getUserId() != null) {
                    // Registration successful, switch to login tab
                    tabPane.getSelectionModel().select(loginTab);
                    registerName.clear();
                    registerEmail.clear();
                    registerPassword.clear();
                    showRegisterError("Registration successful! Please login.");
                    registerErrorLabel.setStyle("-fx-text-fill: green;");
                } else {
                    // If parsing as User fails, it might be an error message
                    showRegisterError(response);
                }
            } catch (Exception e) {
                // If parsing fails, response is likely an error message string
                showRegisterError(response != null && !response.isEmpty() ? response : "Registration failed");
            }
        } catch (Exception e) {
            showRegisterError("Connection error: " + e.getMessage());
        }
    }

    private void showLoginError(String message) {
        loginErrorLabel.setText(message);
        loginErrorLabel.setVisible(true);
        loginErrorLabel.setStyle("-fx-text-fill: red;");
    }

    private void showRegisterError(String message) {
        registerErrorLabel.setText(message);
        registerErrorLabel.setVisible(true);
        if (!message.contains("successful")) {
            registerErrorLabel.setStyle("-fx-text-fill: red;");
        }
    }
}

