package VideoConsultations;

import Connection.Conn;
import Helper.Helper;
import UserManagement.Doctor;
import UserManagement.Patient;
import UserManagement.SessionStorage;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ViewConsultationPatient extends Application {

    private Patient p;  // Patient object
    private Doctor d;  // Doctor object

    @Override
    public void start(Stage stage) {
        // VBox layout for the consultation details
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #f5f5f5;");

        p = (Patient) SessionStorage.loggedInUser;  // Get the logged-in patient
        d = Helper.fetchDoctor(p.getAssignedTo());  // Fetch the doctor assigned to the patient

        // SQL query to fetch the next consultation for the patient and doctor
        String query = "SELECT * FROM videoconsultations WHERE patient_id = " + p.getUnique_id() +
                " AND doctor_id = " + d.getUnique_id() + " AND scheduled_datetime > NOW() ORDER BY scheduled_datetime ASC LIMIT 1";

        try (Conn conn = new Conn(); ResultSet rs = conn.runQuery(query)) {
            // If a consultation is found
            if (rs.next()) {
                String meetingLink = rs.getString("meeting_link");  // Get the meeting link for the consultation
                LocalDateTime datetime = rs.getTimestamp("scheduled_datetime").toLocalDateTime();  // Get the scheduled time

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a");  // Format the datetime
                String formattedDateTime = datetime.format(formatter);

                String note = rs.getString("notes");  // Get the doctor's notes

                // Create UI components to display consultation details
                Label heading = new Label("Your Upcoming Video Consultation");
                heading.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

                Label doctorEmail = new Label("Doctor Email: " + d.getEmail());

                Label dateTimeLabel = new Label("Scheduled At: " + formattedDateTime);
                Label noteLabel = new Label("Doctor's Note: " + (note == null ? "None" : note));  // Handle null note
                noteLabel.setWrapText(true);
                noteLabel.setMaxWidth(320);

                // Button to start the consultation now
                Button startNow = new Button("Start Now");
                startNow.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
                startNow.setOnAction(e -> {
                    try {
                        Desktop.getDesktop().browse(new URI(meetingLink));  // Open the meeting link in the browser
                    } catch (Exception ex) {
                        // Show error if the link fails to open
                        new Alert(Alert.AlertType.ERROR, "Failed to open meeting link.").showAndWait();
                        ((Stage)((Node)e.getSource()).getScene().getWindow()).close();
                    }
                });

                // Add the labels and button to the layout
                layout.getChildren().addAll(heading, doctorEmail, dateTimeLabel, noteLabel, startNow);
            } else {
                // If no upcoming consultations are found, display a message
                Label noConsultation = new Label("No Upcoming Consultations Found");
                noConsultation.setStyle("-fx-font-size: 16px; -fx-text-fill: #777;");

                // Button to go back to the dashboard
                Button backBtn = new Button("Back to Dashboard");
                backBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px;");
                backBtn.setOnAction(e -> stage.close());  // Close the current stage

                layout.getChildren().addAll(noConsultation, backBtn);  // Add the message and back button to the layout
            }
        } catch (Exception e) {
            // Handle any database errors
            new Alert(Alert.AlertType.ERROR, "Database Error: " + e.getMessage()).showAndWait();
        }

        // Set up the scene and show the stage
        Scene scene = new Scene(layout, 400, 300);
        stage.setScene(scene);
        stage.setTitle("View Consultation");
        stage.show();
    }
}