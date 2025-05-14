package DoctorPatientInteraction;

import Connection.Conn;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PatientHistory extends Application {

    private TextField tfPatientId; // TextField to enter patient ID
    private VBox historyContainer; // VBox to contain the history records
    private ScrollPane scrollPane; // ScrollPane to allow scrolling through the records

    @Override
    public void start(Stage primaryStage) {
        // Label for the title/heading of the screen
        Label heading = new Label("Patient History");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 30)); // Set custom font, bold, size 30
        heading.setMaxWidth(Double.MAX_VALUE);
        heading.setAlignment(Pos.CENTER);
        heading.setStyle("-fx-text-fill: #2c3e50;"); // Dark text color for heading

        // TextField for inputting Patient ID
        tfPatientId = new TextField();
        tfPatientId.setPromptText("Enter Patient ID");
        tfPatientId.setStyle("-fx-font-family: Arial; -fx-font-size: 14px; -fx-background-color: #f5f5f5; -fx-border-color: #ccc;"); // Custom styling for TextField

        // Button to fetch the history of the entered patient ID
        Button fetchButton = new Button("Fetch History");
        fetchButton.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-family: Arial; -fx-font-size: 14px;");

        // HBox to hold the TextField and Button in a horizontal layout
        HBox inputBox = new HBox(10, tfPatientId, fetchButton);
        inputBox.setAlignment(Pos.CENTER);

        // VBox to contain the patient's history records (Vitals, Prescriptions, Feedback)
        historyContainer = new VBox(15);
        historyContainer.setPadding(new Insets(20));

        // ScrollPane to wrap the historyContainer allowing vertical scrolling
        scrollPane = new ScrollPane(historyContainer);
        scrollPane.setFitToWidth(false);
        scrollPane.setStyle("-fx-background: white; -fx-border-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Vertical scrollbar as needed
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Horizontal scrollbar as needed
        scrollPane.setVisible(false); // Initially hidden until history is fetched

        // When fetchButton is clicked, fetch the patient's history
        fetchButton.setOnAction(e -> fetchHistory());

        // Main layout with heading, input box (TextField + Button), and ScrollPane
        VBox layout = new VBox(20, heading, inputBox, scrollPane);
        layout.setPadding(new Insets(20));
        VBox.setVgrow(scrollPane, Priority.ALWAYS); // Allow ScrollPane to grow and take remaining space

        // Set up the scene and show the primary stage
        Scene scene = new Scene(layout, 600, 600);
        primaryStage.setTitle("View Patient History");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Method to fetch and display the patient's history from database
    private void fetchHistory() {
        historyContainer.getChildren().clear(); // Clear any previous records

        String patientIdText = tfPatientId.getText(); // Get the Patient ID from the input field

        // If Patient ID is empty, show an alert and exit
        if (patientIdText.isEmpty()) {
            showAlert(AlertType.ERROR, "Patient ID is required.");
            ((Stage) tfPatientId.getScene().getWindow()).close(); // Close the window if no ID is provided
            return;
        }

        // Convert the patient ID to long type
        long patientId = Long.parseLong(patientIdText);
        String query = "select * from vitals where patient_id = " + patientId; // Query to fetch vitals history

        scrollPane.setVisible(true); // Make ScrollPane visible to display results

        try (Conn conn = new Conn(); // Try with resources to handle DB connection
             ResultSet rsVitals = conn.runQuery(query)) {

            // Label for Vitals History section
            Label vitalsLabel = new Label("Vitals History");
            vitalsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20)); // Custom Font for the label
            vitalsLabel.setStyle("-fx-text-fill: #2c3e50;"); // Set color for the label
            historyContainer.getChildren().add(vitalsLabel); // Add label to the history container

            boolean vitalsFound = false; // Flag to check if any vitals records are found
            while (rsVitals.next()) { // Iterate through the vitals records
                vitalsFound = true; // Set vitalsFound to true if records are present
                // Create label to display each vital record
                Label record = new Label("- Heart Rate: " + rsVitals.getInt("heart_rate") + " bpm, "
                        + "Oxygen Level: " + rsVitals.getInt("oxygen_level") + "%, "
                        + "Temperature: " + rsVitals.getDouble("temperature_fahrenheit") + " °F, "
                        + "Blood Pressure: " + rsVitals.getInt("blood_pressure_systolic") + "/" + rsVitals.getInt("blood_pressure_diastolic") + " mmHg, "
                        + "Weight: " + rsVitals.getDouble("weight_pounds") + " lbs, "
                        + "Height: " + rsVitals.getDouble("height_inches") + " inches, "
                        + "Date: " + rsVitals.getTimestamp("timestamp"));
                record.setStyle("-fx-font-family: Arial; -fx-font-size: 14px; -fx-text-fill: #34495e;"); // Styling for record label
                historyContainer.getChildren().add(record); // Add the record to the container
            }

            // If no vitals records were found, show a message
            if (!vitalsFound) {
                Label noVitals = new Label("No vitals found.");
                noVitals.setStyle("-fx-font-family: Arial; -fx-font-size: 14px; -fx-text-fill: #e74c3c;"); // Red text color for error
                historyContainer.getChildren().add(noVitals);
            }

            // Query to fetch prescriptions for the given patient ID
            ResultSet rsPrescriptions = conn.runQuery("select * from prescription where patient_id = " + patientId);
            // Label for Prescriptions section
            Label prescriptionLabel = new Label("Prescriptions");
            prescriptionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20)); // Custom Font for the label
            prescriptionLabel.setStyle("-fx-text-fill: #2c3e50;");
            historyContainer.getChildren().add(prescriptionLabel);

            boolean prescriptionFound = false; // Flag for checking prescriptions
            while (rsPrescriptions.next()) { // Iterate through the prescriptions records
                prescriptionFound = true;
                // Create label to display each prescription record
                Label record = new Label("- " + rsPrescriptions.getString("medicine_name") + " ("
                        + rsPrescriptions.getString("dosage") + "), "
                        + rsPrescriptions.getString("schedule")
                        + " | Instructions: " + rsPrescriptions.getString("instructions")
                        + " | Date Issued: " + rsPrescriptions.getDate("date_issued"));
                record.setStyle("-fx-font-family: Arial; -fx-font-size: 14px; -fx-text-fill: #34495e;");
                historyContainer.getChildren().add(record); // Add prescription record to the container
            }

            // If no prescriptions records were found, show a message
            if (!prescriptionFound) {
                Label noPrescriptions = new Label("No prescriptions found.");
                noPrescriptions.setStyle("-fx-font-family: Arial; -fx-font-size: 14px; -fx-text-fill: #e74c3c;");
                historyContainer.getChildren().add(noPrescriptions);
            }

            // Query to fetch doctor's feedback for the given patient ID
            ResultSet rsFeedback = conn.runQuery("select * from feedback where patient_id = " + patientId);
            // Label for Feedback section
            Label feedbackLabel = new Label("Doctor's Feedback");
            feedbackLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20)); // Custom Font for the label
            feedbackLabel.setStyle("-fx-text-fill: #2c3e50;");
            historyContainer.getChildren().add(feedbackLabel);

            boolean feedbackFound = false; // Flag for checking feedback records
            while (rsFeedback.next()) { // Iterate through feedback records
                feedbackFound = true;
                // Create label to display each feedback record
                Label record = new Label("- " + rsFeedback.getString("notes") + " | Date: " +
                        rsFeedback.getTimestamp("timestamp"));
                record.setStyle("-fx-font-family: Arial; -fx-font-size: 14px; -fx-text-fill: #34495e;");
                historyContainer.getChildren().add(record); // Add feedback record to the container
            }

            // If no feedback records were found, show a message
            if (!feedbackFound) {
                Label noFeedback = new Label("No feedback found.");
                noFeedback.setStyle("-fx-font-family: Arial; -fx-font-size: 14px; -fx-text-fill: #e74c3c;");
                historyContainer.getChildren().add(noFeedback);
            }

        } catch (SQLException se) {
            se.printStackTrace(); // Print any database errors to console
            showAlert(AlertType.ERROR, "Database error: " + se.getMessage()); // Show an alert if there is a database error
        }
    }

    // Method to display an alert with the specified type and message
    private void showAlert(AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null); // No header text
        alert.setContentText(message); // Set content text for the alert
        alert.showAndWait(); // Show the alert and wait for user interaction
    }
}