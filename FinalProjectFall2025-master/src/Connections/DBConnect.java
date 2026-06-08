package Connections;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnect {

    private static final String DB_URL = 
        "jdbc:mysql://www.papademas.net:3307/510sp?autoReconnect=true&useSSL=false";

    private static final String USER = "sp510";
    private static final String PASS = "iit1";

    public Connection connect() {
        Connection conn = null;

        try {
            // Load MySQL driver for non-modular JavaFX projects
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✔ MySQL JDBC Driver loaded.");

            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("✔ Connected to the database!");

        } 
        catch (ClassNotFoundException e) {
            System.err.println("❌ ERROR: MySQL JDBC Driver NOT FOUND.");
            System.err.println("Make sure mysql-connector-j-8.x.x.jar is in:");
            System.err.println("   ✔ Build Path -> Classpath");
            System.err.println("   ✔ Run Configurations -> Classpath");
        }
        catch (SQLException e) {
            System.err.println("❌ DATABASE CONNECTION FAILED.");
            System.err.println("Reason: " + e.getMessage());
        }

        return conn;
    }
}
