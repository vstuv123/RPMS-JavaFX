package UserManagement;

import Helper.Helper;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import Connection.Conn;

import java.sql.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class UpdatePatientDoctor extends Application {

    private String tableName;
    private ComboBox<String> cId;
    private TextField tfName, tfAddress, tfContactNumber, tfEmail, tfEmergencyEmail;
    private Label labelDob, labelRole, labelAssignedTo, labelSpecialization;
    private ComboBox<String> cbAssignedTo, cbSpecialization;

    private Map<String, Control> fieldMap;
    ArrayList<String> doctorEmails = new ArrayList<>();

    // Constructor to initialize the table name (either "Patient" or "Doctor")
    public UpdatePatientDoctor(String name) {
        this.tableName = name;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Update " + tableName +" Details");

        // Heading of the screen
        Label heading = new Label("Update " + tableName + " Details");
        heading.setFont(new Font("Tahoma", 25));

        // Labels for ID selection and other fields
        Label lblId = new Label("Select ID");
        lblId.setFont(new Font("Serif", 20));

        labelDob = new Label();
        labelRole = new Label();
        labelAssignedTo = new Label("Assigned To");
        labelSpecialization = new Label("Specialization");

        // Text fields for user input
        tfAddress = new TextField();
        tfContactNumber = new TextField();
        tfEmail = new TextField();
        tfEmergencyEmail = new TextField();
        tfName = new TextField();

        // ComboBoxes for selecting values like assigned doctor and specialization
        cbAssignedTo = new ComboBox<>();
        cbAssignedTo.setMinWidth(110);
        cbSpecialization = new ComboBox<>();
        cbSpecialization.getItems().addAll("Cardiologist", "Neurologist", "Orthopedic", "Dermatologist",
                "Psychiatrist", "Surgeon", "Dentist", "Radiologist");
        cbSpecialization.setMinWidth(110);

        // If the table is "Patient", fetch the list of doctors
        if (tableName.equals("Patient")) {
            fetchDoctorEmails();
        }

        // Map of fields to display based on the table type
        fieldMap = Map.of(
                "dob", labelDob,
                "name", tfName,
                "address", tfAddress,
                "contactNumber", tfContactNumber,
                "email", tfEmail,
                "emergencyEmail", tfEmergencyEmail,
                "role", labelRole
        );

        // ComboBox for selecting the ID of the patient or doctor
        cId = new ComboBox<>();
        cId.setMinWidth(110);
        loadIds();  // Load available IDs from the database

        // Event listener for ID selection, triggering data load for the selected ID
        cId.setOnAction(e -> loadRecordData(cId.getValue()));

        // Grid layout for organizing the fields
        GridPane grid = new GridPane();
        grid.setVgap(15);
        grid.setHgap(15);
        grid.setPadding(new Insets(20));

        int i = 0;
        // Adding rows for each field and label
        grid.addRow(i++, heading);
        grid.addRow(i++, lblId, cId);
        grid.addRow(i++, new Label("Name:"), tfName);
        grid.addRow(i++, new Label("Email:"), tfEmail);
        grid.addRow(i++, new Label("Contact Number:"), tfContactNumber);
        if (tableName.equals("Doctor")) {
            grid.addRow(i++, labelSpecialization, cbSpecialization); // Doctor specialization
        }

        grid.addRow(i++, new Label("Date of Birth:"), labelDob);
        grid.addRow(i++, new Label("Role:"), labelRole);
        grid.addRow(i++, new Label("Address:"), tfAddress);
        if (tableName.equals("Patient")) {
            grid.addRow(i++, new Label("Emergency Email"), tfEmergencyEmail);
            grid.addRow(i++, labelAssignedTo, cbAssignedTo); // Patient's assigned doctor
        }

        // Buttons for updating and canceling the update
        Button updateBtn = new Button("Update");
        updateBtn.setOnAction(e -> updateRecord(e)); // Update record action

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> primaryStage.close()); // Close window action

        // HBox to hold the buttons
        HBox buttons = new HBox(15, updateBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER);

        // VBox to hold the form and buttons together
        VBox root = new VBox(13, grid, buttons);
        root.setPadding(new Insets(42));

        // Set the scene for the primary stage
        Scene scene = new Scene(root, 600, 530);
        primaryStage.setScene(scene);
        primaryStage.setX(100);
        primaryStage.setY(30);
        primaryStage.show();
    }

    // Loads the IDs from the database (either from Patient or Doctor table)
    private void loadIds() {
        String query = "SELECT id FROM " + tableName;
        try (Conn c = new Conn();
             ResultSet rs = c.runQuery(query)) {

            // Adding IDs to the ComboBox
            while (rs.next()) {
                cId.getItems().add(rs.getString("id"));
            }
            if (!cId.getItems().isEmpty()) {
                cId.getSelectionModel().selectFirst(); // Select the first ID by default
                loadRecordData(cId.getValue()); // Load data for the first selected ID
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Fetches the list of doctor emails to populate the assigned doctor ComboBox
    public void fetchDoctorEmails() {
        String query = "select email from Doctor";
        try(Conn conn = new Conn()) {
            ResultSet rs = conn.runQuery(query);
            while (rs.next()) {
                doctorEmails.add(rs.getString("email"));
            }
            cbAssignedTo.getItems().setAll(doctorEmails); // Set all fetched doctor emails in the ComboBox
        }catch(SQLException e) {
            e.printStackTrace();
        }
    }

    // Loads the data for the selected ID from the database
    private void loadRecordData(String Id) {
        long id = Long.parseLong(Id);
        String query = "SELECT * FROM " + tableName + " WHERE id='" + id + "'";
        try (Conn c = new Conn();
             ResultSet rs = c.runQuery(query)) {
            if (rs.next()) {
                // Set each field in the UI with the corresponding data from the database
                for (Map.Entry<String, Control> entry: fieldMap.entrySet()) {
                    String value = rs.getString(entry.getKey());
                    Control control = entry.getValue();

                    if (control instanceof Label label) {
                        label.setText(value); // For labels, just set the text
                    } else if (control instanceof TextField tf) {
                        tf.setText(value); // For text fields, set the text value
                    }
                }
                // If it's a patient, set the assigned doctor
                if (tableName.equals("Patient")) {
                    Doctor doctor = Objects.requireNonNull(Helper.fetchDoctor(rs.getLong("assignedTo")));
                    cbAssignedTo.getSelectionModel().select(doctor.getEmail());
                } else if(tableName.equals("Doctor")) {
                    cbSpecialization.getSelectionModel().select(rs.getString("specialization"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Updates the record in the database with the new information
    private void updateRecord(ActionEvent e) {
        long id = Long.parseLong(cId.getValue());
        String idInStr = String.valueOf(id);
        String name = tfName.getText();
        String address = tfAddress.getText();
        String contactNumber = tfContactNumber.getText();
        String email = tfEmail.getText();
        String emergencyEmail = tfEmergencyEmail.getText();
        String specialization = cbSpecialization.getSelectionModel().getSelectedItem();
        String assignedToMail = cbAssignedTo.getSelectionModel().getSelectedItem();
        long assignedTo = Helper.fetchDoctorID(assignedToMail, "Doctor");

        int i = 0;
        String queryTableName = "Login";

        while (i < 2) {
            String query = "UPDATE " + queryTableName + " SET name='" + name + "', email='"
                    + email + "', contactNumber='" + contactNumber + "', specialization='" + (tableName.equals("Doctor") ?
                    specialization : null) + "', " + "address='"
                    + address + "', emergencyEmail='" + (tableName.equals("Patient") ?
                    emergencyEmail : null) + "', assignedTo='" + (tableName.equals("Patient") ?
                    assignedTo : 0) + "' WHERE id='" + id + "'";

            try (Conn c = new Conn()) {
                c.runUpdate(query);
                if (i > 0) {
                    Helper.insertLog(idInStr, "Update User Information",
                            "User Information Updated for ID " + id + " at " +
                                    Helper.currentTimestamp());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText(tableName + " Details Updated Successfully");
                    alert.showAndWait();
                    ((Stage)((Node)e.getSource()).getScene().getWindow()).close(); // Close the window after update
                }
            } catch (SQLException se) {
                se.printStackTrace();
                Helper.insertLog(idInStr, "Failed User Update",
                        "User Updation failed for ID " + id + " at " +
                                Helper.currentTimestamp());
                // Display error alert if update fails
                Alert alert = new Alert(Alert.AlertType.ERROR, (se.getErrorCode() == 1062)
                        ? "Duplicate email error: Email already exists." : se.getMessage(), ButtonType.OK);
                alert.showAndWait();
                ((Stage)((Node)e.getSource()).getScene().getWindow()).close(); // Close window on failure
                break;
            }
            i++;
            queryTableName = tableName;
        }
    }
}
