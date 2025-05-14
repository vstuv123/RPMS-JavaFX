package AppointmentScheduling;

import Connection.Conn;
import Helper.Helper;
import UserManagement.Patient;
import UserManagement.SessionStorage;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

public class RequestAppointment extends Application {
    // Labels for patient ID and doctor email
    private Label labelPatientID, labelDoctorEmail;
    // Date picker and time combo box for scheduling the appointment
    private DatePicker datePicker;
    private ComboBox<LocalTime> cbTime;

    @Override
    public void start(Stage primaryStage) {
        // Fetch the logged-in patient and associated doctor details
        Patient patient = (Patient) SessionStorage.loggedInUser;
        long pid = patient.getUnique_id();
        String dEmail = patient.getAssignedToEmail();

        // Heading label
        Label heading = new Label("Request Appointment");
        heading.setFont(new Font("Serif", 30));
        heading.setMaxWidth(Double.MAX_VALUE);
        GridPane.setColumnSpan(heading, 2);
        GridPane.setHalignment(heading, HPos.CENTER);

        // Patient ID and doctor email labels
        labelPatientID = new Label(Long.toString(pid));
        labelPatientID.setFont(new Font("Serif", 18));

        labelDoctorEmail = new Label(dEmail);
        labelDoctorEmail.setFont(new Font("Serif", 18));

        // Date picker and time combo box for selecting the appointment date and time
        datePicker = new DatePicker();
        cbTime = new ComboBox<>();
        ObservableList<LocalTime> timeOptions = FXCollections.observableArrayList();

        // Generate available times for the appointment (10:30 AM to 5:00 PM with 30-minute intervals)
        LocalTime startTime = LocalTime.of(10, 30);
        LocalTime endTime = LocalTime.of(17, 0);

        while (!startTime.isAfter(endTime)) {
            timeOptions.add(startTime);
            startTime = startTime.plusMinutes(30);
        }
        cbTime.setItems(timeOptions);
        cbTime.setMinWidth(110);

        // Set up the layout with GridPane for organizing components
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(25);
        grid.setVgap(22);

        grid.getColumnConstraints().addAll(
                new ColumnConstraints(150),
                new ColumnConstraints(260)
        );

        // Labels for the fields in the form
        Label lblDoctorEmail = new Label("Doctor Email");
        lblDoctorEmail.setFont(Font.font("Serif", FontWeight.BOLD, 18));

        Label lblPatientId = new Label("Patient ID");
        lblPatientId.setFont(Font.font("Serif", FontWeight.BOLD, 18));

        Label lblDate = new Label("Date");
        lblDate.setFont(Font.font("Serif", FontWeight.BOLD, 18));

        Label lblTime = new Label("Time");
        lblTime.setFont(Font.font("Serif", FontWeight.BOLD, 18));

        // Adding labels and fields to the grid
        grid.addRow(0, heading);
        GridPane.setColumnSpan(heading, 2);
        GridPane.setHalignment(heading, javafx.geometry.HPos.CENTER);

        grid.add(lblPatientId, 0, 1); grid.add(labelPatientID, 1, 1);
        grid.add(lblDoctorEmail, 0, 2); grid.add(labelDoctorEmail, 1, 2);
        grid.add(lblDate, 0, 3); grid.add(datePicker, 1, 3);
        grid.add(lblTime, 0, 4); grid.add(cbTime, 1, 4);

        // Buttons for submitting and canceling the request
        Button submit = new Button("Submit");
        Button cancel = new Button("Cancel");

        // Styling for the buttons
        submit.setStyle(
                "-fx-background-color: #4CAF50; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 8 20 8 20; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );

        cancel.setStyle(
                "-fx-background-color: #F44336; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 8 20 8 20; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );

        // Arrange the buttons in a horizontal layout
        HBox buttons = new HBox(20, submit, cancel);
        buttons.setAlignment(Pos.CENTER);

        // Arrange the form and buttons in a vertical layout
        VBox layout = new VBox(20, grid, buttons);
        layout.setPadding(new Insets(30));

        // Handle submit and cancel button actions
        submit.setOnAction(e -> handleSubmit(e));
        cancel.setOnAction(e -> primaryStage.close());

        // Set the scene and show the window
        Scene scene = new Scene(layout, 560, 454);
        primaryStage.setTitle("Request Appointment");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleSubmit(ActionEvent e) {
        // Get patient ID, doctor email, date, and time from the UI
        long patientId = Long.parseLong(labelPatientID.getText());
        String doctorEmail = labelDoctorEmail.getText();
        long doctorId = Helper.fetchDoctorID(doctorEmail, "Doctor");
        LocalDate date = datePicker.getValue();
        LocalTime time = cbTime.getValue();

        // Construct the SQL query to insert the appointment into the database
        StringBuilder query = new StringBuilder();
        query.append("insert into appointment values(null, ")
                .append(patientId).append(", ")
                .append(doctorId).append(", '")
                .append(date).append("', '")
                .append(time).append("', '")
                .append("Pending").append("')");

        // Execute the query to insert the appointment in the database
        try (Conn conn = new Conn()) {
            conn.runUpdate(query.toString());  // Execute the query
            // Show success alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Appointment Requested Successfully");
            alert.showAndWait();
            // Close the window after success
            ((Stage)((Node)e.getSource()).getScene().getWindow()).close();
        } catch (SQLException se) {
            se.printStackTrace();
            // Show error alert if something goes wrong
            Alert alert = new Alert(Alert.AlertType.ERROR, se.getMessage(), ButtonType.OK);
            alert.showAndWait();
            // Close the window on error as well
            ((Stage)((Node)e.getSource()).getScene().getWindow()).close();
        }
    }
}