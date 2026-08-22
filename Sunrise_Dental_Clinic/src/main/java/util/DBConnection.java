/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static DBConnection instance;
    private Connection connection;
    private static final String URL ="jdbc:mysql://localhost:3306/sunrise_db"+ "?sslMode=DISABLED";

    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Thushara2828";

    private DBConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
            System.out.println("Database connected successfully!");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                "MySQL JDBC Driver not found. Add mysql-connector-j dependency to pom.xml", e);
        } catch (SQLException e) {
            throw new RuntimeException(
                "Database connection FAILED. Check: MySQL server is running, "
                + "DB_PASSWORD is correct, database 'sunrise_db' exists. "+ e.getMessage(), e);
        }
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not re-establish database connection: " + e.getMessage(), e);
        }
        return connection;
    }
}