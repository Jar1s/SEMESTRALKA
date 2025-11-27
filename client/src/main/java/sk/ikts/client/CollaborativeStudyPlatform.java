package sk.ikts.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sk.ikts.client.util.SceneManager;

/**
 * Entry point for the Collaborative Study Platform desktop client.
 * Currently loads a lightweight dashboard shell so the project can be launched
 * from IntelliJ or Maven javaFX plugin without class not found errors.
 */
public class CollaborativeStudyPlatform extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Set primary stage in SceneManager for scene transitions
        SceneManager.setPrimaryStage(primaryStage);
        
        FXMLLoader loader = new FXMLLoader(
                CollaborativeStudyPlatform.class.getResource("/sk/ikts/client/view/login.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Collaborative Study Platform");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinWidth(450);
        primaryStage.setMinHeight(550);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

