package Reports;

import Connection.Conn;
import DoctorPatientInteraction.Feedback;
import Helper.Helper;
import UserManagement.Patient;
import UserManagement.SessionStorage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class FeedbacksTrend {

    private TableView<Feedback> tableView;  // TableView to display feedback data

    // Method to initialize and set up the report area with feedback data
    public void start(BorderPane reportArea, ComboBox<String> patientSelector) {
        tableView = new TableView<>();  // Initialize TableView for feedback display

        // Check if the logged-in user is a Patient
        if (SessionStorage.loggedInUser instanceof Patient) {
            // Create a column for Doctor's email in the table
            TableColumn<Feedback, String> doctorEmailCol = new TableColumn<>("Doctor Email");

            // Set a custom cell factory for the doctor email column
            doctorEmailCol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);  // Set text to null if cell is empty
                    } else {
                        // Retrieve doctor email based on doctor ID from feedback object
                        Feedback feedback = getTableView().getItems().get(getIndex());
                        long doctorId = feedback.getDoctorID();
                        String email = Objects.requireNonNull(Helper.fetchDoctor(doctorId)).getEmail();  // Fetch doctor info
                        setText(email);  // Display the doctor's email in the cell
                    }
                }
            });
            tableView.getColumns().add(doctorEmailCol);  // Add the doctor email column to the table
        }

        // Column for displaying feedback notes
        TableColumn<Feedback, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));

        // Column for displaying feedback submission timestamp
        TableColumn<Feedback, String> timestampCol = new TableColumn<>("Submitted On");
        timestampCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        tableView.getColumns().addAll(notesCol, timestampCol);  // Add columns to the table
        reportArea.setCenter(tableView);  // Set the table as the center of the report area

        // Check if a patient selector is provided
        if (patientSelector != null) {
            patientSelector.setOnAction(e -> {
                String selectedPatient = patientSelector.getValue();
                if (selectedPatient != null) {
                    // Load feedback data for the selected patient
                    loadFeedbackData(Long.parseLong(selectedPatient));
                }
            });

            // If a patient is already selected, load their feedback data
            if (patientSelector.getValue() != null) {
                loadFeedbackData(Long.parseLong(patientSelector.getValue()));
            }
        } else {
            // If no selector is provided, load feedback for the currently logged-in patient
            loadFeedbackData(SessionStorage.loggedInUser.getUnique_id());
        }
    }

    // Method to load feedback data for a given patient ID
    private void loadFeedbackData(Long patientId) {
        ObservableList<Feedback> data = FXCollections.observableArrayList();  // Observable list to hold feedback data

        try (Conn conn = new Conn();
             ResultSet rs = conn.runQuery("SELECT doctor_id, notes, timestamp FROM feedback WHERE patient_id = " + patientId)) {

            // Iterate through the result set to fetch feedback details
            while (rs.next()) {
                long doctorId = rs.getLong("doctor_id");
                String notes = rs.getString("notes");
                LocalDateTime timestampObj = rs.getTimestamp("timestamp").toLocalDateTime();
                String formattedTimestamp = timestampObj.format(DateTimeFormatter.ofPattern("MMMM d, yyyy hh:mm a"));  // Format timestamp

                // Add the feedback data to the observable list
                data.add(new Feedback(doctorId, notes, formattedTimestamp));
            }

        } catch (Exception e) {
            e.printStackTrace();  // Handle any exceptions that may occur during the database operation
        }

        tableView.setItems(data);  // Set the loaded feedback data into the table view
    }
}