package VideoConsultations;

import Connection.Conn;
import Helper.Helper;
import NotificationsAndReminders.ContactInfo;
import NotificationsAndReminders.ReminderService;
import UserManagement.Doctor;
import UserManagement.Patient;
import UserManagement.SessionStorage;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.Node;
import javafx.geometry.HPos;

import javafx.event.ActionEvent;
import resources.ConfigLoader;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class DoctorScheduleConsultations extends Application {

    // Define the UI components as class variables
    private TextField tfPatientId, tfMeetingLink, tfTime;
    private TextArea taNote;
    private DatePicker datePicker;
    private Label labelDoctorID;
    private Doctor doctor;
    private Helper helper = new Helper();
    private ReminderService remainder = helper.generateReminderObject();

    @Override
    public void start(Stage primaryStage) {
        // Get the logged-in doctor from SessionStorage.
        doctor = (Doctor) SessionStorage.loggedInUser;

        // Set up the UI elements.
        Label heading = new Label("Schedule Video Consultation");
        heading.setFont(new Font("Serif", 30));
        heading.setMaxWidth(Double.MAX_VALUE);
        GridPane.setColumnSpan(heading, 2);
        GridPane.setHalignment(heading, HPos.CENTER);

        labelDoctorID = new Label(Long.toString(doctor.getUnique_id()));
        labelDoctorID.setFont(new Font("Serif", 18));

        tfPatientId = new TextField();
        tfMeetingLink = new TextField();
        tfTime = new TextField();
        tfTime.setPromptText("HH:mm (24-hour format)");
        taNote = new TextArea();
        taNote.setPrefHeight(60);
        datePicker = new DatePicker();

        // Set up the GridPane layout.
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(25);
        grid.setVgap(22);
        grid.getColumnConstraints().addAll(
                new ColumnConstraints(150),
                new ColumnConstraints(260)
        );

        grid.addRow(0, heading);
        GridPane.setColumnSpan(heading, 2);
        GridPane.setHalignment(heading, HPos.CENTER);

        grid.add(new Label("Doctor ID"), 0, 1); grid.add(labelDoctorID, 1, 1);
        grid.add(new Label("Patient ID"), 0, 2); grid.add(tfPatientId, 1, 2);
        grid.add(new Label("Date"), 0, 3); grid.add(datePicker, 1, 3);
        grid.add(new Label("Time (HH:MM)"), 0, 4); grid.add(tfTime, 1, 4);
        grid.add(new Label("Meeting Link"), 0, 5); grid.add(tfMeetingLink, 1, 5);
        grid.add(new Label("Note"), 0, 6); grid.add(taNote, 1, 6);

        // Set up the buttons.
        Button submit = new Button("Schedule");
        Button cancel = new Button("Cancel");
        submit.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        cancel.setStyle("-fx-background-color: black; -fx-text-fill: white;");

        HBox buttons = new HBox(20, submit, cancel);
        buttons.setAlignment(Pos.CENTER);

        VBox layout = new VBox(20, grid, buttons);
        layout.setPadding(new Insets(30));

        // Set up button actions.
        submit.setOnAction(e -> handleSubmit(e));
        cancel.setOnAction(e -> primaryStage.close());

        // Set up the scene and stage.
        Scene scene = new Scene(layout, 600, 550);
        primaryStage.setTitle("Schedule Consultation");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleSubmit(ActionEvent e) {
        try {
            // Parse the input values from the UI.
            long doctorId = Long.parseLong(labelDoctorID.getText());
            long patientId = Long.parseLong(tfPatientId.getText());
            LocalDate date = datePicker.getValue();
            LocalTime time = LocalTime.parse(tfTime.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            String meetingLink = tfMeetingLink.getText();
            String note = taNote.getText();

            // Construct the SQL query. Added single quotes for string values and used prepared statement.
            String query = "INSERT INTO videoconsultations (doctor_id, patient_id, meeting_link, scheduled_datetime, notes, status) VALUES (" +
                    doctorId + ", " +
                    patientId + ", '" +
                    meetingLink + "', '" +
                    date.atTime(time) + "', '" +
                    note.replace("'", "''") + "', 'SCHEDULED')";

            // Use a try-with-resources statement to automatically close the connection.
            try (Conn conn = new Conn()) {
                conn.runUpdate(query); // Execute the query

                // Fetch patient details to send the reminder.
                Patient p = Objects.requireNonNull(Helper.fetchPatient(patientId));
                ContactInfo ci = new ContactInfo(p.getEmail(), ConfigLoader.get("contactNumber"));
                String message = "\uD83D\uDCC5 Doctor " + doctor.getEmail() + " has scheduled a video consultation for " + date + " at " + Helper.formatTime(time) + ". Please join the consultation at the mentioned time.";

                // Send the reminder.
                remainder.sendReminder(ci, "Doctor Scheduled Video Consultation", message);

                // Show a success message.
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Video consultation scheduled successfully.");
                alert.showAndWait();

                // Close the window.
                ((Stage)((Node)e.getSource()).getScene().getWindow()).close();
            }
            // Handle potential SQL exceptions.
        } catch (SQLException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR, ex.getMessage(), ButtonType.OK);
            alert.showAndWait();
            ((Stage)((Node)e.getSource()).getScene().getWindow()).close();
        }
        // Handle NullPointerException if user input is missing.
        catch (NullPointerException ex) {
            Alert alert = new Alert(AlertType.ERROR, "Please enter all the details", ButtonType.OK);
            alert.showAndWait();
            ((Stage)((Node)e.getSource()).getScene().getWindow()).close();
        }
        // Handle any unexpected exceptions.
        catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR, "An unexpected error occurred.", ButtonType.OK);
            alert.showAndWait();
            ((Stage)((Node)e.getSource()).getScene().getWindow()).close();
        }
    }
}
