package controllers;

import application.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import models.User;
import models.AdminUser;
import models.Ticket;

import javafx.scene.control.TextInputDialog;
import java.util.Optional;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;


public class ManagerController
{

    // Database configuration constants
    private static final String DATABASE_URL ="jdbc:mysql://www.papademas.net:3307/510sp?autoReconnect=true&useSSL=false";
    private static final String DB_USERNAME ="sp510";
    private static final String DB_PASS ="iit1";

    //  For Header
    @FXML private Label profileLabel;
    @FXML private BorderPane rootPane;
    @FXML private MenuItem profileNameItem;

    // For Dashboard statistics
    @FXML private Label totalEmployeesLabel;
    @FXML private Label totalAdminsLabel;

    // For View containers
    @FXML private VBox dashboardBox;
    @FXML private VBox employeeBox;
    @FXML private VBox adminBox;
    @FXML private VBox alertsBox;
    
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Number> userIdColumn;
    @FXML private TableColumn<User, String> userEmailColumn;
    @FXML private TableColumn<User, String> userDeptColumn;

    // UI Components - Admin table
    @FXML private TableView<AdminUser> adminTable;
    @FXML private TableColumn<AdminUser, Number> adminIdColumn;
    @FXML private TableColumn<AdminUser, String> adminEmailColumn;
    @FXML private TableColumn<AdminUser, String> adminManagerColumn;

    // UI Components - Alerts table
    @FXML private TableView<Ticket> alertsTable;
    @FXML private TableColumn<Ticket, String>alertIdColumn;
    @FXML private TableColumn<Ticket, String>alertUserColumn;
    @FXML private TableColumn<Ticket, String>alertTypeColumn;
    @FXML private TableColumn<Ticket, String>alertSeverityColumn;
    @FXML private TableColumn<Ticket, String>alertStatusColumn;
    @FXML private TableColumn<Ticket, String>alertTimeColumn;
    @FXML private TableColumn<Ticket, String>alertAssignedColumn;

    // Profile menu and dragging support
    private ContextMenu profileMenu;
    private double mouseX,mouseY;

    //Initialize manager dashboard automatically called when FXML loads 
    @FXML
    private void initialize()
    {
        setupDraggableWindow();
        setupAccountMenu();
        configureAllTables();
        refreshStatistics();
        displayDashboardView();
    }

    // Navigate to employee activities view
     
    @FXML
    private void goEmployeeActivities()
    {
        switchToView(employeeBox);
        fetchEmployeeData();
    }

    // Navigate to admin details view
     
    @FXML
    private void goAdminDetails()
    {
        switchToView(adminBox);
        fetchAdminData();
    }

    // Navigate to alerts view
     
    @FXML
    private void goAlerts() {
        switchToView(alertsBox);
        fetchAlertData();
    }

    // Open survey window for suspicious activity overview
  
