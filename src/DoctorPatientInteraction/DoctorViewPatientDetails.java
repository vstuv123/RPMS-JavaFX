package DoctorPatientInteraction;

import Connection.Conn;
import UserManagement.Patient;
import UserManagement.SessionStorage;
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
import java.util.ArrayList;

public class DoctorViewPatientDetails extends Application {
    private TableView table;
    private ComboBox<String> cbPatientId;
    private final long assignedTo = SessionStorage.loggedInUser.getUnique_id(); // Get the logged-in doctor's ID

    @Override
    public void start(Stage primaryStage) {
        // Label for the heading of the screen
        Label heading = new Label("Search by Patient ID");
        heading.setFont(new Font("Arial", 14));

        // ComboBox for selecting the patient ID
        cbPatientId = new ComboBox<>();
        cbPatientId.setMinWidth(110);
        loadIds(); // Load patient IDs associated with the doctor

        // Set up the top section with the heading and ComboBox
        HBox topBox = new HBox(10, heading, cbPatientId);
        topBox.setPadding(new Insets(10));
        topBox.setAlignment(Pos.CENTER_LEFT);

        // Table to display the patient details
        table = new TableView<>();
        setupTable();  // Set up table columns
        loadAllRecords(); // Load all patient records for the doctor

        // Button to trigger search based on selected patient ID
        Button searchBtn = new Button("Search");
        searchBtn.setOnAction(e -> searchRecord()); // Perform search when clicked

        // Button to trigger print action
        Button printBtn = new Button("Print");
        printBtn.setOnAction(e -> {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(table.getScene().getWindow())) {
                boolean success = job.printPage(table); // Print the table
                if (success) {
                    job.endJob();
                }
            }
        });

        // Button to cancel and close the window
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> primaryStage.close());

        // Buttons are placed horizontally at the bottom
        HBox buttonBox = new HBox(10, searchBtn, printBtn, cancelBtn);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        // Setting up the main layout
        VBox root = new VBox(10, topBox, buttonBox, table);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: white;");

        // Set up the scene and show the window
        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("View Patient Details");
        primaryStage.show();
    }

    // Method to load the patient IDs assigned to the current doctor
    private void loadIds() {
        String query = "select id from patient where assignedTo = " + assignedTo;
        try (Conn conn = new Conn(); ResultSet rs = conn.runQuery(query)) {
            while (rs.next()) {
                cbPatientId.getItems().add(rs.getString("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Print any SQL exceptions to console
        }
    }

    // Method to load all records of patients assigned to the doctor
    @SuppressWarnings("unchecked")
    private void loadAllRecords() {
        ObservableList<Patient> records = FXCollections.observableArrayList();
        ArrayList<Long> ids = new ArrayList<>();

        String query = "select * from patient where assignedTo = " + assignedTo;
        try (Conn conn = new Conn(); ResultSet rs = conn.runQuery(query)) {
            while (rs.next()) {
                records.add(new Patient(Long.parseLong(rs.getString("id")), rs.getString("name"),
                        rs.getString("email"), rs.getString("contactNumber"), rs.getString("dob"), rs.getString("role"), rs.getString("address")));
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Print any SQL exceptions to console
        }
        table.setItems(records); // Populate the table with the fetched records
    }

    // Method to search for a record based on the selected patient ID
    @SuppressWarnings("unchecked")
    private void searchRecord() {
        long id = Long.parseLong(cbPatientId.getValue()); // Get the patient ID from ComboBox
        ObservableList<Patient> records = FXCollections.observableArrayList();
        String query = "select * from patient where id = " + id; // Query for the selected patient ID

        try (Conn conn = new Conn(); ResultSet rs = conn.runQuery(query)) {
            while (rs.next()) {
                records.add(new Patient(Long.parseLong(rs.getString("id")), rs.getString("name"),
                        rs.getString("email"), rs.getString("contactNumber"), rs.getString("dob"), rs.getString("role"), rs.getString("address")));
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Print any SQL exceptions to console
        }
        table.setItems(records); // Set the table's item to the search result
    }

    // Utility method to create columns for the table
    private <T> TableColumn<T, ?> createColumn(String title, String property) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }

    // Method to set up the table columns
    @SuppressWarnings("unchecked")
    private void setupTable() {
        table.getColumns().addAll(
                createColumn("id", "unique_id"),
                createColumn("name", "name"),
                createColumn("email", "email"),
                createColumn("contactNumber", "contactNumber"),
                createColumn("dob", "dob"),
                createColumn("address", "address")
        );
    }
}