package Connections;

import java.sql.Connection;
import java.sql.SQLException;

public class Testdb {
    public static void main(String[] args) {
    	
    	// create a db connect object
        DBConnect db = new DBConnect();
        
        // test for connection
        try (Connection conn = db.connect()) {
        	
        	// Not null - meaning db connected 
            if (conn != null) {
                System.out.println("Database connection successful!");
            } else {
                System.out.println(" Connection returned null!");
            }
        } 
        catch (SQLException e) {
        	// any errors while connecting 
            System.out.println(" Connection failed!");
            e.printStackTrace();
        }
    }
}