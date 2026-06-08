package controllers;

import application.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.control.TextArea;
import models.FileAccessConfig;
import models.Ticket;



import java.sql.*;

public class AdminController {

    // DB config
    private static final String DB_URL ="jdbc:mysql://www.papademas.net:3307/510sp?autoReconnect=true&useSSL=false";
    private static final String DB_USER ="sp510";
    private static final String DB_PASS ="iit1";

    // logged-in admin
    private String currentAdmin;

    // window drag
    private double mouseX,mouseY;

    // top bar
    @FXML private BorderPane rootPane;
    @FXML private HBox headerBar;
    @FXML private Label profileLabel;
    private ContextMenu accountMenu;

    // counters
    @FXML private Label openTicketsLabel;
    @FXML private Label inProgressTicketsLabel;
    @FXML private Label closedTicketsLabel;

    // filters
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> severityFilter;
    @FXML private TextField searchField;

    // table
    @FXML private TableView<Ticket> ticketTable;
    @FXML private TableColumn<Ticket,String>ticketIdColumn;
    @FXML private TableColumn<Ticket,String>userColumn;
    @FXML private TableColumn<Ticket,String>typeColumn;
    @FXML private TableColumn<Ticket,String>severityColumn;
    @FXML private TableColumn<Ticket,String>statusColumn;
    @FXML private TableColumn<Ticket,String>createdAtColumn;
    @FXML private TableColumn<Ticket,String>assignedToColumn;

    private final ObservableList<Ticket> allTickets =FXCollections.observableArrayList();

    // status update
    @FXML private ComboBox<String> statusUpdateCombo;

    // called after login
    public void setAdminUser(String email)
    {
        currentAdmin =email;
        fetchTickets();
        updateCounters();
    }

    @FXML
    private void initialize() 
    {
        makeWindowDraggable();
        buildProfileMenu();
        setupFilters();
        bindTableColumns();
        setupStatusCombo();
        attachFilterHandlers();
    }

    private void buildProfileMenu()
    {
        accountMenu = new ContextMenu();

        MenuItem role = new MenuItem("Admin");
        MenuItem logout = new MenuItem("Logout");
        logout.setOnAction(e -> logout());

        accountMenu.getItems().addAll(role, logout);
    }

    @FXML
    private void showProfileMenu(MouseEvent e)
    {
        accountMenu.show(profileLabel, Side.BOTTOM, 0, 5);
    }

    private void logout() 
    {
        try
        {
            Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/views/login.fxml"));
            Main.stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void makeWindowDraggable()
    {
        rootPane.setOnMousePressed(e -> {
        	mouseX = e.getSceneX();
            mouseY = e.getSceneY();
        });

        rootPane.setOnMouseDragged(e -> {
            Main.stage.setX(e.getScreenX()- mouseX);
            Main.stage.setY(e.getScreenY()- mouseY);
        });
    }

    private void setupFilters()
    {
        statusFilter.getItems().setAll("All","Open","In-Progress","Closed");
        severityFilter.getItems().setAll("All","Low","Medium","High");
        statusFilter.setValue("All");
        severityFilter.setValue("All");
    }

    private void attachFilterHandlers() {
        statusFilter.setOnAction(e->applyFilters());
        severityFilter.setOnAction(e->applyFilters());
        searchField.textProperty().addListener((a,b,c)->applyFilters());
    }

    private void bindTableColumns() {
        ticketIdColumn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getTicketId()));
        userColumn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getUserEmail()));
        typeColumn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getActivityType()));
        severityColumn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getSeverity()));
        statusColumn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));
        createdAtColumn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getActivityTime()));
        assignedToColumn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getAssignedTo()));
    }

    private void setupStatusCombo() 
    {
        statusUpdateCombo.getItems().setAll("Open","In-Progress","Closed");
        statusUpdateCombo.setValue("Open");
    }

    private void fetchTickets() 
    {
        allTickets.clear();

        String sql = "SELECT * FROM suspicious_activity ORDER BY activity_time DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL,DB_USER,DB_PASS);
             ResultSet rs = conn.createStatement().executeQuery(sql)) {

            while (rs.next())
            {
                allTickets.add(new Ticket(
                        "T-" + rs.getInt("alert_id"),
                        rs.getString("email"),
                        rs.getString("activity_type"),
                        rs.getString("severity"),
                        rs.getString("status"),
                        rs.getString("activity_time"),
                        rs.getString("assigned_to"),
                        rs.getString("file_name")
                ));
            }

        } 
        catch (Exception e)
        {
            e.printStackTrace();
        }

        applyFilters();
    }

    private void applyFilters()
    {
        ObservableList<Ticket> filtered = FXCollections.observableArrayList();

        for (Ticket t : allTickets) {
            boolean okStatus =statusFilter.getValue().equals("All")
                    || t.getStatus().equalsIgnoreCase(statusFilter.getValue());

            boolean okSeverity = severityFilter.getValue().equals("All")
                    || t.getSeverity().equalsIgnoreCase(severityFilter.getValue());

            boolean okSearch =searchField.getText().isBlank()
                    || t.getUserEmail().toLowerCase().contains(searchField.getText().toLowerCase());

            if (okStatus && okSeverity && okSearch) 
            {
                filtered.add(t);
            }
        }

        ticketTable.setItems(filtered);
    }

    private void updateCounters()
    {
        updateCounter(openTicketsLabel,"Open");
        updateCounter(inProgressTicketsLabel,"In-Progress");
        updateCounter(closedTicketsLabel,"Closed");
    }

    private void updateCounter(Label label, String status)
    {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM suspicious_activity WHERE status=?")) {

            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) label.setText(rs.getString(1));

        } catch (Exception e) {
            label.setText("0");
        }
    }

    @FXML
    private void refreshTickets() {
        fetchTickets();
        updateCounters();
    }

    @FXML
    private void updateSelectedTicketStatus() 
    {

        Ticket t = ticketTable.getSelectionModel().getSelectedItem();
        if (t == null) return;

        String newStatus = statusUpdateCombo.getValue();
        if (newStatus == null) return;

        String sql =
                "UPDATE suspicious_activity SET status=?, assigned_to=? WHERE alert_id=?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setString(2, currentAdmin);
            ps.setInt(3, Integer.parseInt(t.getTicketId().substring(2)));
            ps.executeUpdate();

            t.setStatus(newStatus);
            ticketTable.refresh();
            updateCounters();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showFileAccess() {

        StringBuilder sb = new StringBuilder();

        FileAccessConfig.FILE_ACCESS_MAP.forEach((file, users) ->
                sb.append(file)
                  .append(" -> ")
                  .append(String.join(", ", users))
                  .append("\n"));

        TextArea area = new TextArea(sb.toString());
        area.setEditable(false);

        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("File Access");
        a.setHeaderText("Files and allowed employees");
        a.getDialogPane().setContent(area);
        a.showAndWait();
        
        
    }
     
    
    @FXML
    private void openEmployeeList() 
    {
        try 
        {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/views/employee_list.fxml"));
            Parent root = loader.load();

            EmployeeListController controller = loader.getController();
            controller.setAdminEmail(currentAdmin);

            Stage stage = new Stage();
            stage.setTitle("Employee List");
            stage.setScene(new Scene(root));
            stage.initOwner(Main.stage);
            stage.show();

        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }


}
