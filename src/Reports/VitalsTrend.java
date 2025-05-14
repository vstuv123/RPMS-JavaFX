package Reports;

import Connection.Conn;
import HealthDataHandling.VitalSign;
import UserManagement.SessionStorage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VitalsTrend {

    private TableView<VitalSign> tableView;  // TableView to display vital sign data

    // Method to start and set up the Vitals Trend report view
    public void start(BorderPane reportArea, ComboBox<String> patientSelector) {
        tableView = new TableView<>();  // Initialize the TableView for vital signs

        // Create columns for displaying different vital signs
        TableColumn<VitalSign, String> heartRateCol = new TableColumn<>("Heart Rate");
        heartRateCol.setCellValueFactory(new PropertyValueFactory<>("heartRate"));

        TableColumn<VitalSign, String> bpCol = new TableColumn<>("Blood Pressure");
        bpCol.setCellValueFactory(new PropertyValueFactory<>("formattedBloodPressure"));

        TableColumn<VitalSign, String> tempCol = new TableColumn<>("Temperature");
        tempCol.setCellValueFactory(new PropertyValueFactory<>("temperature"));

        TableColumn<VitalSign, String> oxygenCol = new TableColumn<>("Oxygen Level");
        oxygenCol.setCellValueFactory(new PropertyValueFactory<>("oxygenLevel"));

        TableColumn<VitalSign, String> weightCol = new TableColumn<>("Weight");
        weightCol.setCellValueFactory(new PropertyValueFactory<>("weight"));

        TableColumn<VitalSign, String> heightCol = new TableColumn<>("Height");
        heightCol.setCellValueFactory(new PropertyValueFactory<>("height"));

        TableColumn<VitalSign, String> dateCol = new TableColumn<>("Recorded At");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        // Add the columns to the TableView
        tableView.getColumns().addAll(heartRateCol, bpCol, tempCol, oxygenCol, weightCol, heightCol, dateCol);
        reportArea.setCenter(tableView);  // Set the TableView as the central content in the report area

        // If a patient selector is provided (for doctors), set up an action to load data based on selected patient
        if (patientSelector != null) {
            // When a patient is selected, load the vital signs data for the selected patient
            patientSelector.setOnAction(e -> {
                String selectedPatient = patientSelector.getValue();
                if (selectedPatient != null) {
                    loadVitalsData(Long.parseLong(selectedPatient));  // Load vitals data for the selected patient
                }
            });

            // If a patient is already selected, load their vital signs data initially
            if (patientSelector.getValue() != null) {
                loadVitalsData(Long.parseLong(patientSelector.getValue()));
            }
        } else {
            // If no patient selector (for general users), load the logged-in user's vitals data
            loadVitalsData(SessionStorage.loggedInUser.getUnique_id());
        }
    }

    // Method to load vitals data from the database and populate the TableView
    private void loadVitalsData(Long patientId) {
        ObservableList<VitalSign> data = FXCollections.observableArrayList();  // Observable list to hold the data

        // SQL query to retrieve vital sign data for a specific patient
        try (Conn conn = new Conn();
             ResultSet rs = conn.runQuery(
                     "SELECT * FROM vitals WHERE patient_id = " + patientId + " ORDER BY timestamp DESC")) {

            // Iterate through the result set and create VitalSign objects for each row
            while (rs.next()) {
                long patientID = rs.getLong("patient_id");
                int heartRate = rs.getInt("heart_rate");
                int systolic = rs.getInt("blood_pressure_systolic");
                int diastolic = rs.getInt("blood_pressure_diastolic");
                double temp = rs.getDouble("temperature_fahrenheit");
                int oxygen = rs.getInt("oxygen_level");
                double weight = rs.getDouble("weight_pounds");
                double height = rs.getDouble("height_inches");

                // Format the timestamp of when the vital sign was recorded
                LocalDateTime time = rs.getTimestamp("timestamp").toLocalDateTime();
                String formattedTime = time.format(DateTimeFormatter.ofPattern("MMMM d, yyyy hh:mm a"));

                // Add the VitalSign object to the data list
                data.add(new VitalSign(patientID, heartRate, oxygen, temp, systolic, diastolic, weight, height, formattedTime));
            }

        } catch (Exception e) {
            e.printStackTrace();  // Print any exceptions encountered during the database query
        }

        // Set the populated data to the TableView to display the vitals
        tableView.setItems(data);
    }
}