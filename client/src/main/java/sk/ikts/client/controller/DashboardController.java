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
import javafx.stage.Stage;
import javafx.util.StringConverter;
import sk.ikts.client.model.Group;
import sk.ikts.client.model.Notification;
import sk.ikts.client.model.Task;
import sk.ikts.client.controller.GroupDetailController;
import sk.ikts.client.util.ApiClient;
import sk.ikts.client.util.NotificationWebSocketClient;
import sk.ikts.client.util.SceneManager;

import java.lang.reflect.Type;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for the main dashboard view
 * Handles groups, tasks, statistics, and notifications
 * Added by Cursor AI - main dashboard controller
 */
public class DashboardController implements Initializable {

    private Long userId;
    private final Gson gson = ApiClient.getGson();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private ObservableList<Group> groupsList = FXCollections.observableArrayList();
    private ObservableList<Task> tasksList = FXCollections.observableArrayList();
    private List<Group> allGroups = List.of();
    
    private NotificationWebSocketClient webSocketClient;

    // FXML elements
    @FXML private Label statusLabel;
    @FXML private Button refreshButton;
    @FXML private Button profileButton;
    @FXML private TabPane mainTabPane;
    
    // Groups Tab
    @FXML private TableView<Group> groupsTable;
    @FXML private TableColumn<Group, String> groupNameColumn;
    @FXML private TableColumn<Group, String> groupDescriptionColumn;
    @FXML private TableColumn<Group, String> groupCreatedColumn;
    @FXML private TableColumn<Group, String> groupActionsColumn;
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
        setupGroupsTable();
        setupTasksTable();
        
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
    
    private void setupGroupsTable() {
        if (groupsTable == null) return;
        
        if (groupNameColumn != null) {
            groupNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        }
        if (groupDescriptionColumn != null) {
            groupDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        }
        if (groupCreatedColumn != null) {
            groupCreatedColumn.setCellFactory(column -> new TableCell<Group, String>() {
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
        if (groupActionsColumn != null) {
            groupActionsColumn.setCellFactory(column -> new TableCell<Group, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        Group group = getTableRow().getItem();
                        HBox hbox = new HBox(5);
                        Button viewButton = new Button("View");
                        viewButton.setOnAction(e -> viewGroupDetails(group));
                        hbox.getChildren().add(viewButton);
                        setGraphic(hbox);
                    }
                }
            });
        }
        groupsTable.setItems(groupsList);
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
            taskStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
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
                        HBox hbox = new HBox(5);
                        Button editButton = new Button("Edit");
                        editButton.setOnAction(e -> showEditTaskDialog(task));
                        ComboBox<Task.TaskStatus> statusCombo = new ComboBox<>(
                                FXCollections.observableArrayList(Task.TaskStatus.values()));
                        statusCombo.setValue(task.getStatus());
                        statusCombo.setOnAction(e -> updateTaskStatus(task.getTaskId(), statusCombo.getValue()));
                        hbox.getChildren().addAll(editButton, statusCombo);
                        setGraphic(hbox);
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
                
                // Refresh data based on notification type
                if ("NEW_TASK".equals(notification.getType()) || 
                    "TASK_STATUS_CHANGED".equals(notification.getType())) {
                    loadTasks();
                    updateStatistics();
                } else if ("NEW_GROUP".equals(notification.getType()) || 
                          "NEW_MEMBER".equals(notification.getType())) {
                    loadGroups();
                    updateStatistics();
                }
                
                // Show notification alert
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Notification");
                alert.setHeaderText(notification.getType());
                alert.setContentText(notification.getMessage());
                alert.show();
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

        // Load groups and tasks in parallel
        CompletableFuture<Void> groupsFuture = loadGroups();
        CompletableFuture<Void> tasksFuture = loadTasks();

        CompletableFuture.allOf(groupsFuture, tasksFuture).thenRun(() -> {
            Platform.runLater(() -> {
                if (statusLabel != null) {
                    statusLabel.setText("Dashboard loaded. Groups: " + groupsList.size() + 
                                      ", Tasks: " + tasksList.size());
                }
                updateStatistics();
                updateGroupFilter();
            });
        });
    }

    /**
     * Load groups from API
     */
    private CompletableFuture<Void> loadGroups() {
        return CompletableFuture.runAsync(() -> {
            try {
                String response = ApiClient.get("/groups");
                Type listType = new TypeToken<List<Group>>(){}.getType();
                List<Group> groups = gson.fromJson(response, listType);
                
                Platform.runLater(() -> {
                    groupsList.clear();
                    if (groups != null) {
                        groupsList.addAll(groups);
                        allGroups = groups;
                        updateGroupFilter();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (statusLabel != null) {
                        statusLabel.setText("Error loading groups: " + e.getMessage());
                    }
                });
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
                        groupsList.add(newGroup);
                        allGroups = List.copyOf(groupsList);
                        loadData(); // Refresh all data
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
        stats.put("totalGroups", groupsList.size());
        stats.put("totalTasks", tasksList.size());
        
        long openTasks = tasksList.stream()
                .filter(t -> t.getStatus() == Task.TaskStatus.OPEN || 
                            t.getStatus() == Task.TaskStatus.IN_PROGRESS)
                .count();
        long doneTasks = tasksList.stream()
                .filter(t -> t.getStatus() == Task.TaskStatus.DONE)
                .count();
        
        stats.put("openTasks", (int) openTasks);
        stats.put("doneTasks", (int) doneTasks);
        
        return stats;
    }

    /**
     * Get groups list
     */
    public ObservableList<Group> getGroups() {
        return groupsList;
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
            
            Stage stage = (Stage) groupsTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Failed to open group details: " + e.getMessage());
            alert.show();
        }
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
    }

    private void updateGroupFilter() {
        if (groupFilterComboBox != null) {
            groupFilterComboBox.setItems(FXCollections.observableArrayList(allGroups));
            groupFilterComboBox.setConverter(new StringConverter<Group>() {
                @Override
                public String toString(Group group) {
                    return group != null ? group.getName() : "";
                }
                
                @Override
                public Group fromString(String string) {
                    return allGroups.stream()
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

                String response = ApiClient.put("/users/" + userId, request);
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
}
