package controllers;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import Authorization.Auth;
import models.User;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;

public class EmployeeController
{

    @FXML
    private ComboBox<String> topLeftMenu;

    @FXML
    private TextField lastLoginField;

    @FXML
    private ComboBox<String> userMenu;

    private User currentUser;
    private Auth auth;

    @FXML
    private ImageView emp_image;

    @FXML
    private TextField dep_info;

    //file access granted to employees
    private final Map<String, List<String>> fileAccessMap = Map.of(
            "Emp_data.csv", List.of("aisha.khan@gmail.com", "priya.sharma@gmail.com"),
            "Maintenance.txt", List.of("rahul.joshi@gmail.com", "vikram.nair@gmail.com"),
            "Proj_Report.txt", List.of("arjun.patel@gmail.com", "nikhil.verma@gmail.com"),
            "System_Audio.csv", List.of("nikhil.verma@gmail.com", "vikram.nair@gmail.com")
    );

    public void initData(User user)
    {
        this.currentUser = user;
        this.auth = new Auth();

        //Displaying contents in employee dashboard
        InputStream img = getClass().getResourceAsStream("/images/emp_pic.png");
        if (img != null)
        {
            emp_image.setImage(new Image(img));
        }
        

        dep_info.setText("Department: " + user.getDepartment());

        //Top Left Menu Options: with all file options in the drop down
        topLeftMenu.getItems().clear();
        topLeftMenu.getItems().addAll(fileAccessMap.keySet());
        

        //File handle selection
        topLeftMenu.setOnAction(e -> handleFileAccess(topLeftMenu.getValue()));

        //Last login/logout display of the employee
        lastLoginField.setText("Last login: " + user.getLastLogin() + " | Last logout: " + user.getLastLogout());

        //Top Right Menu to show user's first & last name with logout option
        userMenu.getItems().clear();
        userMenu.getItems().addAll(user.getFullName(), "Logout");
        

        //Logout action
        userMenu.setOnAction((ActionEvent e) ->
        {
            if ("Logout".equals(userMenu.getValue()))
            {
                auth.updateLastLogout(user.getEmail());
                try
                {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) lastLoginField.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        });
    }

    //top left menu, files reading with access grant and decline
    private void handleFileAccess(String fileName)
    {
        List<String> allowedUsers = fileAccessMap.get(fileName);

        if (allowedUsers != null && allowedUsers.contains(currentUser.getEmail()))
        {
            // Reading file content for the users who are allowed to access the file
            InputStream is = getClass().getResourceAsStream("/files/" + fileName);
            if (is != null)
            {
                String content = new BufferedReader(new InputStreamReader(is))
                        .lines()
                        .collect(Collectors.joining("\n"));

                // for displaying the contents of the file when opened:
                TextArea textArea = new TextArea(content);
                textArea.setEditable(false);
                textArea.setWrapText(true);

                VBox layout = new VBox(textArea);
                Scene scene = new Scene(layout, 600, 400);

                Stage window = new Stage();
                window.setTitle("Viewing: " + fileName);
                window.setScene(scene);
                window.show();
            }
            else
            {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("File Not Found");
                error.setHeaderText("Missing File in the database");
                error.setContentText("The file " + fileName + " could not be found.");
                error.showAndWait();
            }

        }
        else
        {
            //If user does not have access, pop-up alert
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Access Denied");
            error.setHeaderText("Cannot access this file");
            error.setContentText("Insufficient privileges!");
            error.showAndWait();

            //The row entries in suspicious_activity database
            auth.insertSuspiciousActivityFile(
                    currentUser.getEmployeeId(),
                    currentUser.getEmail(),
                    "High",
                    "Invalid File Access",
                    fileName
            );
        }
    }
}