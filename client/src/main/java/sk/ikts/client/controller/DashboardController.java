package sk.ikts.client.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import sk.ikts.client.model.Group;
import sk.ikts.client.model.Task;
import sk.ikts.client.model.User;
import sk.ikts.client.util.ApiClient;
import sk.ikts.client.util.NotificationManager;
import sk.ikts.client.util.NotificationWebSocketClient;
import sk.ikts.client.util.SceneManager;
import sk.ikts.client.util.SessionManager;

import java.lang.reflect.Type;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for the main dashboard view
 * Handles groups, tasks, statistics, and notifications
 */
public class DashboardController implements Initializable {

    private Long userId;
    private final Gson gson = ApiClient.getGson();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private ObservableList<Group> allGroupsList = FXCollections.observableArrayList();
    private ObservableList<Group> myGroupsList = FXCollections.observableArrayList();
    private ObservableList<Task> tasksList = FXCollections.observableArrayList();
    private List<Group> allGroups = List.of(); // For filtering tasks
    
    private NotificationWebSocketClient webSocketClient;

    // FXML elements
    @FXML private Label statusLabel;
    @FXML private Button refreshButton;
    @FXML private Button profileButton;
    @FXML private TabPane mainTabPane;
    
    // All Groups Tab
    @FXML private TableView<Group> allGroupsTable;
    @FXML private TableColumn<Group, String> allGroupNameColumn;
    @FXML private TableColumn<Group, String> allGroupDescriptionColumn;
    @FXML private TableColumn<Group, String> allGroupOwnerColumn;
    @FXML private TableColumn<Group, String> allGroupCreatedColumn;
    @FXML private TableColumn<Group, String> allGroupActionsColumn;
    
    // My Groups Tab
    @FXML private TableView<Group> myGroupsTable;
    @FXML private TableColumn<Group, String> myGroupNameColumn;
    @FXML private TableColumn<Group, String> myGroupDescriptionColumn;
    @FXML private TableColumn<Group, String> myGroupOwnerColumn;
    @FXML private TableColumn<Group, String> myGroupCreatedColumn;
    @FXML private TableColumn<Group, String> myGroupActionsColumn;
    
    @FXML private Button createGroupButton;
    
    // Tasks Tab
    @FXML private TableView<Task> tasksTable;
    @FXML private TableColumn<Task, String> taskTitleColumn;
    @FXML private TableColumn<Task, String> taskGroupColumn;
    @FXML private TableColumn<Task, String> taskStatusColumn;
    @FXML private TableColumn<Task, String> taskDeadlineColumn;
    @FXML private TableColumn<Task, String> taskActionsColumn;
    @FXML private ComboBox<Group> groupFilterComboBox;
    @FXML private Button createTaskButton;
    
    // Statistics Tab
    @FXML private Label totalGroupsLabel;
    @FXML private Label totalTasksLabel;
    @FXML private Label openTasksLabel;
    @FXML private Label completedTasksLabel;
    @FXML private Label completionRatioLabel;
    @FXML private ProgressBar completionProgressBar;
    @FXML private PieChart taskStatusPieChart;
    @FXML private BarChart<String, Number> taskStatusBarChart;
    @FXML private CategoryAxis statusCategoryAxis;
    @FXML private NumberAxis taskCountAxis;
    @FXML private BarChart<String, Number> tasksByGroupBarChart;
    @FXML private CategoryAxis groupCategoryAxis;
    @FXML private NumberAxis groupTaskCountAxis;
    @FXML private VBox activitySummaryBox;
    @FXML private Label activityLabel;
    
    // Settings Tab
    @FXML private TextField settingsNameField;
    @FXML private PasswordField settingsPasswordField;
    @FXML private PasswordField settingsConfirmPasswordField;
    @FXML private Button updateNameButton;
    @FXML private Button updatePasswordButton;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> userNameColumn;
    @FXML private TableColumn<User, String> userEmailColumn;
    
    private ObservableList<User> usersList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Basic initialization
        if (statusLabel != null) {
            statusLabel.setText("Loading dashboard...");
        }
        if (refreshButton != null) {
            refreshButton.setOnAction(e -> loadData());
        }
        
        // Setup tables
        setupTasksTable();
        setupUsersTable();
        
        // Setup buttons
        if (createGroupButton != null) {
            createGroupButton.setOnAction(e -> showCreateGroupDialog());
        }
        if (createTaskButton != null) {
            createTaskButton.setOnAction(e -> showCreateTaskDialog());
        }
        if (groupFilterComboBox != null) {
            groupFilterComboBox.setOnAction(e -> handleGroupFilterChange());
        }
        if (profileButton != null) {
            profileButton.setOnAction(e -> showProfileDialog());
        }
        
        // Initialize charts
        initializeCharts();
        
