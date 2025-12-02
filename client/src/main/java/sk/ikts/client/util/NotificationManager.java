package sk.ikts.client.util;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.concurrent.CompletableFuture;

/**
 * Manager for displaying toast notifications in top right corner
 */
public class NotificationManager {
    
    private static Stage primaryStage;
    private static VBox notificationContainer;
    private static StackPane overlayPane;
    
    /**
     * Initialize notification manager with primary stage
     */
    public static void initialize(Stage stage) {
        primaryStage = stage;
        
        // Create notification container
        notificationContainer = new VBox(10);
        notificationContainer.setAlignment(Pos.TOP_RIGHT);
        notificationContainer.setPadding(new Insets(20, 20, 0, 0));
        notificationContainer.setMouseTransparent(true);
        notificationContainer.setStyle("-fx-background-color: transparent;");
        
        // Create overlay pane
        overlayPane = new StackPane();
        overlayPane.setAlignment(Pos.TOP_RIGHT);
        overlayPane.setMouseTransparent(true);
        overlayPane.getChildren().add(notificationContainer);
        
        // Listen for scene changes and add overlay
        primaryStage.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> {
                    addOverlayToScene();
                });
            }
        });
        
        // Add to current scene if available
        if (primaryStage.getScene() != null) {
            Platform.runLater(() -> {
                addOverlayToScene();
            });
        }
    }
    
    private static void addOverlayToScene() {
        if (primaryStage == null || primaryStage.getScene() == null) return;
        
        Node root = primaryStage.getScene().getRoot();
        if (root == null) return;
        
        // Check if overlay is already added
        if (overlayPane.getParent() != null) {
            return;
        }
        
        if (root instanceof StackPane) {
            StackPane stackPane = (StackPane) root;
            if (!stackPane.getChildren().contains(overlayPane)) {
                stackPane.getChildren().add(overlayPane);
            }
        } else if (root instanceof javafx.scene.layout.BorderPane) {
            javafx.scene.layout.BorderPane borderPane = (javafx.scene.layout.BorderPane) root;
            // Wrap in StackPane
            StackPane wrapper = new StackPane();
            wrapper.getChildren().addAll(borderPane, overlayPane);
            primaryStage.getScene().setRoot(wrapper);
        } else if (root instanceof Pane) {
            Pane pane = (Pane) root;
            if (!pane.getChildren().contains(overlayPane)) {
                pane.getChildren().add(overlayPane);
            }
        } else {
            // Wrap in StackPane
            StackPane wrapper = new StackPane();
            wrapper.getChildren().addAll(root, overlayPane);
            primaryStage.getScene().setRoot(wrapper);
        }
    }
    
    /**
     * Show a notification in the top right corner
     */
    public static void showNotification(String message, String type) {
        if (primaryStage == null) {
            System.err.println("NotificationManager not initialized!");
            return;
        }
        
        // Play sound
        playNotificationSound();
        
        Platform.runLater(() -> {
            // Ensure overlay is in scene
            addOverlayToScene();
            
            // Create notification content
            HBox notificationBox = new HBox(10);
            notificationBox.setAlignment(Pos.CENTER_LEFT);
            notificationBox.setPadding(new Insets(12, 18, 12, 18));
            notificationBox.setMaxWidth(350);
            notificationBox.setMinWidth(250);
            
            // Style based on type
            String backgroundColor = "#667eea"; // Default purple
            String icon = "🔔";
            
            if (type != null) {
                switch (type.toLowerCase()) {
                    case "success":
                        backgroundColor = "#4facfe";
                        icon = "✅";
                        break;
                    case "error":
                        backgroundColor = "#f5576c";
                        icon = "❌";
                        break;
                    case "warning":
                        backgroundColor = "#f39c12";
                        icon = "⚠️";
                        break;
                    case "info":
                    default:
                        backgroundColor = "#667eea";
                        icon = "🔔";
                        break;
                }
            }
            
            notificationBox.setStyle(
                "-fx-background-color: " + backgroundColor + "; " +
                "-fx-background-radius: 12px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10, 0, 0, 2);"
            );
            
            Label iconLabel = new Label(icon);
            iconLabel.setStyle("-fx-font-size: 18px;");
            
            Label messageLabel = new Label(message);
            messageLabel.setWrapText(true);
            messageLabel.setStyle(
                "-fx-text-fill: white; " +
                "-fx-font-size: 13px; " +
                "-fx-font-weight: bold;"
            );
            
            notificationBox.getChildren().addAll(iconLabel, messageLabel);
            
            // Add to container (at the top)
            notificationContainer.getChildren().add(0, notificationBox);
            
            // Animate in
            notificationBox.setTranslateX(400);
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), notificationBox);
            slideIn.setFromX(400);
            slideIn.setToX(0);
            slideIn.play();
            
            // Auto hide after 4 seconds
            CompletableFuture.delayedExecutor(4, java.util.concurrent.TimeUnit.SECONDS).execute(() -> {
                Platform.runLater(() -> {
                    TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), notificationBox);
                    slideOut.setFromX(0);
                    slideOut.setToX(400);
                    slideOut.setOnFinished(e -> notificationContainer.getChildren().remove(notificationBox));
                    slideOut.play();
                });
            });
        });
    }
    
    /**
     * Play notification sound
     */
    private static void playNotificationSound() {
        try {
            // Use system beep for notification sound
            java.awt.Toolkit.getDefaultToolkit().beep();
            
            // Play a second beep after a short delay for better notification
            CompletableFuture.delayedExecutor(100, java.util.concurrent.TimeUnit.MILLISECONDS).execute(() -> {
                try {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                } catch (Exception e) {
                    // Ignore
                }
            });
        } catch (Exception e) {
            // Ignore if beep fails
        }
    }
    
    /**
     * Show success notification
     */
    public static void showSuccess(String message) {
        showNotification(message, "success");
    }
    
    /**
     * Show error notification
     */
    public static void showError(String message) {
        showNotification(message, "error");
    }
    
    /**
     * Show warning notification
     */
    public static void showWarning(String message) {
        showNotification(message, "warning");
    }
    
    /**
     * Show info notification
     */
    public static void showInfo(String message) {
        showNotification(message, "info");
    }
}
