package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    public static Stage stage;

    @Override
    public void start(Stage primaryStage) {
        try {
            stage = primaryStage;

            Parent root = FXMLLoader.load(
                    getClass().getResource("/views/login.fxml"));

            
            Scene scene = new Scene(root, 900, 600);  

            
            stage.initStyle(StageStyle.UNDECORATED);  


            stage.centerOnScreen();                   

            stage.setTitle("Login");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
