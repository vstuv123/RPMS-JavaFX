package Connection;

import resources.ConfigLoader;

import java.sql.*;

public class Conn implements AutoCloseable {
    // Instance variables for connection and statement
    private Connection c;
    private Statement s;

    // Constructor that establishes a database connection
    public Conn() {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish connection using configuration values
            c = DriverManager.getConnection(
                    ConfigLoader.get("mySQLUrl"),
                    ConfigLoader.get("host"),
                    ConfigLoader.get("mySQLPassword"));

            // Create statement object to execute SQL queries
            s = c.createStatement();
        } catch (Exception e) {
            e.printStackTrace();  // Print the exception stack trace if there is an error
        }
    }

    // Method to run a SELECT query and return a ResultSet
    public ResultSet runQuery(String query) throws SQLException {
        return s.executeQuery(query);  // Executes SELECT queries
    }

    // Method to run an INSERT, UPDATE, or DELETE query and return the number of affected rows
    public int runUpdate(String query) throws SQLException {
        return s.executeUpdate(query);  // Executes INSERT, UPDATE, DELETE queries
    }

    // Override the close method from AutoCloseable to close resources
    @Override
    public void close() {
        try {
            // Close the statement and connection if they are not null
            if (s != null) s.close();
            if (c != null) c.close();
        } catch (SQLException e) {
            e.printStackTrace();  // Print the exception stack trace if there is an error closing resources
        }
    }
}