package Authorization;

import Connections.DBConnect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;



public class Auth
{
    private static Map<String, Integer> failedAttemptsMap = new HashMap<>();

    public boolean authenticate(String email, String password, String role) {

        String table = "";
        if (role.equals("Admin")) table = "admin";
        else if (role.equals("Manager")) table = "manager";
        else if (role.equals("Employee")) table = "employee";

        String query = "SELECT password_hash, employee_id FROM " + table + " WHERE email=?";

        try (Connection conn = new DBConnect().connect(); PreparedStatement stmt = conn.prepareStatement(query))
        {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next())
            {
                String storedHash = rs.getString("password_hash");
                int employeeId = role.equals("Employee") ? rs.getInt("employee_id") : 0;

                if (storedHash.equals(password))
                {
                    //If successful login, reset failed attempts
                    failedAttemptsMap.remove(email);
                    if (role.equals("Employee")) updateLastLogin(email);
                    return true;
                }
                else {
                    // Failed login
                    if (role.equals("Employee")) {
                        int attempts = failedAttemptsMap.getOrDefault(email, 0) + 1;
                        failedAttemptsMap.put(email, attempts);

                        if (attempts >= 3) {
                            insertSuspiciousActivity(employeeId,
                                    email,
                                    "Medium",
                                    "Multiple Failed Login");
                        }
                    }
                    return false;
                }

            }
            else
            {
                return false;
            }

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }


    public void updateLastLogin(String email)
    {
        String query = "UPDATE employee SET last_login=NOW() WHERE email=?";
        try (Connection conn = new DBConnect().connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void updateLastLogout(String email)
    {
        String query = "UPDATE employee SET last_logout=NOW() WHERE email=?";
        try (Connection conn = new DBConnect().connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getLastLogin(String email)
    {
        String query = "SELECT last_login FROM employee WHERE email=?";
        try (Connection conn = new DBConnect().connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("last_login");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getLastLogout(String email)
    {
        String query = "SELECT last_logout FROM employee WHERE email=?";
        try (Connection conn = new DBConnect().connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("last_logout");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getFullName(String email)
    {
        String query = "SELECT first_name, last_name FROM employee WHERE email=?";
        try (Connection conn = new DBConnect().connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("first_name") + " " + rs.getString("last_name");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public int getFailedAttempts(String email)
    {
        return failedAttemptsMap.getOrDefault(email, 0);
    }

    public int getEmployeeId(String email)
    {
        String query = "SELECT employee_id FROM employee WHERE email=?";
        try (Connection conn = new DBConnect().connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("employee_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void insertSuspiciousActivity(int employeeId, String email, String severity, String activityType)
    {
        String query = "INSERT INTO suspicious_activity (employee_id, email, severity, activity_type, activity_time) " +
                "VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = new DBConnect().connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, employeeId);
            stmt.setString(2, email);
            stmt.setString(3, severity);
            stmt.setString(4, activityType);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void insertSuspiciousActivityFile(int employeeId, String email, String severity, String activityType, String fileName)
    {
        String query = "INSERT INTO suspicious_activity (employee_id, email, severity, activity_type, activity_time, file_name) " +
                "VALUES (?, ?, ?, ?, NOW(), ?)";
        try (Connection conn = new DBConnect().connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, employeeId);
            stmt.setString(2, email);
            stmt.setString(3, severity);
            stmt.setString(4, activityType);
            stmt.setString(5, fileName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //For employee dashboard display
    public String getDepartment(String email)
    {
        String query = "SELECT department FROM employee WHERE email = ?";

        //DBConnect:
        try (Connection conn = new DBConnect().connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next())
            {
                return rs.getString("department");
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

}