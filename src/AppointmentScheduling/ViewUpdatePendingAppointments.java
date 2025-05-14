package AppointmentScheduling;

import Connection.Conn;
import Helper.Helper;
import NotificationsAndReminders.ContactInfo;
import NotificationsAndReminders.ReminderService;
import UserManagement.Doctor;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

public class ViewUpdatePendingAppointments extends Application {
    private TableView table; // TableView to display appointment data
    private DatePicker datePicker; // DatePicker for selecting a date to filter appointments
    Helper helper = new Helper(); // Helper object for additional functionalities
    ReminderService remainder = helper.generateReminderObject(); // ReminderService to send notifications

    @Override
    public void start(Stage primaryStage) {
        // Label to display the heading
        Label heading = new Label("Search by Date");
        heading.setFont(new Font("Arial", 14)); // Set font style for the heading

        // DatePicker for users to select a specific date to filter appointments
        datePicker = new DatePicker();

        // Box to hold the heading and DatePicker for alignment
        HBox topBox = new HBox(10, heading, datePicker);
        topBox.setPadding(new Insets(10));
        topBox.setAlignment(Pos.CENTER_LEFT);

        // TableView to display pending appointments
        table = new TableView<>();
        setupTable(); // Set up the table columns and structure
        loadAllRecords(); // Load all pending appointments initially

        // Button to search appointments based on the selected date
        Button searchBtn = new Button("Search");
        searchBtn.setOnAction(e -> searchRecord()); // Action for search button

        // Button to print the table of appointments
        Button printBtn = new Button("Print");
        printBtn.setOnAction(e -> {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(table.getScene().getWindow())) {
                boolean success = job.printPage(table);
                if (success) {
                    job.endJob(); // End print job if successful
                }
            }
        });

