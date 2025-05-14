package HealthDataHandling;

import Connection.Conn;
import EmergencyAlertSystem.EmergencyAlert;
import Helper.Helper;
import UserManagement.Patient;
import UserManagement.PatientDashboard;
import UserManagement.SessionStorage;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.concurrent.Task;

import javafx.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.SQLException;

public class UploadVitals extends Application {

    // Reference to the currently logged-in patient
    Patient patient = (Patient) SessionStorage.loggedInUser;
    EmergencyAlert ea = new EmergencyAlert();

    @Override
    public void start(Stage primaryStage) {
        // UI label
        Label instructionLabel = new Label("Upload Your Vitals (CSV Format)");
        instructionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        instructionLabel.setStyle("-fx-text-fill: #2E8B57;");
        instructionLabel.setAlignment(Pos.CENTER);

        // Upload button setup
        Button uploadButton = new Button("Choose CSV File");
        uploadButton.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 18));
        uploadButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8;");
        uploadButton.setPrefWidth(200);
        uploadButton.setPrefHeight(40);

        // File chooser on button click
        uploadButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);

            if (selectedFile != null) {
                processCSV(selectedFile, e);
            }
        });

        // Layout setup
        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(instructionLabel, uploadButton);
        layout.setStyle("-fx-background-color: white;");

        // Scene setup
        Scene scene = new Scene(layout, 500, 300);
        primaryStage.setTitle("Upload Vitals");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Reads and processes CSV file data
    private void processCSV(File file, ActionEvent e) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // Skip header
                    continue;
                }

                // Parse CSV line
                String[] data = line.split(",");
                int heartRate = Integer.parseInt(data[0].trim());
                int oxygenLevel = Integer.parseInt(data[1].trim());
                double temperature = Double.parseDouble(data[2].trim());
                int systolic = Integer.parseInt(data[3].trim());
                int diastolic = Integer.parseInt(data[4].trim());
                double weight = Double.parseDouble(data[5].trim());
                double height = Double.parseDouble(data[6].trim());
                String timestamp = Helper.currentTimestamp();

                // Validate and store or reject
                if (validateVitals(heartRate, oxygenLevel, temperature, systolic, diastolic, weight, height)) {
                    saveToDatabase(heartRate, oxygenLevel, temperature, systolic, diastolic, weight, height, timestamp, e);
                } else {
                    showAlert("Invalid data found. Upload cancelled.");
                    return;
                }
            }
        } catch (Exception se) {
            se.printStackTrace();
            showAlert("Error processing file: " + se.getMessage());
        }
    }

    // Validates vitals against acceptable ranges
    private boolean validateVitals(int heartRate, int oxygenLevel, double temperatureF,
                                   int systolic, int diastolic,
                                   double weight, double height) {
        return heartRate >= 30 && heartRate <= 220 &&
                oxygenLevel >= 80 && oxygenLevel <= 100 &&
                temperatureF >= 85 && temperatureF <= 110 &&
                systolic >= 70 && systolic <= 200 &&
                diastolic >= 40 && diastolic <= 130 &&
                weight >= 50 && weight <= 700 &&
                height >= 20 && height <= 100;
    }

    // Saves validated vitals to DB and checks for emergencies in the background
    private void saveToDatabase(int heartRate, int oxygenLevel, double tempF,
                                int systolic, int diastolic,
                                double weight, double height, String timestamp, ActionEvent e) {

        long patientId = patient.getUnique_id();
        StringBuilder query = new StringBuilder();

        query.append("INSERT INTO vitals VALUES (");
        query.append("NULL, ");
        query.append(patientId).append(", ");
        query.append(heartRate).append(", ");
        query.append(oxygenLevel).append(", ");
        query.append(tempF).append(", ");
        query.append(systolic).append(", ");
        query.append(diastolic).append(", ");
        query.append(weight).append(", ");
        query.append(height).append(", '");
        query.append(timestamp).append("')");

        try (Conn conn = new Conn()) {
            conn.runUpdate(query.toString());

            // Add to in-memory patient record
            VitalSign vital = new VitalSign(patientId, heartRate, oxygenLevel, tempF, systolic, diastolic, weight, height, timestamp);
            patient.addVitalSign(vital);

            // Background task to evaluate and notify emergency contact
            Task<Void> backgroundTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    if (ea.checkVitals(vital)) {
                        String message = buildEmergencyMessage();
                        ea.sendEmailToEmergencyContact(patient, "URGENT: Critical Health Alert for " + patient.getName(), message);
                    }
                    return null;
                }

                // UI update on success
                @Override
                protected void succeeded() {
                    PatientDashboard.showEmergencyNotification(PatientDashboard.emergencyNotificationBox);
                    showAlert("Vitals Uploaded Successfully!");
                    ((Stage) ((Node) e.getSource()).getScene().getWindow()).close();
                }

                // Alert on failure
                @Override
                protected void failed() {
                    showAlert("Failed to send email: " + getException().getMessage());
                }
            };

            new Thread(backgroundTask).start();
        } catch (SQLException se) {
            se.printStackTrace();
            showAlert(se.getMessage());
        }
    }

    // Composes the emergency alert message
    private String buildEmergencyMessage() {
        return "Dear " + patient.getEmergencyEmail() + ",\n\n" +
                "This is an automated emergency alert from RPMS.\n\n" +
                "Patient Details:\n" +
                "- Name: " + patient.getName() + "\n" +
                "- Patient Email: " + patient.getEmail() + "\n" +
                "- Contact Number: " + patient.getContactNumber() + "\n\n" +
                "Critical Alert:\n" +
                "The patient's vital signs have exceeded safe thresholds. Immediate medical attention may be required.\n\n" +
                "Patient Address: " + patient.getAddress() + "\n\n" +
                "Thank you,\nRPMS Emergency Alert System\n";
    }

    // Displays user-friendly alerts
    private void showAlert(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Status");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
