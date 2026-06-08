package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class EmployeeListController {

    // DB config
    private static final String DATABASE_URL =
            "jdbc:mysql://www.papademas.net:3307/510sp?autoReconnect=true&useSSL=false";
    private static final String DB_USERNAME = "sp510";
    private static final String DB_PASS = "iit1";


    @FXML private TableView<User> UserTable;
    @FXML private TableColumn<User, Number> idColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> deptColumn;

    @FXML
    private void initialize() {
        bindColumns();
        fetchAllEmployees();   
    }

    private void bindColumns() {

        idColumn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleIntegerProperty(
                        c.getValue().getEmployeeId()));

        emailColumn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getEmail()));

        deptColumn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getDepartment()));
    }

    private void fetchAllEmployees() {

        ObservableList<User> list = FXCollections.observableArrayList();

        String sql =
                "SELECT employee_id, email, department " +
                "FROM employee ORDER BY employee_id";

        try (Connection conn =
                     DriverManager.getConnection(DATABASE_URL, DB_USERNAME, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
            	 list.add(new User(
                         rs.getString("first_name"), // firstName
                         rs.getString("last_name"),   // lastName
                         rs.getString("email"), // email
                         "Employee",         // role
                         rs.getString("last_login"), // lastLogin
                         rs.getString("last_logout"),  // lastLogout
                         rs.getInt("employee_id"), // employeeId
                         rs.getString("department") // department
                     ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        UserTable.setItems(list);
    }

    @FXML
    private void addEmployee() {

        String first = prompt("First name:");
        if (first == null) return;

        String last = prompt("Last name:");
        if (last == null) return;

        String email = prompt("Email:");
        if (email == null) return;

        String dept = prompt("Department:");
        if (dept == null) return;

        String sql =
                "INSERT INTO employee " +
                "(first_name, last_name, email, department, hire_date, manager_id, password_hash) " +
                "VALUES (?, ?, ?, ?, CURDATE(), NULL, ?)";

        try (Connection conn =
                     DriverManager.getConnection(DATABASE_URL, DB_USERNAME, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, first);
            ps.setString(2, last);
            ps.setString(3, email);
            ps.setString(4, dept);
            ps.setString(5, "emp123");

            ps.executeUpdate();
            fetchAllEmployees();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteSelectedEmployee() {

        User user = UserTable.getSelectionModel().getSelectedItem();
        if (user == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setContentText("Delete employee ID: " + user.getEmployeeId());

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK)
            return;

        try (Connection conn =
                     DriverManager.getConnection(DATABASE_URL, DB_USERNAME, DB_PASS);
             PreparedStatement ps =
                     conn.prepareStatement("DELETE FROM employee WHERE employee_id=?")) {

            ps.setInt(1, user.getEmployeeId());
            ps.executeUpdate();
            fetchAllEmployees();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String prompt(String label) {

        TextInputDialog d = new TextInputDialog();
        d.setHeaderText(null);
        d.setContentText(label);

        Optional<String> r = d.showAndWait();
        return r.isPresent() && !r.get().trim().isEmpty()
                ? r.get().trim()
                : null;
    }

	public void setAdminEmail(String currentAdmin)
	{
		// TODO 
		
	}

}
