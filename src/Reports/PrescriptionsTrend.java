package Reports;

import Connection.Conn;
import DoctorPatientInteraction.Prescription;
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

public class PrescriptionsTrend {

    private TableView<Prescription> tableView;  // TableView to display the prescription data

    public void start(BorderPane reportArea, ComboBox<String> patientSelector) {
        tableView = new TableView<>();  // Initialize the TableView

        // Check if the logged-in user is a patient and add a column for the doctor's email if so
        if (SessionStorage.loggedInUser instanceof Patient) {
            // Create a column for the doctor's email and set up a custom cell factory to fetch it dynamically
            TableColumn<Prescription, String> doctorEmailCol = new TableColumn<>("Doctor Email");
            doctorEmailCol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);  // Clear the cell content if the item is empty
                    } else {
                        Prescription prescription = getTableView().getItems().get(getIndex());
                        long doctorId = prescription.getDoctorID();
                        String email = Objects.requireNonNull(Helper.fetchDoctor(doctorId)).getEmail();  // Fetch doctor's email using a helper method
                        setText(email);  // Set the text of the cell to the doctor's email
                    }
                }
            });
            tableView.getColumns().add(doctorEmailCol);  // Add the doctor email column to the table
        }

        // Create columns for other prescription data like medicine name, dosage, schedule, instructions, etc.
        TableColumn<Prescription, String> medicineCol = new TableColumn<>("Medicine Name");
        medicineCol.setCellValueFactory(new PropertyValueFactory<>("medicineName"));

        TableColumn<Prescription, String> dosageCol = new TableColumn<>("Dosage");
        dosageCol.setCellValueFactory(new PropertyValueFactory<>("dosage"));

        TableColumn<Prescription, String> scheduleCol = new TableColumn<>("Schedule");
        scheduleCol.setCellValueFactory(new PropertyValueFactory<>("schedule"));

        TableColumn<Prescription, String> instructionsCol = new TableColumn<>("Instructions");
        instructionsCol.setCellValueFactory(new PropertyValueFactory<>("instructions"));

        TableColumn<Prescription, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));

        TableColumn<Prescription, String> dateCol = new TableColumn<>("Date Issued");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        // Add all the columns to the TableView
        tableView.getColumns().addAll(medicineCol, dosageCol, scheduleCol, instructionsCol, notesCol, dateCol);

        // Set the TableView as the center of the report area (UI container)
        reportArea.setCenter(tableView);

        // Handle patient selection from the ComboBox, and load prescription data for the selected patient
        if (patientSelector != null) {
            patientSelector.setOnAction(e -> {
                String selectedPatient = patientSelector.getValue();
                if (selectedPatient != null) {
                    loadPrescriptionData(Long.parseLong(selectedPatient));  // Load prescription data for selected patient
                }
            });

            // If a patient is already selected, load prescription data for that patient
            if (patientSelector.getValue() != null) {
                loadPrescriptionData(Long.parseLong(patientSelector.getValue()));
            }
        } else {
            // If no patient selector, load data for the logged-in user
            loadPrescriptionData(SessionStorage.loggedInUser.getUnique_id());
        }
    }

    // Method to fetch and load prescription data for a given patient ID
    private void loadPrescriptionData(Long patientId) {
        ObservableList<Prescription> data = FXCollections.observableArrayList();  // Observable list to hold prescription data

        try (Conn conn = new Conn();
             ResultSet rs = conn.runQuery(
                     "SELECT doctor_id, medicine_name, dosage, schedule, instructions, notes, timestamp " +
                             "FROM prescription WHERE patient_id = " + patientId)) {

            // Process the result set and convert it to prescription objects
            while (rs.next()) {
                long doctorId = rs.getLong("doctor_id");
                String medicineName = rs.getString("medicine_name");
                String dosage = rs.getString("dosage");
                String schedule = rs.getString("schedule");
                String instructions = rs.getString("instructions");
                String notes = rs.getString("notes");
                LocalDateTime issuedDateTime = rs.getTimestamp("timestamp").toLocalDateTime();  // Parse the timestamp
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy hh:mm a");  // Format the timestamp
                String timestamp = issuedDateTime.format(formatter);  // Format the date

                // Create a new Prescription object and add it to the data list
                data.add(new Prescription(doctorId, instructions, medicineName, dosage, schedule, notes, timestamp));
            }

        } catch (Exception e) {
            e.printStackTrace();  // Handle any exceptions that occur during database querying
        }

        // Set the loaded data into the TableView
        tableView.setItems(data);
    }
}