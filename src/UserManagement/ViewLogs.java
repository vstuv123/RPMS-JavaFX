package UserManagement;

import Connection.Conn;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class ViewLogs extends Application {
    private TableView<Logs> table; // TableView to display logs
    private DatePicker datePicker; // DatePicker to filter logs by date

    @Override
    public void start(Stage primaryStage) {
        // Label for the search section
        Label heading = new Label("Search by Timestamp");
        heading.setFont(new Font("Arial", 14)); // Set font for the heading

        // DatePicker to select a specific date for searching logs
        datePicker = new DatePicker();

        // HBox to contain the search heading and DatePicker
        HBox topBox = new HBox(10, heading, datePicker);
        topBox.setPadding(new Insets(10)); // Padding for the HBox
        topBox.setAlignment(Pos.CENTER_LEFT); // Align to the left

        // TableView to display the log details
        table = new TableView<>();
        setupTable(); // Setup the table structure
        loadAllRecords(); // Load all records initially

        // Button to search the logs based on the selected date
        Button searchBtn = new Button("Search");
        searchBtn.setOnAction(e -> searchRecord()); // Trigger search when clicked

        // Button to print the current table view
        Button printBtn = new Button("Print");
        printBtn.setOnAction(e -> {
            PrinterJob job = PrinterJob.createPrinterJob(); // Create a print job
            if (job != null && job.showPrintDialog(table.getScene().getWindow())) { // Show print dialog
                boolean success = job.printPage(table); // Print the table content
                if (success) {
                    job.endJob(); // End the print job if successful
                }
            }
        });

        // Button to cancel the current operation and close the window
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> primaryStage.close()); // Close the window when clicked

        // HBox to arrange the buttons horizontally
        HBox buttonBox = new HBox(10, searchBtn, printBtn, cancelBtn);
        buttonBox.setPadding(new Insets(10)); // Padding for the button box
        buttonBox.setAlignment(Pos.CENTER_LEFT); // Align buttons to the left

        // VBox to contain all the components (topBox, buttonBox, and table)
        VBox root = new VBox(10, topBox, buttonBox, table);
        root.setPadding(new Insets(10)); // Padding for the root container
        root.setStyle("-fx-background-color: white;"); // Set background color for the root

        // Set the scene and show the primaryStage
        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Log Details");
        primaryStage.show();
    }

    // Method to load all log records from the database
    @SuppressWarnings("unchecked")
    private void loadAllRecords() {
        ObservableList<Logs> logs = FXCollections.observableArrayList(); // List to hold log records
        try (Conn conn = new Conn();
             ResultSet rs = conn.runQuery("select * from logs")) {

            // Loop through the result set and add logs to the observable list
            while (rs.next()) {
                logs.add(new Logs(rs.getString("user_id"), rs.getString("action_type"),
                        rs.getString("description")));
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Print error if any exception occurs
        }
        table.setItems(logs); // Set the observable list as the table's data source
    }

    // Method to search logs based on the selected date
    @SuppressWarnings("unchecked")
    private void searchRecord() {
        LocalDate date = datePicker.getValue(); // Get the selected date
        ObservableList<Logs> logs = FXCollections.observableArrayList(); // List to hold the search results

        // Query to filter logs based on the selected date
        String query = "select * from logs where DATE(timestamp) ='" + date + "'";

        try (Conn conn = new Conn(); ResultSet rs = conn.runQuery(query)) {
            // Loop through the result set and add the logs to the observable list
            while (rs.next()) {
                logs.add(new Logs(rs.getString("user_id"), rs.getString("action_type"),
                        rs.getString("description")));
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Print error if any exception occurs
        }
        table.setItems(logs); // Set the filtered logs as the table's data source
    }

    // Helper method to create a table column
    private <T> TableColumn<T, ?> createColumn(String title, String property) {
        TableColumn<T, String> column = new TableColumn<>(title); // Create a new column with the given title
        column.setCellValueFactory(new PropertyValueFactory<>(property)); // Set the property for each row in the column
        return column;
    }

    // Method to set up the table structure with columns
    @SuppressWarnings("unchecked")
    private void setupTable() {
        // Add columns to the table for user_id, action_type, and description
        table.getColumns().addAll(
                createColumn("user_id", "userId"),
                createColumn("action_type", "actionType"),
                createColumn("description", "desc")
        );
    }
}