package models;

public class User
{
    private String firstName, lastName, email, role, lastLogin, lastLogout, department;
    private int employeeId;

    public User(String firstName, String lastName, String email, String role, String lastLogin, String lastLogout, int employeeId, String department)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.lastLogin = lastLogin;
        this.lastLogout = lastLogout;
        this.employeeId = employeeId;
        this.department = department;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getLastLogin() { return lastLogin; }
    public String getLastLogout() { return lastLogout; }
    public int getEmployeeId() { return employeeId; }

    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }
    public void setLastLogout(String lastLogout) { this.lastLogout = lastLogout; }

    public String getFullName() { return firstName + " " + lastName; }

    public String getDepartment()
    {
        return department;
    }
}