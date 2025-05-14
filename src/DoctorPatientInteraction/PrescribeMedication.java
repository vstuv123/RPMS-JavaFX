package DoctorPatientInteraction;

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
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.Node;
import javafx.geometry.HPos;

import javafx.event.ActionEvent;
import resources.ConfigLoader;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

public class PrescribeMedication extends Application {

    // UI components
    private TextField tfId, tfName, tfDosage, tfSchedule;
    private TextArea taInstructions, taNotes;
    private DatePicker datePicker;
    private Label labelDoctorID;
    Helper helper = new Helper();
    ReminderService remainder = helper.generateReminderObject();

    @Override
    public void start(Stage primaryStage) {
        // Get the logged-in doctor's ID
        long id = SessionStorage.loggedInUser.getUnique_id();

        // Heading
        Label heading = new Label("Prescribe Medication");
        heading.setFont(new Font("Serif", 30));
        heading.setMaxWidth(Double.MAX_VALUE);
        GridPane.setColumnSpan(heading, 2);
        GridPane.setHalignment(heading, HPos.CENTER);

        // Doctor ID label (for internal reference)
        labelDoctorID = new Label(Long.toString(id));
        labelDoctorID.setFont(new Font("Serif", 18));

        // Initialize text fields and text areas
        tfId = new TextField();
        tfName = new TextField();
        tfDosage = new TextField();
        tfSchedule = new TextField();
        taNotes = new TextArea();
        taInstructions = new TextArea();
        datePicker = new DatePicker();

        taInstructions.setPrefHeight(60); // Set the preferred height for instructions area

        // GridPane layout for form fields
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(25);
        grid.setVgap(22);

        // Column constraints for GridPane
        grid.getColumnConstraints().addAll(
                new ColumnConstraints(150),
                new ColumnConstraints(260)
        );

        // Labels for form fields
        Label labelDoctorId = new Label("Doctor ID");
        labelDoctorId.setFont(Font.font("Serif", FontWeight.BOLD, 18));

        Label labelPatientId = new Label("Patient ID");
        labelPatientId.setFont(Font.font("Serif", FontWeight.BOLD, 18));

        Label labelPrescriptionID = new Label("Prescription ID");
        labelPrescriptionID.setFont(Font.font("Serif", FontWeight.BOLD, 18));

        Label labelDateIssued = new Label("Date Issued");
        labelDateIssued.setFont(Font.font("Serif", FontWeight.BOLD, 18));

        Label labelName = new Label("Medicine Name");
        labelName.setFont(Font.font("Serif", FontWeight.BOLD, 18));

        Label labelDosage = new Label("Dosage");
        labelDosage.setFont(Font.font("Serif", FontWeight.BOLD, 18));

        Label labelSchedule = new Label("Schedule");
        labelSchedule.setFont(Font.font("Serif", FontWeight.BOLD, 18));

        Label labelInstructions = new Label("Instructions");
        labelInstructions.setFont(Font.font("Serif", FontWeight.BOLD, 18));

        Label labelNotes = new Label("Notes");
        labelNotes.setFont(Font.font("Serif", FontWeight.BOLD, 18));

        // Add components to the grid
        grid.addRow(0, heading);
        GridPane.setColumnSpan(heading, 2);
        GridPane.setHalignment(heading, javafx.geometry.HPos.CENTER);

        grid.add(labelDoctorId, 0, 1); grid.add(labelDoctorID, 1, 1);
        grid.add(labelPatientId, 0, 2); grid.add(tfId, 1, 2);
        grid.add(labelDateIssued, 0, 3); grid.add(datePicker, 1, 3);
        grid.add(labelName, 0, 4); grid.add(tfName, 1, 4);
        grid.add(labelDosage, 0, 5); grid.add(tfDosage, 1, 5);
        grid.add(labelSchedule, 0, 6); grid.add(tfSchedule, 1, 6);
        grid.add(labelInstructions, 0, 7); grid.add(taInstructions, 1, 7);
        grid.add(labelNotes, 0, 8); grid.add(taNotes, 1, 8);

        // Create submit and cancel buttons
        Button submit = new Button("Submit");
        Button cancel = new Button("Cancel");

        submit.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        cancel.setStyle("-fx-background-color: black; -fx-text-fill: white;");

        // Buttons container
        HBox buttons = new HBox(20, submit, cancel);
        buttons.setAlignment(Pos.CENTER);

        // Layout container
        VBox layout = new VBox(20, grid, buttons);
        layout.setPadding(new Insets(30));

        // Action handlers for buttons
        submit.setOnAction(e -> handleSubmit(e));
        cancel.setOnAction(e -> primaryStage.close());

        // Set up the scene
        Scene scene = new Scene(layout, 600, 600);
        primaryStage.setTitle("Prescribe Medication");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Handle submit button action
    private void handleSubmit(ActionEvent e) {
        // Retrieve user input from form fields
        long doctorID = Long.parseLong(labelDoctorID.getText());
        long patientID = Long.parseLong(tfId.getText());
        LocalDate dateIssued = datePicker.getValue();
        String instructions = taInstructions.getText();
        String name = tfName.getText();
        String dosage = tfDosage.getText();
        String schedule = tfSchedule.getText();
        String notes = taNotes.getText();
        String timestamp = Helper.currentTimestamp();

        instructions = instructions.replace("'", "''"); // Escape single quotes in instructions

        // Build SQL query string
        StringBuilder query = new StringBuilder();
        query.append("insert into prescription (doctor_id, patient_id, date_issued, instructions, ")
                .append("medicine_name, dosage, schedule, notes, timestamp) ")
                .append("values(")
                .append(doctorID).append(", ")
                .append(patientID).append(", '")
                .append(dateIssued).append("', '")
                .append(instructions).append("', '")
                .append(name).append("', '")
                .append(dosage).append("', '")
                .append(schedule).append("', '")
                .append(notes).append("', '")
                .append(timestamp).append("')");

        try (Conn conn = new Conn()) {
            // Execute the query
            conn.runUpdate(query.toString());

            // Fetch patient and doctor details for reminder
            Patient p = Objects.requireNonNull(Helper.fetchPatient(patientID));
            ContactInfo contact = new ContactInfo(p.getEmail(), ConfigLoader.get("contactNumber"));
            Doctor d = Objects.requireNonNull(Helper.fetchDoctor(p.getAssignedTo()));

            // Construct reminder message
            String message = "Dr. " + d.getName() + " prescribed " + name + " (" + dosage + ") on " + dateIssued + ". Please follow instructions carefully.";

            // Send reminder
            remainder.sendReminder(contact, "New Prescription Issued by Your Doctor", message);

            // Show success alert
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Prescription created Successfully");
            alert.showAndWait();
            ((Stage)((Node)e.getSource()).getScene().getWindow()).close(); // Close the form after submission
        } catch (SQLException se) {
            // Handle SQL errors
            se.printStackTrace();
            generateErrorAlert(se.getMessage(), e);
        }
    }

    // Generate error alert on failure
    private void generateErrorAlert(String message, ActionEvent e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
        ((Stage)((Node)e.getSource()).getScene().getWindow()).close(); // Close the form on error
    }
}