        // Cancel button to close the window
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> primaryStage.close());

        // HBox to align the buttons horizontally
        HBox buttonBox = new HBox(10, searchBtn, printBtn, cancelBtn);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        // Main layout of the page containing the DatePicker, buttons, and table
        VBox root = new VBox(10, topBox, buttonBox, table);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: white;");

        // Setting up the scene and showing the stage
        Scene scene = new Scene(root, 600, 550);
        primaryStage.setScene(scene);
        primaryStage.setTitle("View Pending Appointments");
        primaryStage.show();
    }

    // Fetch the appointment results based on a query and populate the table with the data
    @SuppressWarnings("unchecked")
    public void getResults(String query, ObservableList<Appointment> records) {
        try (Conn conn = new Conn(); // Establish a connection to the database
             ResultSet rs = conn.runQuery(query)) { // Run the provided query
            while (rs.next()) {
                // Add each appointment record to the ObservableList
                records.add(new Appointment(rs.getLong("appointment_id"), LocalDate.parse(rs.getString("appointment_date")),
                        rs.getTime("appointment_time").toLocalTime(),
                        rs.getLong("doctor_id"), rs.getLong("patient_id"),
                        rs.getString("status")));
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Print any SQL exceptions
        }
        table.setItems(records); // Set the records to the table
    }

    // Load all pending appointments of the logged-in doctor from the database
    private void loadAllRecords() {
        ObservableList<Appointment> records = FXCollections.observableArrayList();
        String query = "select * from appointment where status = 'Pending' and doctor_id = " + SessionStorage.loggedInUser.getUnique_id();
        getResults(query, records); // Fetch and display all pending appointments
    }

    // Search appointments based on the selected date from the DatePicker
    private void searchRecord() {
        LocalDate date = datePicker.getValue(); // Get the selected date
        ObservableList<Appointment> records = FXCollections.observableArrayList();
        String query = "select * from appointment where status = 'Pending' " +
                "and doctor_id = " + SessionStorage.loggedInUser.getUnique_id() +
                " and appointment_date = '" + date.toString() + "'";
        getResults(query, records); // Fetch and display appointments for the selected date
    }

    // Method to create table columns with dynamic headers and properties
    private <T> TableColumn<T, ?> createColumn(String title, String property) {
        TableColumn<T, String> column = new TableColumn<>(title); // Create a column with the given title
        column.setCellValueFactory(new PropertyValueFactory<>(property)); // Set the property for each cell
        return column; // Return the created column
    }

    // Setup the table columns for displaying appointment information
    @SuppressWarnings("unchecked")
    private void setupTable() {
        // Add the necessary columns to the table
        table.getColumns().addAll(
                createColumn("appointment_id", "appointmentId"),
                createColumn("patient_id", "patientID"),
                createColumn("appointment_date", "date"),
                createColumn("appointment_time", "time"),
                createColumn("status", "status")
        );

        // Add an additional "Actions" column with accept and reject buttons for each row
        TableColumn<Appointment, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new TableCell<Appointment, Void>() {
            private final Button acceptButton = new Button(); // Button to accept the appointment
            private final Button rejectButton = new Button(); // Button to reject the appointment

            {
                // Set the "accept" button's graphic (an icon)
                Image acceptImage = new Image("resources/icons/accept.png");
                ImageView acceptImageView = new ImageView(acceptImage);
                acceptImageView.setFitHeight(20);
                acceptImageView.setFitWidth(20);
                acceptButton.setGraphic(acceptImageView);
                acceptButton.setStyle("-fx-background-color: transparent;"); // Make the button background transparent

                // Set the action for the accept button
                acceptButton.setOnAction(event -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    acceptAppointment(appointment); // Call acceptAppointment method when clicked
                });

                // Set the "reject" button's graphic (an icon)
                Image rejectImage = new Image("resources/icons/reject.png");
                ImageView rejectImageView = new ImageView(rejectImage);
                rejectImageView.setFitHeight(20);
                rejectImageView.setFitWidth(20);
                rejectButton.setGraphic(rejectImageView);
                rejectButton.setStyle("-fx-background-color: transparent;");

                // Set the action for the reject button
                rejectButton.setOnAction(event -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    rejectAppointment(appointment); // Call rejectAppointment method when clicked
                });
            }

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null); // Set no graphic if the row is empty
                } else {
                    // Set a horizontal box (HBox) containing both buttons
                    HBox hbox = new HBox(5, acceptButton, rejectButton);
                    hbox.setAlignment(Pos.CENTER); // Align buttons at the center
                    hbox.setPadding(new Insets(5));
                    setGraphic(hbox); // Set the graphic for the table cell
                }
            }
        });
        table.getColumns().add(actionCol); // Add the action column to the table
    }

    // Fetch appointment details by appointment ID
    private Appointment getAppointment(long id) {
        String query = "select * from appointment where appointment_id = " + id;
        try (Conn c = new Conn(); // Open a database connection
             ResultSet rs = c.runQuery(query)) {
            if (rs.next()) {
                return new Appointment(rs.getLong("appointment_id"), LocalDate.parse(rs.getString("appointment_date")),
                        rs.getTime("appointment_time").toLocalTime(),
                        rs.getLong("doctor_id"), rs.getLong("patient_id"),
                        rs.getString("status"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Method to accept the appointment and update the status to 'Accepted'
    private void acceptAppointment(Appointment ap) {
        String query = "update appointment set status = 'Accepted' where appointment_id = " + ap.getAppointmentId();
        long patientId = Objects.requireNonNull(getAppointment(ap.getAppointmentId())).getPatientID();
        Patient p = Objects.requireNonNull(Helper.fetchPatient(patientId)); // Fetch patient details
        Doctor d = Objects.requireNonNull(Helper.fetchDoctor(p.getAssignedTo())); // Fetch doctor details
        ContactInfo contact = new ContactInfo(p.getEmail(), ConfigLoader.get("contactNumber")); // Contact info for reminder
        LocalTime time = Objects.requireNonNull(getAppointment(ap.getAppointmentId())).getTime(); // Get appointment time
        LocalDate date = Objects.requireNonNull(getAppointment(ap.getAppointmentId())).getDate(); // Get appointment date

        try (Conn c = new Conn()) {
            c.runUpdate(query); // Run the update query to change the status to 'Accepted'
            successAlert("Appointment Accepted Successfully"); // Show success alert
            remainder.sendReminder(contact, "Your Appointment Has Been Approved!", // Send reminder
                    "You have appointment with Dr. " + d.getName() + " at " + date + " " + Helper.formatTime(time));
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            alert.showAndWait(); // Show error alert if any exception occurs
        }
    }

    // Method to reject the appointment and remove it from the database
    private void rejectAppointment(Appointment ap) {
        String query = "delete from appointment where appointment_id = " + ap.getAppointmentId();
        long patientId = Objects.requireNonNull(getAppointment(ap.getAppointmentId())).getPatientID();
        Patient p = Objects.requireNonNull(Helper.fetchPatient(patientId));
        Doctor d = Objects.requireNonNull(Helper.fetchDoctor(p.getAssignedTo()));
        ContactInfo contact = new ContactInfo(p.getEmail(), ConfigLoader.get("contactNumber"));
        LocalTime time = Objects.requireNonNull(getAppointment(ap.getAppointmentId())).getTime();
        LocalDate date = Objects.requireNonNull(getAppointment(ap.getAppointmentId())).getDate();

        try (Conn c = new Conn()) {
            c.runUpdate(query); // Run the delete query to reject the appointment
            successAlert("Appointment Rejected Successfully"); // Show success alert
            remainder.sendReminder(contact,
                    "Your Appointment Has Been Rejected!", // Send rejection reminder
                    "Your appointment with Dr. " + d.getName() + " at " + date + " " + Helper.formatTime(time) + " has been cancelled. Plz reschedule.");
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            alert.showAndWait(); // Show error alert if any exception occurs
        }
    }

    // Method to show a success alert after updating the appointment
    private void successAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message); // Show success message
        loadAllRecords(); // Reload all the appointments
        alert.showAndWait(); // Show the alert
    }
}