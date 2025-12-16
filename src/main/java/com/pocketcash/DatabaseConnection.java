package com.pocketcash;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/pocketcash"; // your DB name
    private static final String USER = "root";  // default XAMPP user
    private static final String PASS = "";      // default XAMPP password is empty

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // load driver
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // returns null if connection fails
        }
    }
}
