package controllers;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import models.User;
import application.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import Authorization.Auth;

public class LoginController {

    private static final String DB_URL =
            "jdbc:mysql://www.papademas.net:3307/510sp?autoReconnect=true&useSSL=false";
    private static final String DB_USER = "sp510";
    private static final String DB_PASS = "iit1";

    // track failed employee logins
    private static final Map<String, Integer> loginAttempts = new HashMap<>();
    private static final int MAX_ATTEMPTS = 3;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleField;
    @FXML private Label lblError;
    
    private final Auth auth = new Auth();

    @FXML
    public void login() {

        lblError.setText("");

        String email = usernameField.getText();
        String password = passwordField.getText();
        String role = roleField.getValue();

        if (email == null || email.trim().isEmpty()) {
            lblError.setText("Username cannot be empty");
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            lblError.setText("Password cannot be empty");
            return;
        }

        if (role == null || role.isEmpty()) {
            lblError.setText("Please select a role");
            return;
        }

        email = email.trim();
        String key = email.toLowerCase();

        switch (role.toLowerCase()) {

            case "manager":
                handleManagerLogin(email, password);
                break;

            case "admin":
                handleAdminLogin(email, password);
                break;

            case "employee":
                handleEmployeeLogin(email, key, password);
                break;

            default:
                lblError.setText("Invalid role selected");
        }
    }

    // manager login

    private void handleManagerLogin(String email, String password) {

        ManagerDetails manager = authenticateManager(email, password);

        if (manager != null) {
            openManagerDashboard(manager);
        } else {
            lblError.setText("Invalid manager credentials");
            logSuspicious(email, "Login Failure", "Medium", null);
        }
    }

    private ManagerDetails authenticateManager(String email, String password) {

        String sql =
                "SELECT first_name, last_name FROM manager " +
                "WHERE email = ? AND password_hash = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new ManagerDetails(
                        rs.getString("first_name"),
                        rs.getString("last_name")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // admin login

    private void handleAdminLogin(String email, String password) {

        if (authenticateAdmin(email, password)) {
            openAdminDashboard(email);
        } else {
            lblError.setText("Invalid admin credentials");
            logSuspicious(email, "Login Failure", "Medium", null);
        }
    }

    private boolean authenticateAdmin(String email, String password) {

        String sql =
                "SELECT 1 FROM admin WHERE email = ? AND password_hash = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            return ps.executeQuery().next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //employee login

    private void handleEmployeeLogin(String email, String key, String password) {

        if (authenticateEmployee(email, password)) {
            loginAttempts.remove(key);
            auth.updateLastLogin(email); 
            openEmployeeDashboard(email);
        } else {
            int count = loginAttempts.getOrDefault(key, 0) + 1;
            loginAttempts.put(key, count);

            lblError.setText("Invalid employee credentials (Attempt " + count + ")");

            if (count == MAX_ATTEMPTS) {
                logEmployeeMultipleFailures(email);
            }
        }
    }

    private boolean authenticateEmployee(String email, String password) {

        String sql =
                "SELECT 1 FROM employee WHERE email = ? AND password_hash = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            return ps.executeQuery().next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Suspicious activity

    private void logSuspicious(String email, String type, String severity, String fileName) {

        String sql =
                "INSERT INTO suspicious_activity " +
                "(employee_id, email, activity_type, severity, file_name, status, activity_time) " +
                "VALUES (NULL, ?, ?, ?, ?, 'Open', NOW())";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, type);
            ps.setString(3, severity);
            ps.setString(4, fileName);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void logEmployeeMultipleFailures(String email) {

        String sql =
                "INSERT INTO suspicious_activity " +
                "(employee_id, email, activity_type, severity, status, activity_time) " +
                "VALUES ((SELECT employee_id FROM employee WHERE email = ?), ?, " +
                "'Multiple Failed Login', 'Medium', 'Open', NOW())";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, email);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //  for navigation

    private void openManagerDashboard(ManagerDetails manager) throws RuntimeException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/manager_dashboard.fxml"));
            Parent root = loader.load();

            ManagerController ctrl = loader.getController();
            ctrl.setManagerDetails(manager.firstName, manager.lastName);

            Main.stage.setScene(new Scene(root));
            Main.stage.setTitle("Manager Dashboard");
            Main.stage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
//Admin dashboard
    private void openAdminDashboard(String email) throws RuntimeException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin_dashboard.fxml"));
            Parent root = loader.load();

            AdminController ctrl = loader.getController();
            ctrl.setAdminUser(email);

            Main.stage.setScene(new Scene(root));
            Main.stage.setTitle("Admin Dashboard");
            Main.stage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    
    private void openEmployeeDashboard(String email) throws RuntimeException {
        try {
            FXMLLoader loader =
                new FXMLLoader(getClass().getResource("/views/employee_dashboard.fxml"));
            Parent root = loader.load();

            EmployeeController ctrl = loader.getController();

            User loggedInUser = loadEmployeeUser(email);

            if (loggedInUser == null) {
                throw new RuntimeException("Employee not found: " + email);
            }

            ctrl.initData(loggedInUser);  

            Main.stage.setScene(new Scene(root));
            Main.stage.setTitle("Employee Dashboard");
            Main.stage.show();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
 // Creates User object for EmployeeController
    private User loadEmployeeUser(String email) {

        String sql =
            "SELECT employee_id, first_name, last_name, department, " +
            "last_login, last_logout " +
            "FROM employee WHERE email = ?";

        try (Connection conn =
                 DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new User(
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    email,
                    "Employee",
                    rs.getString("last_login"),
                    rs.getString("last_logout"),
                    rs.getInt("employee_id"),
                    rs.getString("department")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }



    // Helper function

    private static class ManagerDetails {
        final String firstName;
        final String lastName;

        ManagerDetails(String f, String l) {
            firstName = f;
            lastName = l;
        }
    }
}
