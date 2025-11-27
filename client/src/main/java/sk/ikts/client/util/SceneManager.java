package sk.ikts.client.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sk.ikts.client.controller.DashboardController;

import java.io.IOException;

/**
 * Utility class for managing scene transitions
 * Handles loading and switching between different views
 * Added by Cursor AI - scene management utility
 */
public class SceneManager {

    private static Stage primaryStage;
    private static final String BASE_PATH = "/sk/ikts/client/view/";

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Show login/register scene
     */
    public static void showLoginScene() {
        if (primaryStage == null) {
            System.err.println("Error: Primary stage not set. Call SceneManager.setPrimaryStage() first.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(BASE_PATH + "login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 600, 500);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Show dashboard scene
     */
    public static void showDashboardScene(Long userId) {
        if (primaryStage == null) {
            System.err.println("Error: Primary stage not set. Call SceneManager.setPrimaryStage() first.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(BASE_PATH + "dashboard.fxml"));
            Parent root = loader.load();
            DashboardController controller = loader.getController();
            controller.setUserId(userId);
            controller.loadData();
            
            Scene scene = new Scene(root, 1000, 700);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

