package models;

/**
 * Represents a suspicious activity ticket in the system.
 */
public class Ticket {

    private String ticketId;
    private String userEmail;
    private String activityType;
    private String severity;
    private String status;
    private String activityTime;
    private String assignedTo;
    private String fileName;

    // constructor
    public Ticket(String ticketId, String userEmail, String activityType,
                  String severity, String status, String activityTime,
                  String assignedTo, String fileName) {

        this.ticketId = ticketId;
        this.userEmail = userEmail;
        this.activityType = activityType;
        this.severity = severity;
        this.status = status;
        this.activityTime = activityTime;
        this.assignedTo = assignedTo;
        this.fileName = fileName;
    }

    // getters
    public String getTicketId() { return ticketId; }
    public String getUserEmail() { return userEmail; }
    public String getActivityType() { return activityType; }
    public String getSeverity() { return severity; }
    public String getStatus() { return status; }
    public String getActivityTime() { return activityTime; }
    public String getAssignedTo() { return assignedTo; }
    public String getFileName() { return fileName; }

    // setter for updating ticket status
    public void setStatus(String status) { this.status = status; }
}
