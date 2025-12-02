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
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import okhttp3.*;
import sk.ikts.client.model.ChatMessage;
import sk.ikts.client.model.Group;
import sk.ikts.client.model.Resource;
import sk.ikts.client.model.Task;
import sk.ikts.client.util.ApiClient;
import sk.ikts.client.util.ChatWebSocketClient;
import sk.ikts.client.util.NotificationManager;
import sk.ikts.client.util.SceneManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class GroupDetailController implements Initializable {

    private Group group;
    private Long userId;
    private final Gson gson = ApiClient.getGson();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final OkHttpClient httpClient = new OkHttpClient();
    
    private ObservableList<Task> tasksList = FXCollections.observableArrayList();
    private ObservableList<Resource> resourcesList = FXCollections.observableArrayList();

    @FXML private Label groupNameLabel;
    @FXML private Button backButton;
    @FXML private TabPane detailTabPane;
    
    // Tasks
    @FXML private TableView<Task> tasksTable;
    @FXML private TableColumn<Task, String> taskTitleColumn;
    @FXML private TableColumn<Task, String> taskStatusColumn;
    @FXML private TableColumn<Task, String> taskDeadlineColumn;
    @FXML private TableColumn<Task, String> taskActionsColumn;
    @FXML private Button createTaskButton;
    
    // Resources
    @FXML private TableView<Resource> resourcesTable;
    @FXML private TableColumn<Resource, String> resourceTitleColumn;
    @FXML private TableColumn<Resource, String> resourceTypeColumn;
    @FXML private TableColumn<Resource, String> resourceUploadedColumn;
    @FXML private TableColumn<Resource, String> resourceActionsColumn;
    @FXML private Button uploadFileButton;
    @FXML private Button shareUrlButton;
    
    // Members
    @FXML private ListView<String> membersList;
    
    // Chat
    @FXML private VBox chatMessagesBox;
    @FXML private ScrollPane chatScrollPane;
    @FXML private TextField chatMessageField;
    @FXML private Button sendMessageButton;
    
    private ChatWebSocketClient chatWebSocketClient;
    private String currentUserName;

    public void setGroup(Group group) {
        this.group = group;
        if (groupNameLabel != null && group != null) {
            groupNameLabel.setText(group.getName());
        }
        loadData();
    }

    public void setUserId(Long userId) {
        this.userId = userId;
        // Load user name for chat
        loadUserName();
    }
    
    private void loadUserName() {
        if (userId == null) return;
        
        CompletableFuture.runAsync(() -> {
            try {
                String response = ApiClient.get("/users/" + userId);
                Map<String, Object> user = gson.fromJson(response, Map.class);
                if (user != null && user.get("name") != null) {
                    currentUserName = user.get("name").toString();
                }
            } catch (Exception e) {
                currentUserName = "User " + userId;
                e.printStackTrace();
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTables();
        
        // Setup buttons
        if (uploadFileButton != null) {
            uploadFileButton.setOnAction(e -> handleUploadFile());
        }
        if (shareUrlButton != null) {
            shareUrlButton.setOnAction(e -> handleShareUrl());
        }
        if (createTaskButton != null) {
            createTaskButton.setOnAction(e -> handleCreateTask());
        }
        if (backButton != null) {
            backButton.setOnAction(e -> handleBack());
        }
        if (sendMessageButton != null) {
            sendMessageButton.setOnAction(e -> handleSendMessage());
        }
        if (chatMessageField != null) {
            chatMessageField.setOnAction(e -> handleSendMessage());
        }
    }

    private void setupTables() {
        // Tasks table
        if (taskTitleColumn != null) {
            taskTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
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
                        setText(task.getDeadline() != null ? 
                            task.getDeadline().format(dateFormatter) : "No deadline");
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
        if (tasksTable != null) {
            tasksTable.setItems(tasksList);
            // Add double-click to edit task
            tasksTable.setRowFactory(tv -> {
                TableRow<Task> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        Task task = row.getItem();
                        showEditTaskDialog(task);
                    }
                });
                return row;
            });
        }

        // Resources table
        if (resourceTitleColumn != null) {
            resourceTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        }
        if (resourceTypeColumn != null) {
            resourceTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        }
        if (resourceUploadedColumn != null) {
            resourceUploadedColumn.setCellFactory(column -> new TableCell<Resource, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow().getItem() == null) {
                        setText(null);
                    } else {
                        Resource resource = getTableRow().getItem();
                        setText(resource.getUploadedAt() != null ? 
                            resource.getUploadedAt().format(dateFormatter) : "N/A");
                    }
                }
            });
        }
        if (resourceActionsColumn != null) {
            resourceActionsColumn.setCellFactory(column -> new TableCell<Resource, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        Resource resource = getTableRow().getItem();
                        HBox hbox = new HBox(5);
                        if (resource.getType() == Resource.ResourceType.FILE) {
                            Button downloadButton = new Button("Download");
                            downloadButton.setOnAction(e -> GroupDetailController.this.downloadResource(resource));
                            hbox.getChildren().add(downloadButton);
                        } else {
                            Button openButton = new Button("Open");
                            openButton.setOnAction(e -> GroupDetailController.this.openUrl(resource.getPathOrUrl()));
                            hbox.getChildren().add(openButton);
                        }
                        Button deleteButton = new Button("Delete");
                        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                        deleteButton.setOnAction(e -> GroupDetailController.this.deleteResource(resource));
                        hbox.getChildren().add(deleteButton);
                        setGraphic(hbox);
                    }
                }
            });
        }
        if (resourcesTable != null) {
            resourcesTable.setItems(resourcesList);
        }
    }

    private void loadData() {
        if (group == null) return;
        
        loadTasks();
        loadResources();
        loadMembers();
        loadChatMessages();
        connectChatWebSocket();
    }
    
    private void loadChatMessages() {
        if (group == null || chatMessagesBox == null) {
            System.out.println("Cannot load messages: group=" + group + ", chatMessagesBox=" + chatMessagesBox);
            return;
        }
        
        CompletableFuture.runAsync(() -> {
            try {
                System.out.println("Loading chat messages for group: " + group.getGroupId());
                String response = ApiClient.get("/chat/group/" + group.getGroupId());
                System.out.println("Chat API response: " + response);
                
                Type listType = new TypeToken<List<ChatMessage>>(){}.getType();
                List<ChatMessage> messages = gson.fromJson(response, listType);
                
                System.out.println("Loaded " + (messages != null ? messages.size() : 0) + " messages");
                
                Platform.runLater(() -> {
                    chatMessagesBox.getChildren().clear();
                    if (messages != null && !messages.isEmpty()) {
                        for (ChatMessage message : messages) {
                            System.out.println("Adding message: " + message.getMessage() + " from " + message.getUserName());
                            addChatMessageToUI(message);
                        }
                        scrollChatToBottom();
                    } else {
                        // Show empty state
                        Label emptyLabel = new Label("No messages yet. Start the conversation!");
                        emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d; -fx-padding: 20;");
                        emptyLabel.setAlignment(javafx.geometry.Pos.CENTER);
                        chatMessagesBox.getChildren().add(emptyLabel);
                    }
                });
            } catch (Exception e) {
                System.err.println("Error loading chat messages: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    Label errorLabel = new Label("Error loading messages: " + e.getMessage());
                    errorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #f5576c; -fx-padding: 10;");
                    chatMessagesBox.getChildren().add(errorLabel);
                });
            }
        });
    }
    
    private void connectChatWebSocket() {
        if (group == null || userId == null) return;
        
        // Disconnect existing connection
        if (chatWebSocketClient != null) {
            chatWebSocketClient.disconnect();
        }
        
        chatWebSocketClient = new ChatWebSocketClient();
        chatWebSocketClient.connect(group.getGroupId(), this::addChatMessageToUI);
    }
    
    private void addChatMessageToUI(ChatMessage message) {
        if (chatMessagesBox == null || message == null) return;
        
        Platform.runLater(() -> {
            boolean isMyMessage = message.getUserId() != null && message.getUserId().equals(userId);
            
            // Main container for the message
            HBox messageContainer = new HBox(10);
            messageContainer.setMaxWidth(Double.MAX_VALUE);
            messageContainer.setPadding(new Insets(8, 15, 8, 15));
            
            if (isMyMessage) {
                messageContainer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            } else {
                messageContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            }
            
            // Spacer for alignment
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            // Message bubble container
            VBox messageBubble = new VBox(5);
            messageBubble.setMaxWidth(500);
            messageBubble.setPadding(new Insets(10, 15, 10, 15));
            
            // Style for message bubble
            if (isMyMessage) {
                messageBubble.setStyle(
                    "-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%); " +
                    "-fx-background-radius: 18px 18px 4px 18px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.3), 5, 0, 0, 2);"
                );
            } else {
                messageBubble.setStyle(
                    "-fx-background-color: white; " +
                    "-fx-background-radius: 18px 18px 18px 4px; " +
                    "-fx-border-color: #e9ecef; " +
                    "-fx-border-width: 1px; " +
                    "-fx-border-radius: 18px 18px 18px 4px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 3, 0, 0, 1);"
                );
            }
            
            // User name and time
            HBox headerBox = new HBox(8);
            headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            Label nameLabel = new Label(message.getUserName() != null ? message.getUserName() : "Unknown");
            nameLabel.setStyle(
                "-fx-font-size: 13px; " +
                "-fx-font-weight: bold; " +
                (isMyMessage ? "-fx-text-fill: rgba(255, 255, 255, 0.9);" : "-fx-text-fill: #667eea;")
            );
            
            if (message.getSentAt() != null) {
                Label timeLabel = new Label(message.getSentAt().format(DateTimeFormatter.ofPattern("HH:mm")));
                timeLabel.setStyle(
                    "-fx-font-size: 11px; " +
                    (isMyMessage ? "-fx-text-fill: rgba(255, 255, 255, 0.7);" : "-fx-text-fill: #6c757d;")
                );
                headerBox.getChildren().addAll(nameLabel, timeLabel);
            } else {
                headerBox.getChildren().add(nameLabel);
            }
            
            // Message text
            Label messageLabel = new Label(message.getMessage() != null ? message.getMessage() : "");
            messageLabel.setWrapText(true);
            messageLabel.setStyle(
                "-fx-font-size: 14px; " +
                "-fx-line-spacing: 2px; " +
                (isMyMessage ? "-fx-text-fill: white;" : "-fx-text-fill: #2c3e50;")
            );
            
            messageBubble.getChildren().addAll(headerBox, messageLabel);
            
            // Add to container
            if (isMyMessage) {
                messageContainer.getChildren().addAll(spacer, messageBubble);
            } else {
                messageContainer.getChildren().addAll(messageBubble, spacer);
            }
            
            chatMessagesBox.getChildren().add(messageContainer);
            scrollChatToBottom();
        });
    }
    
    private void scrollChatToBottom() {
        if (chatScrollPane != null) {
            Platform.runLater(() -> {
                chatScrollPane.setVvalue(1.0);
            });
        }
    }
    
    @FXML
    private void handleSendMessage() {
        if (group == null || userId == null || chatMessageField == null) {
            System.out.println("Cannot send message: group=" + group + ", userId=" + userId + ", field=" + chatMessageField);
            return;
        }
        
        String messageText = chatMessageField.getText().trim();
        if (messageText.isEmpty()) return;
        
        System.out.println("Sending message: " + messageText + " to group " + group.getGroupId());
        
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("groupId", group.getGroupId());
                request.put("userId", userId);
                request.put("message", messageText);
                
                String response = ApiClient.post("/chat/send", request);
                System.out.println("Message sent successfully: " + response);
                
                // Reload messages to show the new one
                Platform.runLater(() -> {
                    chatMessageField.clear();
                    loadChatMessages();
                });
            } catch (Exception e) {
                System.err.println("Error sending message: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    new Alert(Alert.AlertType.ERROR, "Failed to send message: " + e.getMessage()).show();
                });
            }
        });
    }

    private void loadTasks() {
        CompletableFuture.runAsync(() -> {
            try {
                String response = ApiClient.get("/tasks/group/" + group.getGroupId());
                Type listType = new TypeToken<List<Task>>(){}.getType();
                List<Task> tasks = gson.fromJson(response, listType);
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

    private void loadResources() {
        CompletableFuture.runAsync(() -> {
            try {
                String response = ApiClient.get("/resources/group/" + group.getGroupId());
                Type listType = new TypeToken<List<Resource>>(){}.getType();
                List<Resource> resources = gson.fromJson(response, listType);
                Platform.runLater(() -> {
                    resourcesList.clear();
                    if (resources != null) {
                        resourcesList.addAll(resources);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void loadMembers() {
        // TODO: Load members from API
        if (membersList != null) {
            membersList.setItems(FXCollections.observableArrayList("Member 1", "Member 2"));
        }
    }

    @FXML
    private void handleBack() {
        // Disconnect chat WebSocket
        if (chatWebSocketClient != null) {
            chatWebSocketClient.disconnect();
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/sk/ikts/client/view/dashboard.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set userId
            sk.ikts.client.controller.DashboardController controller = loader.getController();
            if (controller != null && userId != null) {
                controller.setUserId(userId);
                controller.loadData();
            }
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Failed to return to dashboard: " + e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void handleCreateTask() {
        if (userId == null || group == null) return;

        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Create New Task");
        dialog.setHeaderText("Enter task information");

        TextField titleField = new TextField();
        titleField.setPromptText("Task title");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");
        descriptionArea.setPrefRowCount(3);
        DatePicker deadlinePicker = new DatePicker();

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
        content.getChildren().addAll(
                new Label("Title:"), titleField,
                new Label("Description:"), descriptionArea,
                new Label("Deadline (optional):"), deadlinePicker);
        dialog.getDialogPane().setContent(content);

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                Map<String, Object> result = new HashMap<>();
                result.put("title", titleField.getText().trim());
                result.put("description", descriptionArea.getText().trim());
                result.put("deadline", deadlinePicker.getValue() != null ? 
                    deadlinePicker.getValue().atStartOfDay() : null);
                return result;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String title = (String) result.get("title");
            if (!title.isEmpty()) {
                createTask(title, (String) result.get("description"), 
                          (LocalDateTime) result.get("deadline"));
            }
        });
    }

    private void createTask(String title, String description, LocalDateTime deadline) {
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("groupId", group.getGroupId());
                request.put("createdBy", userId);
                request.put("title", title);
                request.put("description", description);
                if (deadline != null) {
                    request.put("deadline", deadline.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }

                String response = ApiClient.post("/tasks", request);
                Platform.runLater(() -> {
                    loadTasks();
                    NotificationManager.showSuccess("Task created successfully!");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    NotificationManager.showError("Failed to create task: " + e.getMessage());
                });
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleUploadFile() {
        if (userId == null || group == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Upload");
        File file = fileChooser.showOpenDialog(uploadFileButton.getScene().getWindow());
        
        if (file != null) {
            TextInputDialog dialog = new TextInputDialog(file.getName());
            dialog.setTitle("Upload File");
            dialog.setHeaderText("Enter title for the file");
            dialog.setContentText("Title:");
            
            dialog.showAndWait().ifPresent(title -> {
                if (!title.trim().isEmpty()) {
                    uploadFile(file, title.trim());
                }
            });
        }
    }

    private void uploadFile(File file, String title) {
        if (userId == null || group == null) {
            Platform.runLater(() -> {
                new Alert(Alert.AlertType.ERROR, "Cannot upload: User or group not set").show();
            });
            return;
        }
        
        // Show progress dialog
        Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
        progressAlert.setTitle("Uploading File");
        progressAlert.setHeaderText("Please wait...");
        progressAlert.setContentText("Uploading " + file.getName() + "...");
        progressAlert.show();
        
        CompletableFuture.runAsync(() -> {
            try {
                RequestBody fileBody = RequestBody.create(file, MediaType.parse("application/octet-stream"));
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", file.getName(), fileBody)
                        .addFormDataPart("groupId", group.getGroupId().toString())
                        .addFormDataPart("uploadedBy", userId.toString())
                        .addFormDataPart("title", title)
                        .build();

                Request request = new Request.Builder()
                        .url("http://127.0.0.1:8081/api/resources/upload")
                        .post(requestBody)
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    Platform.runLater(() -> {
                        progressAlert.close();
                    });
                    
                    if (response.isSuccessful()) {
                        Platform.runLater(() -> {
                            progressAlert.close();
                            loadResources();
                            NotificationManager.showSuccess("File '" + title + "' uploaded successfully!");
                        });
                    } else {
                        String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        Platform.runLater(() -> {
                            progressAlert.close();
                            NotificationManager.showError("Failed to upload file: " + response.code() + " - " + errorBody);
                        });
                    }
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressAlert.close();
                    NotificationManager.showError("Error uploading file: " + e.getMessage());
                });
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleShareUrl() {
        if (userId == null || group == null) return;

        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Share URL");
        dialog.setHeaderText("Enter URL information");

        TextField titleField = new TextField();
        titleField.setPromptText("Title");
        TextField urlField = new TextField();
        urlField.setPromptText("URL");

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
        content.getChildren().addAll(
                new Label("Title:"), titleField,
                new Label("URL:"), urlField);
        dialog.getDialogPane().setContent(content);

        ButtonType shareButtonType = new ButtonType("Share", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(shareButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == shareButtonType) {
                Map<String, String> result = new HashMap<>();
                result.put("title", titleField.getText().trim());
                result.put("url", urlField.getText().trim());
                return result;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (!result.get("title").isEmpty() && !result.get("url").isEmpty()) {
                shareUrl(result.get("title"), result.get("url"));
            }
        });
    }

    private void shareUrl(String title, String url) {
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("groupId", group.getGroupId());
                request.put("uploadedBy", userId);
                request.put("title", title);
                request.put("url", url);

                String response = ApiClient.post("/resources/url", request);
                    Platform.runLater(() -> {
                        loadResources();
                        NotificationManager.showSuccess("URL shared successfully!");
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        NotificationManager.showError("Failed to share URL: " + e.getMessage());
                    });
                e.printStackTrace();
            }
        });
    }

    private void downloadResource(Resource resource) {
        CompletableFuture.runAsync(() -> {
            try {
                Request request = new Request.Builder()
                        .url("http://127.0.0.1:8081/api/resources/" + resource.getResourceId() + "/download")
                        .get()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        byte[] fileContent = response.body().bytes();
                        
                        Platform.runLater(() -> {
                            FileChooser fileChooser = new FileChooser();
                            fileChooser.setTitle("Save File");
                            fileChooser.setInitialFileName(resource.getTitle());
                            File file = fileChooser.showSaveDialog(resourcesTable.getScene().getWindow());
                            
                            if (file != null) {
                                try {
                                    Files.write(file.toPath(), fileContent);
                                    new Alert(Alert.AlertType.INFORMATION, "File downloaded successfully!").show();
                                } catch (IOException e) {
                                    new Alert(Alert.AlertType.ERROR, "Failed to save file: " + e.getMessage()).show();
                                }
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    new Alert(Alert.AlertType.ERROR, "Failed to download file: " + e.getMessage()).show();
                });
                e.printStackTrace();
            }
        });
    }

    private void openUrl(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Failed to open URL: " + e.getMessage()).show();
        }
    }

    private void updateTaskStatus(Long taskId, Task.TaskStatus status) {
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, String> request = new HashMap<>();
                request.put("status", status.name());
                String response = ApiClient.put("/tasks/" + taskId + "/status", request);
                Platform.runLater(() -> loadTasks());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void deleteResource(Resource resource) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Resource");
        confirmAlert.setHeaderText("Are you sure you want to delete this resource?");
        confirmAlert.setContentText("Resource: " + resource.getTitle() + "\n\nThis action cannot be undone.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                CompletableFuture.runAsync(() -> {
                    try {
                        ApiClient.delete("/resources/" + resource.getResourceId());
                        
                        Platform.runLater(() -> {
                            loadResources();
                            NotificationManager.showSuccess("Resource deleted successfully!");
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            NotificationManager.showError("Failed to delete resource: " + e.getMessage());
                        });
                        e.printStackTrace();
                    }
                });
            }
        });
    }

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
                        loadTasks(); // Reload tasks to show updated data
                        NotificationManager.showSuccess("Task updated successfully!");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    NotificationManager.showError("Failed to update task: " + e.getMessage());
                });
                e.printStackTrace();
            }
        });
    }
}

