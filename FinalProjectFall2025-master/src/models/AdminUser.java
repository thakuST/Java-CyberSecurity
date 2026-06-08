package models;

public class AdminUser {

    private int adminId;
    private String email;
    private String assignedTo;   // "Manager 1", "Manager 2", etc.

    public AdminUser(int adminId, String email, String assignedTo) {
        this.adminId = adminId;
        this.email = email;
        this.assignedTo = assignedTo;
    }

    public int getAdminId() {
        return adminId;
    }

    public String getEmail() {
        return email;
    }

    public String getAssignedTo() {
        return assignedTo;
    }
}