        // Setup groups tables
        setupAllGroupsTable();
        setupMyGroupsTable();
    }
    
    private void initializeCharts() {
        // Initialize PieChart
        if (taskStatusPieChart != null) {
            taskStatusPieChart.setTitle("Task Status Distribution");
            taskStatusPieChart.setLegendVisible(true);
        }
        
        // Initialize BarChart for task status
        if (taskStatusBarChart != null) {
            taskStatusBarChart.setTitle("Tasks by Status");
            taskStatusBarChart.setLegendVisible(false);
        }
        
        // Initialize BarChart for tasks by group
        if (tasksByGroupBarChart != null) {
            tasksByGroupBarChart.setTitle("Tasks by Group");
            tasksByGroupBarChart.setLegendVisible(false);
        }
    }
    
    @FXML
    private void showProfileDialog() {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Profile Settings");
        dialog.setHeaderText("Update your profile");

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("New password (leave empty to keep current)");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm password");

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
        content.getChildren().addAll(
                new Label("Name:"), nameField,
                new Label("New Password (optional):"), passwordField,
                new Label("Confirm Password:"), confirmPasswordField);
        dialog.getDialogPane().setContent(content);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Map<String, String> result = new HashMap<>();
                result.put("name", nameField.getText().trim());
                result.put("password", passwordField.getText());
                result.put("confirmPassword", confirmPasswordField.getText());
                return result;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String password = result.get("password");
            if (!password.isEmpty()) {
                if (!password.equals(result.get("confirmPassword"))) {
                    showError("Passwords do not match");
                    return;
                }
                if (password.length() < 6) {
                    showError("Password must be at least 6 characters");
                    return;
                }
            }
            // Update profile via API
            updateProfile(result.get("name"), password);
        });
    }
    
    private void setupAllGroupsTable() {
        if (allGroupsTable == null) return;
        
        if (allGroupNameColumn != null) {
            allGroupNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        }
        if (allGroupDescriptionColumn != null) {
            allGroupDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        }
        if (allGroupOwnerColumn != null) {
            allGroupOwnerColumn.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
        }
        if (allGroupCreatedColumn != null) {
            allGroupCreatedColumn.setCellFactory(column -> new TableCell<Group, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow().getItem() == null) {
                        setText(null);
                    } else {
                        Group group = getTableRow().getItem();
                        if (group.getCreatedAt() != null) {
                            setText(group.getCreatedAt().format(dateFormatter));
                        } else {
                            setText("N/A");
                        }
                    }
                }
            });
        }
        if (allGroupActionsColumn != null) {
            allGroupActionsColumn.setCellFactory(column -> new TableCell<Group, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        Group group = getTableRow().getItem();
                        HBox hbox = new HBox(5);
                        Button viewButton = new Button("View");
                        viewButton.setOnAction(e -> DashboardController.this.viewGroupDetails(group));
                        
                        // Check if user is already a member
                        List<Group> myGroupsCopy = new ArrayList<>(myGroupsList);
                        boolean isMember = myGroupsCopy.stream()
                                .anyMatch(g -> g.getGroupId().equals(group.getGroupId()));
                        
                        if (!isMember) {
                            Button joinButton = new Button("Join");
                            joinButton.setStyle("-fx-background-color: #000000; -fx-text-fill: white;");
                            joinButton.setOnAction(e -> DashboardController.this.handleJoinGroup(group));
                            hbox.getChildren().addAll(viewButton, joinButton);
                        } else {
                            hbox.getChildren().add(viewButton);
                        }
                        setGraphic(hbox);
                    }
                }
            });
        }
        if (allGroupsTable != null) {
            allGroupsTable.setItems(allGroupsList);
            // Add double-click to view group details
            allGroupsTable.setRowFactory(tv -> {
                TableRow<Group> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        Group group = row.getItem();
                        viewGroupDetails(group);
                    }
                });
                return row;
            });
        }
    }
    
    private void setupMyGroupsTable() {
        if (myGroupsTable == null) return;
        
        if (myGroupNameColumn != null) {
            myGroupNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        }
        if (myGroupDescriptionColumn != null) {
            myGroupDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        }
        if (myGroupOwnerColumn != null) {
            myGroupOwnerColumn.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
        }
        if (myGroupCreatedColumn != null) {
            myGroupCreatedColumn.setCellFactory(column -> new TableCell<Group, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow().getItem() == null) {
                        setText(null);
                    } else {
                        Group group = getTableRow().getItem();
                        if (group.getCreatedAt() != null) {
                            setText(group.getCreatedAt().format(dateFormatter));
                        } else {
                            setText("N/A");
                        }
                    }
                }
            });
        }
        if (myGroupActionsColumn != null) {
            myGroupActionsColumn.setCellFactory(column -> new TableCell<Group, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        Group group = getTableRow().getItem();
                        HBox hbox = new HBox(5);
                        Button viewButton = new Button("View");
                        viewButton.setOnAction(e -> DashboardController.this.viewGroupDetails(group));
                        
                        // Check if user is owner
                        boolean isOwner = userId != null && group.getCreatedBy() != null && 
                                        group.getCreatedBy().equals(userId);
                        
                        if (isOwner) {
                            Button editButton = new Button("Edit");
                            editButton.setOnAction(e -> DashboardController.this.showEditGroupDialog(group));
                            Button deleteButton = new Button("Delete");
                            deleteButton.setStyle("-fx-background-color: #000000; -fx-text-fill: white;");
                            deleteButton.setOnAction(e -> DashboardController.this.deleteGroup(group));
                            hbox.getChildren().addAll(viewButton, editButton, deleteButton);
                        } else {
                            Button leaveButton = new Button("Leave");
                            leaveButton.setStyle("-fx-background-color: #000000; -fx-text-fill: white;");
                            leaveButton.setOnAction(e -> DashboardController.this.handleLeaveGroup(group));
                            hbox.getChildren().addAll(viewButton, leaveButton);
                        }
                        setGraphic(hbox);
                    }
                }
            });
        }
        if (myGroupsTable != null) {
            myGroupsTable.setItems(myGroupsList);
            // Add double-click to view group details
            myGroupsTable.setRowFactory(tv -> {
                TableRow<Group> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        Group group = row.getItem();
                        viewGroupDetails(group);
                    }
                });
                return row;
            });
        }
    }
    
    private void setupTasksTable() {
        if (tasksTable == null) return;
        
        if (taskTitleColumn != null) {
            taskTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        }
        if (taskGroupColumn != null) {
            taskGroupColumn.setCellFactory(column -> new TableCell<Task, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow().getItem() == null) {
                        setText(null);
                    } else {
                        Task task = getTableRow().getItem();
                        Group group = allGroups.stream()
                                .filter(g -> g.getGroupId().equals(task.getGroupId()))
                                .findFirst()
                                .orElse(null);
                        setText(group != null ? group.getName() : "Unknown");
                    }
                }
            });
        }
        if (taskStatusColumn != null) {
            taskStatusColumn.setCellFactory(column -> new TableCell<Task, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow().getItem() == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        Task task = getTableRow().getItem();
                        ComboBox<Task.TaskStatus> statusCombo = new ComboBox<>(
                                FXCollections.observableArrayList(Task.TaskStatus.values()));
                        statusCombo.setValue(task.getStatus());
                        statusCombo.setPrefWidth(130);
                        statusCombo.setMaxWidth(130);
                        statusCombo.setPrefHeight(25);
                        statusCombo.setMaxHeight(25);
                        // Use CSS class for better styling and readability
                        statusCombo.getStyleClass().add("combo-box");
                        statusCombo.setStyle(
                            "-fx-font-size: 11px; " +
                            "-fx-padding: 2px 5px; " +
                            "-fx-text-fill: #2c3e50;"
                        );
                        statusCombo.setOnAction(e -> updateTaskStatus(task.getTaskId(), statusCombo.getValue()));
                        setGraphic(statusCombo);
                    }
                }
            });
        }
        if (taskDeadlineColumn != null) {
            taskDeadlineColumn.setCellFactory(column -> new TableCell<Task, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow().getItem() == null) {
                        setText(null);
                    } else {
                        Task task = getTableRow().getItem();
                        if (task.getDeadline() != null) {
                            setText(task.getDeadline().format(dateFormatter));
                        } else {
                            setText("No deadline");
                        }
                    }
                }
            });
        }
        if (taskActionsColumn != null) {
            taskActionsColumn.setCellFactory(column -> new TableCell<Task, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        Task task = getTableRow().getItem();
                        Button editButton = new Button("Edit");
                        editButton.setPrefWidth(70);
                        editButton.setMaxWidth(70);
                        editButton.setStyle("-fx-font-size: 12px;");
                        editButton.setOnAction(e -> showEditTaskDialog(task));
                        setGraphic(editButton);
                    }
                }
            });
        }
        tasksTable.setItems(tasksList);
    }

    /**
     * Set the current user ID (called by SceneManager)
     */
    public void setUserId(Long userId) {
        this.userId = userId;
        // Connect to WebSocket for notifications
        connectWebSocket();
    }
    
    private void connectWebSocket() {
        webSocketClient = new NotificationWebSocketClient();
        webSocketClient.connect(notification -> {
            // Handle notification
            Platform.runLater(() -> {
                if (statusLabel != null) {
                    statusLabel.setText("🔔 " + notification.getMessage());
                }
                
                String notificationType = notification.getType();
                
                // Refresh data based on notification type
                if ("NEW_TASK".equals(notificationType) || 
                    "TASK_STATUS_CHANGED".equals(notificationType) ||
                    "DEADLINE_URGENT".equals(notificationType) ||
                    "DEADLINE_WARNING".equals(notificationType) ||
                    "DEADLINE_REMINDER".equals(notificationType) ||
                    "DEADLINE_OVERDUE".equals(notificationType)) {
                    loadTasks();
                    updateStatistics();
                } else if ("NEW_GROUP".equals(notificationType) || 
                          "NEW_MEMBER".equals(notificationType)) {
                    loadAllGroups();
                    loadMyGroups();
                    updateStatistics();
                }
                
                // Show toast notification with appropriate type
                if ("DEADLINE_URGENT".equals(notificationType) || 
                    "DEADLINE_OVERDUE".equals(notificationType)) {
                    NotificationManager.showError(notification.getMessage());
                } else if ("DEADLINE_WARNING".equals(notificationType)) {
                    NotificationManager.showWarning(notification.getMessage());
                } else {
                    NotificationManager.showInfo(notification.getMessage());
                }
            });
        });
    }

    /**
     * Load all data (called by SceneManager)
     */
    public void loadData() {
        if (userId == null) {
            if (statusLabel != null) {
                statusLabel.setText("Error: No user ID");
            }
            return;
        }

        if (statusLabel != null) {
            statusLabel.setText("Loading data...");
        }

        // Load groups, tasks, and users in parallel
        CompletableFuture<Void> allGroupsFuture = loadAllGroups();
        CompletableFuture<Void> myGroupsFuture = loadMyGroups();
        CompletableFuture<Void> tasksFuture = loadTasks();
        loadUsers(); // Load users

        CompletableFuture.allOf(allGroupsFuture, myGroupsFuture, tasksFuture).thenRun(() -> {
            Platform.runLater(() -> {
                if (statusLabel != null) {
                    statusLabel.setText("Dashboard loaded. All Groups: " + allGroupsList.size() + 
                                      ", My Groups: " + myGroupsList.size() + 
                                      ", Tasks: " + tasksList.size());
                }
                updateStatistics();
                updateGroupFilter();
            });
        });
    }

    /**
     * Load all groups from API - loads ALL groups regardless of creator
     */
    private CompletableFuture<Void> loadAllGroups() {
        return CompletableFuture.runAsync(() -> {
            try {
                String response = ApiClient.get("/groups");
                Type listType = new TypeToken<List<Group>>(){}.getType();
                List<Group> groups = gson.fromJson(response, listType);
                
                System.out.println("Loaded " + (groups != null ? groups.size() : 0) + " all groups from API");
                
                Platform.runLater(() -> {
                    allGroupsList.clear();
                    if (groups != null && !groups.isEmpty()) {
                        allGroupsList.addAll(groups);
                        allGroups = groups;
                        System.out.println("Added " + groups.size() + " groups to All Groups table");
                        updateGroupFilter();
                    } else {
                        System.out.println("No groups found or groups list is null");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (statusLabel != null) {
                        statusLabel.setText("Error loading all groups: " + e.getMessage());
                    }
                });
                System.err.println("Error loading all groups: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Load my groups from API - loads only groups where user is a member
     */
    private CompletableFuture<Void> loadMyGroups() {
        if (userId == null) {
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.runAsync(() -> {
            try {
                String response = ApiClient.get("/groups/user/" + userId);
                Type listType = new TypeToken<List<Group>>(){}.getType();
                List<Group> groups = gson.fromJson(response, listType);
                
                System.out.println("Loaded " + (groups != null ? groups.size() : 0) + " my groups from API");
                
                Platform.runLater(() -> {
                    myGroupsList.clear();
                    if (groups != null && !groups.isEmpty()) {
                        myGroupsList.addAll(groups);
                        System.out.println("Added " + groups.size() + " groups to My Groups table");
                    } else {
                        System.out.println("No my groups found");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (statusLabel != null) {
                        statusLabel.setText("Error loading my groups: " + e.getMessage());
                    }
                });
                System.err.println("Error loading my groups: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Load tasks from all groups
     */
    private CompletableFuture<Void> loadTasks() {
        return CompletableFuture.runAsync(() -> {
            try {
                // First load groups if not loaded
                if (allGroups.isEmpty()) {
                    String groupsResponse = ApiClient.get("/groups");
                    Type listType = new TypeToken<List<Group>>(){}.getType();
                    allGroups = gson.fromJson(groupsResponse, listType);
                }

                // Load tasks from all groups
                tasksList.clear();
                for (Group group : allGroups) {
                    try {
                        String tasksResponse = ApiClient.get("/tasks/group/" + group.getGroupId());
                        Type taskListType = new TypeToken<List<Task>>(){}.getType();
                        List<Task> tasks = gson.fromJson(tasksResponse, taskListType);
                        if (tasks != null) {
                            Platform.runLater(() -> tasksList.addAll(tasks));
                        }
                    } catch (Exception e) {
                        // Skip groups with no tasks or errors
                    }
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (statusLabel != null) {
                        statusLabel.setText("Error loading tasks: " + e.getMessage());
                    }
                });
                e.printStackTrace();
            }
        });
    }

    /**
     * Handle refresh button click
     */
    @FXML
    private void handleRefresh() {
        loadData();
        loadUsers();
    }
    
    private void setupUsersTable() {
        if (userNameColumn != null) {
            userNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        }
        if (userEmailColumn != null) {
            userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        }
        if (usersTable != null) {
            usersTable.setItems(usersList);
        }
    }
    
    private void loadUsers() {
        CompletableFuture.runAsync(() -> {
            try {
                String response = ApiClient.get("/users");
                Type listType = new TypeToken<List<User>>(){}.getType();
                List<User> users = gson.fromJson(response, listType);
                
                Platform.runLater(() -> {
                    if (users != null) {
                        usersList.setAll(users);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    @FXML
    private void handleUpdateName() {
        if (userId == null || settingsNameField == null) return;
        
        String newName = settingsNameField.getText().trim();
        if (newName.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Name cannot be empty").show();
            return;
        }
        
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, String> request = new HashMap<>();
                request.put("name", newName);
                
                String response = ApiClient.put("/users/" + userId, request);
                User updatedUser = gson.fromJson(response, User.class);
                
                Platform.runLater(() -> {
                    if (updatedUser != null) {
                        NotificationManager.showSuccess("Name updated successfully!");
                        settingsNameField.clear();
                        loadUsers(); // Refresh users list
                    } else {
                        NotificationManager.showError("Failed to update name");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    NotificationManager.showError("Failed to update name: " + e.getMessage());
                });
                e.printStackTrace();
            }
        });
    }
    
    @FXML
    private void handleUpdatePassword() {
        if (userId == null || settingsPasswordField == null || settingsConfirmPasswordField == null) return;
        
        String newPassword = settingsPasswordField.getText();
        String confirmPassword = settingsConfirmPasswordField.getText();
        
        if (newPassword.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Password cannot be empty").show();
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            new Alert(Alert.AlertType.WARNING, "Passwords do not match").show();
            return;
        }
        
        if (newPassword.length() < 6) {
            new Alert(Alert.AlertType.WARNING, "Password must be at least 6 characters long").show();
            return;
        }
        
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, String> request = new HashMap<>();
                request.put("password", newPassword);
                
                String response = ApiClient.put("/users/" + userId, request);
                User updatedUser = gson.fromJson(response, User.class);
                
                Platform.runLater(() -> {
                    if (updatedUser != null) {
                        NotificationManager.showSuccess("Password updated successfully!");
                        settingsPasswordField.clear();
                        settingsConfirmPasswordField.clear();
                    } else {
                        NotificationManager.showError("Failed to update password");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    NotificationManager.showError("Failed to update password: " + e.getMessage());
                });
                e.printStackTrace();
            }
        });
    }

    /**
     * Create a new group
     */
    public void createGroup(String name, String description) {
        if (userId == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("name", name);
                request.put("description", description);
                request.put("createdBy", userId);

                String response = ApiClient.post("/groups", request);
                Group newGroup = gson.fromJson(response, Group.class);
                
                Platform.runLater(() -> {
                    if (newGroup != null) {
                        // Reload both lists to get updated data with owner names
                        loadAllGroups();
                        loadMyGroups();
                        NotificationManager.showSuccess("Group created successfully!");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Failed to create group: " + e.getMessage());
                });
                e.printStackTrace();
            }
        });
    }

    /**
     * Create a new task
     */
    public void createTask(Long groupId, String title, String description, LocalDateTime deadline) {
        if (userId == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("groupId", groupId);
                request.put("createdBy", userId);
                request.put("title", title);
                request.put("description", description);
                if (deadline != null) {
                    request.put("deadline", deadline.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }

                String response = ApiClient.post("/tasks", request);
                Task newTask = gson.fromJson(response, Task.class);
                
                Platform.runLater(() -> {
                    if (newTask != null) {
                        tasksList.add(newTask);
                        loadData(); // Refresh all data
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Failed to create task: " + e.getMessage());
                });
                e.printStackTrace();
            }
        });
    }

    /**
     * Update task status
     */
    public void updateTaskStatus(Long taskId, Task.TaskStatus status) {
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, String> request = new HashMap<>();
                request.put("status", status.name());

                String response = ApiClient.put("/tasks/" + taskId + "/status", request);
                Task updatedTask = gson.fromJson(response, Task.class);
                
                Platform.runLater(() -> {
                    if (updatedTask != null) {
                        // Update task in list
                        for (int i = 0; i < tasksList.size(); i++) {
                            if (tasksList.get(i).getTaskId().equals(taskId)) {
                                tasksList.set(i, updatedTask);
                                break;
                            }
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Failed to update task: " + e.getMessage());
                });
                e.printStackTrace();
            }
        });
    }

    /**
     * Get statistics
     */
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalGroups", myGroupsList.size());
        stats.put("totalTasks", tasksList.size());
        
        // Create a copy of the list to avoid ConcurrentModificationException
        List<Task> tasksCopy = new ArrayList<>(tasksList);
        
        long openTasks = tasksCopy.stream()
                .filter(t -> t.getStatus() == Task.TaskStatus.OPEN || 
                            t.getStatus() == Task.TaskStatus.IN_PROGRESS)
                .count();
        long doneTasks = tasksCopy.stream()
                .filter(t -> t.getStatus() == Task.TaskStatus.DONE)
                .count();
        
        stats.put("openTasks", (int) openTasks);
        stats.put("doneTasks", (int) doneTasks);
        
        return stats;
    }

    /**
     * Get groups list (for compatibility)
     */
    public ObservableList<Group> getGroups() {
        return myGroupsList;
    }

    /**
     * Get tasks list
     */
    public ObservableList<Task> getTasks() {
        return tasksList;
    }

    /**
     * Get all groups (for filtering)
     */
    public List<Group> getAllGroups() {
        return allGroups;
    }

    @FXML
    private void showCreateGroupDialog() {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Create New Group");
        dialog.setHeaderText("Enter group information");

        TextField nameField = new TextField();
        nameField.setPromptText("Group name");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");
        descriptionArea.setPrefRowCount(3);

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
        content.getChildren().addAll(
                new Label("Name:"), nameField,
                new Label("Description:"), descriptionArea);
        dialog.getDialogPane().setContent(content);

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                Map<String, String> result = new HashMap<>();
                result.put("name", nameField.getText().trim());
                result.put("description", descriptionArea.getText().trim());
                return result;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (!result.get("name").isEmpty()) {
                createGroup(result.get("name"), result.get("description"));
            }
        });
    }

    @FXML
    private void showCreateTaskDialog() {
        if (allGroups.isEmpty()) {
            showError("Please create a group first");
            return;
        }

        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Create New Task");
        dialog.setHeaderText("Enter task information");

        ComboBox<Group> groupCombo = new ComboBox<>(FXCollections.observableArrayList(allGroups));
        groupCombo.setPromptText("Select Group");
        TextField titleField = new TextField();
        titleField.setPromptText("Task title");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");
        descriptionArea.setPrefRowCount(3);
        DatePicker deadlinePicker = new DatePicker();

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
        content.getChildren().addAll(
                new Label("Group:"), groupCombo,
                new Label("Title:"), titleField,
                new Label("Description:"), descriptionArea,
                new Label("Deadline (optional):"), deadlinePicker);
        dialog.getDialogPane().setContent(content);

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                Map<String, Object> result = new HashMap<>();
                result.put("groupId", groupCombo.getValue() != null ? groupCombo.getValue().getGroupId() : null);
                result.put("title", titleField.getText().trim());
                result.put("description", descriptionArea.getText().trim());
                result.put("deadline", deadlinePicker.getValue() != null ? 
                    deadlinePicker.getValue().atStartOfDay() : null);
                return result;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            Long groupId = (Long) result.get("groupId");
            String title = (String) result.get("title");
            if (groupId != null && !title.isEmpty()) {
                createTask(groupId, title, (String) result.get("description"), 
                          (LocalDateTime) result.get("deadline"));
            }
        });
    }

    @FXML
    private void handleGroupFilterChange() {
        Group selectedGroup = groupFilterComboBox.getValue();
        if (selectedGroup == null) {
            loadTasks();
        } else {
            loadTasksForGroup(selectedGroup.getGroupId());
        }
    }

    private void loadTasksForGroup(Long groupId) {
        CompletableFuture.runAsync(() -> {
            try {
                String tasksResponse = ApiClient.get("/tasks/group/" + groupId);
                Type taskListType = new TypeToken<List<Task>>(){}.getType();
                List<Task> tasks = gson.fromJson(tasksResponse, taskListType);
                Platform.runLater(() -> {
                    tasksList.clear();
                    if (tasks != null) {
                        tasksList.addAll(tasks);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void showEditTaskDialog(Task task) {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Edit Task");
        dialog.setHeaderText("Edit task information");

        TextField titleField = new TextField(task.getTitle());
        TextArea descriptionArea = new TextArea(task.getDescription() != null ? task.getDescription() : "");
        descriptionArea.setPrefRowCount(3);
        DatePicker deadlinePicker = new DatePicker();
        if (task.getDeadline() != null) {
            deadlinePicker.setValue(task.getDeadline().toLocalDate());
        }
        ComboBox<Task.TaskStatus> statusCombo = new ComboBox<>(
                FXCollections.observableArrayList(Task.TaskStatus.values()));
        statusCombo.setValue(task.getStatus());

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
        content.getChildren().addAll(
                new Label("Title:"), titleField,
                new Label("Description:"), descriptionArea,
                new Label("Status:"), statusCombo,
                new Label("Deadline (optional):"), deadlinePicker);
        dialog.getDialogPane().setContent(content);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Map<String, Object> result = new HashMap<>();
                result.put("title", titleField.getText().trim());
                result.put("description", descriptionArea.getText().trim());
                result.put("status", statusCombo.getValue().name());
                result.put("deadline", deadlinePicker.getValue() != null ? 
                    deadlinePicker.getValue().atStartOfDay() : null);
                return result;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            updateTask(task.getTaskId(), (String) result.get("title"), 
                      (String) result.get("description"), 
                      Task.TaskStatus.valueOf((String) result.get("status")),
                      (LocalDateTime) result.get("deadline"));
        });
    }

    private void updateTask(Long taskId, String title, String description, 
                           Task.TaskStatus status, LocalDateTime deadline) {
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("title", title);
                request.put("description", description);
                request.put("status", status.name());
                if (deadline != null) {
                    request.put("deadline", deadline.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }

                String response = ApiClient.put("/tasks/" + taskId, request);
                Task updatedTask = gson.fromJson(response, Task.class);
                
                Platform.runLater(() -> {
                    if (updatedTask != null) {
                        for (int i = 0; i < tasksList.size(); i++) {
                            if (tasksList.get(i).getTaskId().equals(taskId)) {
                                tasksList.set(i, updatedTask);
                                break;
                            }
                        }
                        updateStatistics();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Failed to update task: " + e.getMessage());
                });
                e.printStackTrace();
            }
        });
    }

    private void viewGroupDetails(Group group) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/sk/ikts/client/view/group-detail.fxml"));
            Parent root = loader.load();
            GroupDetailController controller = loader.getController();
            controller.setGroup(group);
            controller.setUserId(userId);
            
            Stage stage = (Stage) (allGroupsTable != null && allGroupsTable.getScene() != null ? 
                    allGroupsTable.getScene().getWindow() : 
                    (myGroupsTable != null && myGroupsTable.getScene() != null ? 
                     myGroupsTable.getScene().getWindow() : null));
            if (stage == null) {
                stage = (Stage) mainTabPane.getScene().getWindow();
            }
            stage.setScene(new Scene(root, 1000, 700));
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Failed to open group details: " + e.getMessage());
            alert.show();
        }
    }

    private void showEditGroupDialog(Group group) {
        // Check if user is the owner
        if (userId == null || group.getCreatedBy() == null || !group.getCreatedBy().equals(userId)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Access Denied");
            alert.setHeaderText("Cannot edit group");
            alert.setContentText("Only the group owner can edit the group.");
            alert.showAndWait();
            return;
        }
        
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Edit Group");
        dialog.setHeaderText("Edit group information");

        TextField nameField = new TextField(group.getName());
        TextArea descriptionArea = new TextArea(group.getDescription() != null ? group.getDescription() : "");
        descriptionArea.setPromptText("Description");
        descriptionArea.setPrefRowCount(3);

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
        content.getChildren().addAll(
                new Label("Name:"), nameField,
                new Label("Description:"), descriptionArea);
        dialog.getDialogPane().setContent(content);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Map<String, String> result = new HashMap<>();
                result.put("name", nameField.getText().trim());
                result.put("description", descriptionArea.getText().trim());
                return result;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (!result.get("name").isEmpty()) {
                updateGroup(group.getGroupId(), result.get("name"), result.get("description"));
            }
        });
    }

    private void updateGroup(Long groupId, String name, String description) {
        if (userId == null) {
            showError("User ID not available");
            return;
        }
        
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("name", name);
                request.put("description", description);
                request.put("createdBy", userId); // Required by CreateGroupRequest

                String response = ApiClient.put("/groups/" + groupId, request);
                Group updatedGroup = gson.fromJson(response, Group.class);
                
                Platform.runLater(() -> {
                    if (updatedGroup != null) {
                        // Update group in all groups list
                        for (int i = 0; i < allGroupsList.size(); i++) {
                            if (allGroupsList.get(i).getGroupId().equals(groupId)) {
                                allGroupsList.set(i, updatedGroup);
                                break;
                            }
                        }
                        // Update group in my groups list
                        for (int i = 0; i < myGroupsList.size(); i++) {
                            if (myGroupsList.get(i).getGroupId().equals(groupId)) {
                                myGroupsList.set(i, updatedGroup);
                                break;
                            }
                        }
                        allGroups = List.copyOf(allGroupsList);
                        updateStatistics();
                        updateGroupFilter();
                        NotificationManager.showSuccess("Group updated successfully!");
                    } else {
                        NotificationManager.showError("Failed to update group. Only the owner can edit.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Failed to update group: " + e.getMessage());
                });
                e.printStackTrace();
            }
        });
    }

    private void handleJoinGroup(Group group) {
        if (userId == null) {
            showError("User ID not available");
            return;
        }
        
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Long> request = new HashMap<>();
                request.put("userId", userId);
                
                ApiClient.post("/groups/" + group.getGroupId() + "/join", request);
                
                Platform.runLater(() -> {
                    // Reload both lists
                    loadAllGroups();
                    loadMyGroups();
                    NotificationManager.showSuccess("Successfully joined group: " + group.getName());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Failed to join group: " + e.getMessage());
                });
                e.printStackTrace();
            }
        });
    }
    
    private void handleLeaveGroup(Group group) {
        if (userId == null) {
            showError("User ID not available");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Leave Group");
        confirmAlert.setHeaderText("Are you sure you want to leave this group?");
        confirmAlert.setContentText("Group: " + group.getName());

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                CompletableFuture.runAsync(() -> {
                    try {
                        Map<String, Object> request = new HashMap<>();
                        request.put("userId", userId);
                        
                        ApiClient.delete("/groups/" + group.getGroupId() + "/leave", request);
                        
                        Platform.runLater(() -> {
                            // Reload both lists
                            loadAllGroups();
                            loadMyGroups();
                            NotificationManager.showSuccess("Successfully left group: " + group.getName());
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            showError("Failed to leave group: " + e.getMessage());
                        });
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private void deleteGroup(Group group) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Group");
        confirmAlert.setHeaderText("Are you sure you want to delete this group?");
        confirmAlert.setContentText("Group: " + group.getName() + "\n\nThis action cannot be undone.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                CompletableFuture.runAsync(() -> {
                    try {
                        ApiClient.delete("/groups/" + group.getGroupId());
                        
                        Platform.runLater(() -> {
                            allGroupsList.remove(group);
                            myGroupsList.remove(group);
                            allGroups = List.copyOf(allGroupsList);
                            updateStatistics();
                            updateGroupFilter();
                            NotificationManager.showSuccess("Group deleted successfully!");
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            showError("Failed to delete group: " + e.getMessage());
                        });
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private void updateStatistics() {
        Map<String, Integer> stats = getStatistics();
        if (totalGroupsLabel != null) {
            totalGroupsLabel.setText(String.valueOf(stats.get("totalGroups")));
        }
        if (totalTasksLabel != null) {
            totalTasksLabel.setText(String.valueOf(stats.get("totalTasks")));
        }
        if (openTasksLabel != null) {
            openTasksLabel.setText(String.valueOf(stats.get("openTasks")));
        }
        if (completedTasksLabel != null) {
            completedTasksLabel.setText(String.valueOf(stats.get("doneTasks")));
        }
        
        // Calculate completion ratio
        int totalTasks = stats.get("totalTasks");
        int doneTasks = stats.get("doneTasks");
        double ratio = totalTasks > 0 ? (double) doneTasks / totalTasks : 0.0;
        int percentage = (int) (ratio * 100);
        
        if (completionRatioLabel != null) {
            completionRatioLabel.setText(percentage + "%");
        }
        if (completionProgressBar != null) {
            completionProgressBar.setProgress(ratio);
        }
        
        // Update charts
        updateCharts();
    }
    
    private void updateCharts() {
        // Update PieChart - Task Status Distribution
        if (taskStatusPieChart != null) {
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            
            // Create a copy to avoid ConcurrentModificationException
            List<Task> tasksCopy = new ArrayList<>(tasksList);
            
            long openCount = tasksCopy.stream()
                    .filter(t -> t.getStatus() == Task.TaskStatus.OPEN)
                    .count();
            long inProgressCount = tasksCopy.stream()
                    .filter(t -> t.getStatus() == Task.TaskStatus.IN_PROGRESS)
                    .count();
            long doneCount = tasksCopy.stream()
                    .filter(t -> t.getStatus() == Task.TaskStatus.DONE)
                    .count();
            
            if (openCount > 0) {
                PieChart.Data openData = new PieChart.Data("Open (" + openCount + ")", openCount);
                pieChartData.add(openData);
            }
            if (inProgressCount > 0) {
                PieChart.Data inProgressData = new PieChart.Data("In Progress (" + inProgressCount + ")", inProgressCount);
                pieChartData.add(inProgressData);
            }
            if (doneCount > 0) {
                PieChart.Data doneData = new PieChart.Data("Done (" + doneCount + ")", doneCount);
                pieChartData.add(doneData);
            }
            
            taskStatusPieChart.setData(pieChartData);
            
            // Set colors for pie chart slices
            if (!pieChartData.isEmpty()) {
                for (PieChart.Data data : pieChartData) {
                    String color = "#000000"; // Black for Open
                    if (data.getName().contains("In Progress")) {
                        color = "#000000"; // Black for In Progress
                    } else if (data.getName().contains("Done")) {
                        color = "#000000"; // Black for Done
                    }
                    data.getNode().setStyle("-fx-pie-color: " + color + ";");
                }
            }
        }
        
        // Update BarChart - Tasks by Status
        if (taskStatusBarChart != null) {
            taskStatusBarChart.getData().clear();
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Tasks");
            
            // Create a copy to avoid ConcurrentModificationException
            List<Task> tasksCopy = new ArrayList<>(tasksList);
            
            long openCount = tasksCopy.stream()
                    .filter(t -> t.getStatus() == Task.TaskStatus.OPEN)
                    .count();
            long inProgressCount = tasksCopy.stream()
                    .filter(t -> t.getStatus() == Task.TaskStatus.IN_PROGRESS)
                    .count();
            long doneCount = tasksCopy.stream()
                    .filter(t -> t.getStatus() == Task.TaskStatus.DONE)
                    .count();
            
            series.getData().add(new XYChart.Data<>("Open", openCount));
            series.getData().add(new XYChart.Data<>("In Progress", inProgressCount));
            series.getData().add(new XYChart.Data<>("Done", doneCount));
            
            taskStatusBarChart.getData().add(series);
        }
        
        // Update BarChart - Tasks by Group
        if (tasksByGroupBarChart != null) {
            tasksByGroupBarChart.getData().clear();
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Tasks");
            
            // Count tasks per group
            Map<String, Long> tasksPerGroup = new HashMap<>();
            // Create a copy to avoid ConcurrentModificationException
            List<Task> tasksCopy = new ArrayList<>(tasksList);
            for (Task task : tasksCopy) {
                Group group = allGroups.stream()
                        .filter(g -> g.getGroupId().equals(task.getGroupId()))
                        .findFirst()
                        .orElse(null);
                String groupName = group != null ? group.getName() : "Unknown";
                tasksPerGroup.put(groupName, tasksPerGroup.getOrDefault(groupName, 0L) + 1);
            }
            
            for (Map.Entry<String, Long> entry : tasksPerGroup.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            
            tasksByGroupBarChart.getData().add(series);
        }
        
        // Update Activity Monitoring
        updateActivityMonitoring();
    }
    
    private void updateActivityMonitoring() {
        if (activitySummaryBox == null) return;
        
        activitySummaryBox.getChildren().clear();
        
        // Calculate activity statistics
        int totalGroups = myGroupsList.size();
        int totalTasks = tasksList.size();
        
        // Create a copy to avoid ConcurrentModificationException
        List<Task> tasksCopy = new ArrayList<>(tasksList);
        
        int completedTasks = (int) tasksCopy.stream()
                .filter(t -> t.getStatus() == Task.TaskStatus.DONE)
                .count();
        int openTasks = (int) tasksCopy.stream()
                .filter(t -> t.getStatus() == Task.TaskStatus.OPEN)
                .count();
        int inProgressTasks = (int) tasksCopy.stream()
                .filter(t -> t.getStatus() == Task.TaskStatus.IN_PROGRESS)
                .count();
        
        double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0.0;
        
        // Create activity summary labels
        Label groupsLabel = new Label("• Total Groups: " + totalGroups);
        groupsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50;");
        activitySummaryBox.getChildren().add(groupsLabel);
        
        Label tasksLabel = new Label("• Total Tasks: " + totalTasks);
        tasksLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50;");
        activitySummaryBox.getChildren().add(tasksLabel);
        
        Label openLabel = new Label("• Open Tasks: " + openTasks);
        openLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #000000;");
        activitySummaryBox.getChildren().add(openLabel);
        
        Label inProgressLabel = new Label("• In Progress: " + inProgressTasks);
        inProgressLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #000000;");
        activitySummaryBox.getChildren().add(inProgressLabel);
        
        Label completedLabel = new Label("• Completed: " + completedTasks);
        completedLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #000000;");
        activitySummaryBox.getChildren().add(completedLabel);
        
        Label completionRateLabel = new Label("• Completion Rate: " + String.format("%.1f", completionRate) + "%");
        completionRateLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #000000;");
        activitySummaryBox.getChildren().add(completionRateLabel);
        
        // Add separator
        Separator separator = new Separator();
        activitySummaryBox.getChildren().add(separator);
        
        // Show recent activity (last 5 tasks)
        Label recentLabel = new Label("Recent Tasks:");
        recentLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        activitySummaryBox.getChildren().add(recentLabel);
        
        // Create a copy to avoid ConcurrentModificationException
        List<Task> tasksCopyForRecent = new ArrayList<>(tasksList);
        List<Task> recentTasks = tasksCopyForRecent.stream()
                .sorted((t1, t2) -> {
                    if (t1.getCreatedAt() != null && t2.getCreatedAt() != null) {
                        return t2.getCreatedAt().compareTo(t1.getCreatedAt());
                    }
                    return 0;
                })
                .limit(5)
                .toList();
        
        if (recentTasks.isEmpty()) {
            Label noTasksLabel = new Label("No tasks yet. Create your first task!");
            noTasksLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");
            activitySummaryBox.getChildren().add(noTasksLabel);
        } else {
            for (Task task : recentTasks) {
                Group taskGroup = allGroups.stream()
                        .filter(g -> g.getGroupId().equals(task.getGroupId()))
                        .findFirst()
                        .orElse(null);
                String groupName = taskGroup != null ? taskGroup.getName() : "Unknown";
                String status = task.getStatus().name();
                String created = task.getCreatedAt() != null ? 
                    task.getCreatedAt().format(dateFormatter) : "N/A";
                
                Label taskLabel = new Label("  - " + task.getTitle() + " (" + groupName + ") - " + 
                    status + " - " + created);
                taskLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
                activitySummaryBox.getChildren().add(taskLabel);
            }
        }
    }

    private void updateGroupFilter() {
        if (groupFilterComboBox != null) {
            // Use myGroupsList for filtering tasks (only groups where user is a member)
            groupFilterComboBox.setItems(FXCollections.observableArrayList(myGroupsList));
            groupFilterComboBox.setConverter(new StringConverter<Group>() {
                @Override
                public String toString(Group group) {
                    return group != null ? group.getName() : "";
                }
                
                @Override
                public Group fromString(String string) {
                    // Create a copy to avoid ConcurrentModificationException
                    List<Group> myGroupsCopy = new ArrayList<>(myGroupsList);
                    return myGroupsCopy.stream()
                            .filter(g -> g.getName().equals(string))
                            .findFirst()
                            .orElse(null);
                }
            });
        }
    }

    private void updateProfile(String name, String password) {
        if (userId == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                Map<String, String> request = new HashMap<>();
                if (name != null && !name.trim().isEmpty()) {
                    request.put("name", name.trim());
                }
                if (password != null && !password.isEmpty()) {
                    request.put("password", password);
                }

                ApiClient.put("/users/" + userId, request);
                Platform.runLater(() -> {
                    new Alert(Alert.AlertType.INFORMATION, "Profile updated successfully!").show();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Failed to update profile: " + e.getMessage());
                });
                e.printStackTrace();
            }
        });
    }

    private void showError(String message) {
        if (statusLabel != null) {
            statusLabel.setText("Error: " + message);
        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText(message);
                alert.show();
            });
        }
    }

    /**
     * Handle logout - disconnect WebSocket, clear session, and return to login
     */
    @FXML
    private void handleLogout() {
        // Disconnect WebSocket if connected
        if (webSocketClient != null) {
            webSocketClient.disconnect();
            webSocketClient = null;
        }
        
        // Clear session
        SessionManager.getInstance().logout();
        
        // Navigate to login screen
        SceneManager.showLoginScene();
    }
}