    @FXML
    private void openSurvey() 
    {
        try 
        {
            javafx.fxml.FXMLLoader fxmlLoader =  new javafx.fxml.FXMLLoader(getClass().getResource("/views/survey.fxml"));
            javafx.scene.Parent surveyView = fxmlLoader.load();

            javafx.stage.Stage surveyStage = new javafx.stage.Stage();
            surveyStage.setTitle("Suspicious Activity Overview");
            surveyStage.setScene(new javafx.scene.Scene(surveyView));
            surveyStage.initOwner(Main.stage);
            surveyStage.show();

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    // Navigate to dashboard view
     
    @FXML
    private void goDashboard()
    {
        displayDashboardView();
    }

    //account dropdown menu with logout option
     
    private void setupAccountMenu() {
        profileMenu = new ContextMenu();

        profileNameItem = new MenuItem("Manager Profile");
        MenuItem logoutOption = new MenuItem("Logout");

        logoutOption.setOnAction(evt -> performLogout());

        profileMenu.getItems().addAll(profileNameItem, logoutOption);
    }

    // Show profile menu when profile label is clicked
     
    @FXML
    private void showProfileMenu(MouseEvent evt) {
        if (profileMenu != null) {
            profileMenu.show(profileLabel, Side.BOTTOM, 0, 5);
        }
    }

    // Handle logout - return to login screen
     
    private void performLogout() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/views/login.fxml"));
            javafx.scene.Parent loginView = fxmlLoader.load();

            Main.stage.setScene(new javafx.scene.Scene(loginView));
            Main.stage.setTitle("Login");
            Main.stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Enable window dragging functionality
     
    private void setupDraggableWindow() 
    {
        if (rootPane == null) return;

        rootPane.setOnMousePressed(evt -> {
            mouseX = evt.getSceneX();
            mouseY = evt.getSceneY();
        });

        rootPane.setOnMouseDragged(evt -> {
            Main.stage.setX(evt.getScreenX()-mouseX);
            Main.stage.setY(evt.getScreenY()-mouseY);
        });
    }

    // Display dashboard view
     
    private void displayDashboardView() {
        switchToView(dashboardBox);
    }

    // Switch between different views (dashboard, employees, admins, alerts)
     
    private void switchToView(VBox viewToShow) {
        hideAllViews();

        if (viewToShow != null) {
            viewToShow.setVisible(true);
            viewToShow.setManaged(true);
        }
    }

    
    private void hideAllViews() {
        hideView(dashboardBox);
        hideView(employeeBox);
        hideView(adminBox);
        hideView(alertsBox);
    }

    private void hideView(VBox view) {
        if (view != null) {
            view.setVisible(false);
            view.setManaged(false);
        }
    }

    
    private void configureAllTables() {
        setupEmployeeTable();
        setupAdminTable();
        setupAlertsTable();
    }

   
    private void setupEmployeeTable() {
        if (userTable != null) {
            userIdColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getEmployeeId()));
            userEmailColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getEmail()));
            userDeptColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getDepartment()));
        }
    }

  
    private void setupAdminTable() {
        if (adminTable != null) {
            adminIdColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getAdminId()));
            adminEmailColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getEmail()));
            adminManagerColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getAssignedTo()));
        }
    }

    // Configure alerts table column bindings
     
    private void setupAlertsTable() {
        if (alertsTable != null) {
            alertIdColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getTicketId()));
            alertUserColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getUserEmail()));
            alertTypeColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getActivityType()));
            alertSeverityColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getSeverity()));
            alertStatusColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getStatus()));
            alertTimeColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getActivityTime()));
            alertAssignedColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getAssignedTo()));
        }
    }

    //Update dashboard statistics (total employees and admins)
 
    private void refreshStatistics() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL,DB_USERNAME,DB_PASS);
             Statement stmt = conn.createStatement()) {

            ResultSet empResults = stmt.executeQuery("SELECT COUNT(*) FROM employee");
            if (empResults.next() && totalEmployeesLabel != null) {
                totalEmployeesLabel.setText(empResults.getString(1));
            }

            ResultSet adminResults = stmt.executeQuery("SELECT COUNT(*) FROM admin");
            if (adminResults.next() && totalAdminsLabel != null) {
                totalAdminsLabel.setText(adminResults.getString(1));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Load employee data from database
    
//    private void fetchEmployeeData() {
//        ObservableList<User> userList = FXCollections.observableArrayList();
//
//        String query = "SELECT employee_id, first_name, last_name, department FROM employee";
//
//        try (Connection conn = DriverManager.getConnection(DATABASE_URL,DB_USERNAME,DB_PASS);
//             Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery(query)) {
//
//            while (rs.next()) {
//            	User user = new User(
//                        rs.getString("first_name"),    
//                        rs.getString("last_name"),    
//                        null,          
//                        null,                     
//                        null,
//                        null,
//                        rs.getInt("employee_id"),      
//                        rs.getString("department")    
//                    );
//                userList.add(user);
//            }
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
////       userTable.setItems(userList);
//        if (userTable != null) {
//            userTable.setItems(userList);
//        }
//    }

    private void fetchEmployeeData() {
        ObservableList<User> userList = FXCollections.observableArrayList();

       
        String query = "SELECT employee_id, email, department FROM employee";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL, DB_USERNAME, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {

                User user = new User(
                    null,                             
                    null,                            
                    rs.getString("email"),             
                    "Employee",                      
                    null,                              
                    null,                              
                    rs.getInt("employee_id"),          
                    rs.getString("department")        
                );

                userList.add(user);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (userTable != null) {
            userTable.setItems(userList);
        }
    }
    // Load admin data from database
     
    private void fetchAdminData() {
        ObservableList<AdminUser> adminList =FXCollections.observableArrayList();

        String query = "SELECT admin_id, email FROM admin";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL,DB_USERNAME,DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet results = stmt.executeQuery(query)) {

            while (results.next()) {
                String email = results.getString("email");
                String assignedManager = determineManagerAssignment(email);

                AdminUser admin = new AdminUser(
                    results.getInt("admin_id"),
                    email,
                    assignedManager
                );
                adminList.add(admin);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        adminTable.setItems(adminList);
    }

    // Determine which manager is assigned to an admin based on email
    private String determineManagerAssignment(String email) {
        if (email.equalsIgnoreCase("admin1@gmail.com")) {
            return "Manager 1";
        } else if (email.equalsIgnoreCase("admin2@gmail.com")) {
            return "Manager 2";
        } else {
            return "Unassigned";
        }
    }

    //Add new admin - shows input dialogs for admin details
    
    @FXML
    private void addAdmin() {
        String email = promptUserInput("Add Admin", "Create a new admin", "Admin email:");
        if (email == null) return;

        String firstName = promptUserInput("Add Admin", null, "First name:");
        if (firstName == null) return;

        String lastName = promptUserInput("Add Admin", null, "Last name:");
        if (lastName == null) return;

        if (isEmailDuplicate(email)) {
            showErrorDialog("Duplicate Email", "An admin with this email already exists.");
            return;
        }

        boolean success = insertAdminToDatabase(email, firstName, lastName);

        if (success) {
            fetchAdminData();
            refreshStatistics();
        }
    }

   
    private String promptUserInput(String title, String header, String prompt) {
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle(title);
        inputDialog.setHeaderText(header);
        inputDialog.setContentText(prompt);

        Optional<String> result = inputDialog.showAndWait();

        if (!result.isPresent() || result.get().trim().isEmpty()) {
            return null;
        }

        return result.get().trim();
    }

    // Check if email already exists in admin table
 
    private boolean isEmailDuplicate(String email) {
        String checkQuery = "SELECT COUNT(*) FROM admin WHERE email = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL, DB_USERNAME, DB_PASS);
             PreparedStatement prep = conn.prepareStatement(checkQuery)) {

            prep.setString(1, email);
            ResultSet results = prep.executeQuery();

            if (results.next() && results.getInt(1) > 0) {
                return true;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

   // Insert admin in to database
    private boolean insertAdminToDatabase(String email, String firstName, String lastName) {
        String defaultPass = "admin123";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL, DB_USERNAME, DB_PASS)) {

            int newAdminId = getNextAdminId(conn);

            String insertQuery = "INSERT INTO admin " +
                                "(admin_id, first_name, last_name, email, password_hash) " +
                                "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement prep = conn.prepareStatement(insertQuery)) {
                prep.setInt(1, newAdminId);
                prep.setString(2, firstName);
                prep.setString(3, lastName);
                prep.setString(4, email);
                prep.setString(5, defaultPass);
                prep.executeUpdate();
                return true;
            }

        } catch (Exception ex) {
            showErrorDialog("Database Error", "Could not save admin. Please try again.");
            ex.printStackTrace();
            return false;
        }
    }

    private int getNextAdminId(Connection conn) throws Exception {
        int nextId = 1;

        try (Statement stmt = conn.createStatement();
             ResultSet results = stmt.executeQuery(
                 "SELECT COALESCE(MAX(admin_id),0) + 1 AS next_id FROM admin")) {

            if (results.next()) {
                nextId = results.getInt("next_id");
            }
        }

        return nextId;
    }

    // Delete selected admin from table and database
     
    @FXML
    private void deleteSelectedAdmin() {
        AdminUser selectedAdmin = adminTable.getSelectionModel().getSelectedItem();

        if (selectedAdmin == null) {
            return;
        }

        boolean confirmed = showDeleteConfirmation(selectedAdmin.getEmail());

        if (confirmed) {
            boolean success = removeAdminFromDatabase(selectedAdmin.getAdminId());

            if (success) {
                fetchAdminData();
                refreshStatistics();
            }
        }
    }

    
    private boolean showDeleteConfirmation(String adminEmail) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Admin");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Delete admin: " + adminEmail + "?");

        Optional<ButtonType> response = confirmDialog.showAndWait();

        return response.isPresent() && response.get() == ButtonType.OK;
    }

    //Remove admin from database
   
     
    private boolean removeAdminFromDatabase(int adminId) {
        String deleteQuery = "DELETE FROM admin WHERE admin_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL, DB_USERNAME, DB_PASS);
             PreparedStatement prep = conn.prepareStatement(deleteQuery)) {

            prep.setInt(1, adminId);
            prep.executeUpdate();
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
//Load alert
    private void fetchAlertData() {
        ObservableList<Ticket> alertList = FXCollections.observableArrayList();

        String query = "SELECT alert_id, email, activity_type, severity, status, " +
                      "activity_time, assigned_to, file_name " +
                      "FROM suspicious_activity ORDER BY activity_time DESC";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL, DB_USERNAME, DB_PASS);
             PreparedStatement prep = conn.prepareStatement(query);
             ResultSet results = prep.executeQuery()) {

            while (results.next()) {
                Ticket alert = new Ticket(
                    "T-" + results.getInt("alert_id"),
                    results.getString("email"),
                    results.getString("activity_type"),
                    results.getString("severity"),
                    results.getString("status"),
                    results.getString("activity_time"),
                    results.getString("assigned_to"),
                    results.getString("file_name")
                );
                alertList.add(alert);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        alertsTable.setItems(alertList);
    }

   
    public void setManagerDetails(String firstName, String lastName) {
        if (firstName == null || lastName == null || lastName.isEmpty()) {
            return;
        }

        String displayName = firstName + "." + lastName.charAt(0);

        if (profileNameItem != null) {
            profileNameItem.setText(displayName);
        }

        System.out.println("Manager logged in: " + displayName);
    }

    //show error dialouge
    private void showErrorDialog(String title, String message) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle(title);
        errorAlert.setHeaderText(null);
        errorAlert.setContentText(message);
        errorAlert.showAndWait();
    }
}