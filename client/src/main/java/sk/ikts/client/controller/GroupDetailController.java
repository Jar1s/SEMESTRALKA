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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import okhttp3.*;
import sk.ikts.client.model.Group;
import sk.ikts.client.model.Resource;
import sk.ikts.client.model.Task;
import sk.ikts.client.util.ApiClient;
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

    public void setGroup(Group group) {
        this.group = group;
        if (groupNameLabel != null && group != null) {
            groupNameLabel.setText(group.getName());
        }
        loadData();
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTables();
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
                        ComboBox<Task.TaskStatus> statusCombo = new ComboBox<>(
                                FXCollections.observableArrayList(Task.TaskStatus.values()));
                        statusCombo.setValue(task.getStatus());
                        statusCombo.setOnAction(e -> updateTaskStatus(task.getTaskId(), statusCombo.getValue()));
                        hbox.getChildren().add(statusCombo);
                        setGraphic(hbox);
                    }
                }
            });
        }
        if (tasksTable != null) {
            tasksTable.setItems(tasksList);
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
                            downloadButton.setOnAction(e -> downloadResource(resource));
                            hbox.getChildren().add(downloadButton);
                        } else {
                            Button openButton = new Button("Open");
                            openButton.setOnAction(e -> openUrl(resource.getPathOrUrl()));
                            hbox.getChildren().add(openButton);
                        }
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
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/sk/ikts/client/view/dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (Exception e) {
            e.printStackTrace();
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
                    new Alert(Alert.AlertType.INFORMATION, "Task created successfully!").show();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    new Alert(Alert.AlertType.ERROR, "Failed to create task: " + e.getMessage()).show();
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
                        .url("http://localhost:8080/api/resources/upload")
                        .post(requestBody)
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        Platform.runLater(() -> {
                            loadResources();
                            new Alert(Alert.AlertType.INFORMATION, "File uploaded successfully!").show();
                        });
                    } else {
                        Platform.runLater(() -> {
                            new Alert(Alert.AlertType.ERROR, "Failed to upload file").show();
                        });
                    }
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage()).show();
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
                    new Alert(Alert.AlertType.INFORMATION, "URL shared successfully!").show();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    new Alert(Alert.AlertType.ERROR, "Failed to share URL: " + e.getMessage()).show();
                });
                e.printStackTrace();
            }
        });
    }

    private void downloadResource(Resource resource) {
        CompletableFuture.runAsync(() -> {
            try {
                Request request = new Request.Builder()
                        .url("http://localhost:8080/api/resources/" + resource.getResourceId() + "/download")
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
}

