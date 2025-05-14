package AppointmentScheduling;

import Connection.Conn;
import Helper.Helper;
import NotificationsAndReminders.ContactInfo;
import NotificationsAndReminders.ReminderService;
import UserManagement.Doctor;
import UserManagement.Patient;
import UserManagement.SessionStorage;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
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
import resources.ConfigLoader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class ViewAcceptedAppointment extends Application {
    private TableView table; // Table to display appointment records
    private DatePicker datePicker; // DatePicker to filter appointments by date
    private String tableName; // The type of user ('Doctor' or 'Patient')
    Helper helper = new Helper(); // Helper class for various operations
    ReminderService remainder = helper.generateReminderObject(); // Reminder service for sending reminders

    // Constructor to initialize the table name (Doctor or Patient)
    public ViewAcceptedAppointment(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public void start(Stage primaryStage) {
        // Label for the heading of the page
        Label heading = new Label("Search by Date");
        heading.setFont(new Font("Arial", 14));

        // DatePicker to select a specific date
        datePicker = new DatePicker();

        // Top box containing the heading and DatePicker
        HBox topBox = new HBox(10, heading, datePicker);
        topBox.setPadding(new Insets(10));
        topBox.setAlignment(Pos.CENTER_LEFT);

        // TableView to display the list of appointments
        table = new TableView<>();
        setupTable(); // Setting up the columns based on user type (Doctor or Patient)
        loadAllRecords(); // Loading all records (appointments) initially

        // Search button to filter appointments by selected date
        Button searchBtn = new Button("Search");
        searchBtn.setOnAction(e -> searchRecord());

        // Print button to print the table of appointments
        Button printBtn = new Button("Print");
        printBtn.setOnAction(e -> {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(table.getScene().getWindow())) {
                boolean success = job.printPage(table);
                if (success) {
                    job.endJob();
                }
            }
        });

        // Cancel button to close the current window
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> primaryStage.close());

        // HBox to align the buttons horizontally
        HBox buttonBox = new HBox(10, searchBtn, printBtn, cancelBtn);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        // VBox for the main layout containing the top box, buttons, and the table
        VBox root = new VBox(10, topBox, buttonBox, table);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: white;");

        // Setting the scene and showing the stage
        Scene scene = new Scene(root, 600, 550);
        primaryStage.setScene(scene);
        primaryStage.setTitle("View Appointments");
        primaryStage.show();
    }

    // Method to fetch and display appointment records based on the query
    @SuppressWarnings("unchecked")
    public void getResults(String query, ObservableList<Appointment> records) {
        try (Conn conn = new Conn();
             ResultSet rs = conn.runQuery(query)) { // Execute the query to get results
            while (rs.next()) {
                // Add each appointment to the records list
                records.add(new Appointment(rs.getLong("appointment_id"), LocalDate.parse(rs.getString("appointment_date")),
                        rs.getTime("appointment_time").toLocalTime(),
                        rs.getLong("doctor_id"), rs.getLong("patient_id"),
                        rs.getString("status")));
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle SQL exceptions
        }
        table.setItems(records); // Set the records to the table
    }

    // Load all accepted appointments based on the logged-in user type (Doctor or Patient)
    private void loadAllRecords() {
        ObservableList<Appointment> records = FXCollections.observableArrayList();
        String query;
        if (tableName.equals("Doctor")) {
            // Query to fetch accepted appointments for a doctor
            query = "select * from appointment where status = 'Accepted' and doctor_id = " + SessionStorage.loggedInUser.getUnique_id();
        } else {
            // Query to fetch accepted appointments for a patient
            query = "select * from appointment where status = 'Accepted' and patient_id = " + SessionStorage.loggedInUser.getUnique_id();
        }
        getResults(query, records); // Fetch and display the records
    }

    // Method to search for appointments based on the selected date
    private void searchRecord() {
        LocalDate date = datePicker.getValue(); // Get the selected date
        ObservableList<Appointment> records = FXCollections.observableArrayList();
        String query;

        if (tableName.equals("Doctor")) {
            // Query to fetch accepted appointments for a doctor on the selected date
            query = "select * from appointment where status = 'Accepted' " +
                    "and doctor_id = " + SessionStorage.loggedInUser.getUnique_id() +
                    " and appointment_date = '" + date.toString() + "'";
        } else {
            // Query to fetch accepted appointments for a patient on the selected date
            query = "select * from appointment where status = 'Accepted' " +
                    "and patient_id = " + SessionStorage.loggedInUser.getUnique_id() +
                    " and appointment_date = '" + date.toString() + "'";
        }
        getResults(query, records); // Fetch and display the filtered records
    }

    // Method to create a table column for displaying appointment information
    private <T> TableColumn<T, ?> createColumn(String title, String property) {
        TableColumn<T, String> column = new TableColumn<>(title); // Create a column
        column.setCellValueFactory(new PropertyValueFactory<>(property)); // Set the property to be displayed
        return column; // Return the column
    }

    // Method to setup the table columns based on the user type (Doctor or Patient)
    @SuppressWarnings("unchecked")
    private void setupTable() {
        if (tableName.equals("Patient")) {
            // Add a column for displaying the doctor's email for the patient
            TableColumn<Appointment, String> doctorEmailCol = new TableColumn<>("Doctor Email");
            doctorEmailCol.setCellValueFactory(cellData -> {
                Appointment appointment = cellData.getValue();
                long doctorId = appointment.getDoctorID(); // Get the doctor ID
                String doctorEmail = Objects.requireNonNull(Helper.fetchDoctor(doctorId)).getEmail(); // Fetch doctor's email
                return new SimpleStringProperty(doctorEmail); // Return the doctor's email
            });
            // Add columns to the table
            table.getColumns().addAll(
                    createColumn("appointment_id", "appointmentId"),
                    doctorEmailCol,
                    createColumn("appointment_date", "date"),
                    createColumn("appointment_time", "time"),
                    createColumn("status", "status")
            );
        } else {
            // Add columns for doctor viewing appointments
            table.getColumns().addAll(
                    createColumn("appointment_id", "appointmentId"),
                    createColumn("patient_id", "patientID"),
                    createColumn("appointment_date", "date"),
                    createColumn("appointment_time", "time"),
                    createColumn("status", "status")
            );
        }
    }

    // Method to send reminders to patients about today's appointments
    public void getAppointmentsOfToday() {
        String query = "SELECT * FROM appointment WHERE appointment_date = CURDATE()"; // SQL to fetch today's appointments
        try (Conn conn = new Conn();
             ResultSet rs = conn.runQuery(query)) {
            while (rs.next()) {
                long patientId = rs.getLong("patient_id"); // Get the patient ID
                Patient p = Objects.requireNonNull(Helper.fetchPatient(patientId)); // Fetch the patient
                Doctor d = Objects.requireNonNull(Helper.fetchDoctor(rs.getLong("doctor_id"))); // Fetch the doctor
                ContactInfo contact = new ContactInfo(p.getEmail(), ConfigLoader.get("contactNumber")); // Get contact info
                LocalTime time = rs.getTime("appointment_time").toLocalTime(); // Get appointment time
                // Send reminder to patient
                remainder.sendReminder(contact, "Your Upcoming Appointment with Dr. " + d.getName(),
                        "You have an appointment with Dr. " + d.getName() + " today at " + Helper.formatTime(time));
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle SQL exceptions
        }
    }
}
