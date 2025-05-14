package UserManagement;

import Connection.Conn;
import Helper.Helper;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class NewUserRegistration extends Application {

    // Text fields and combo boxes for user input
    private TextField tfName, tfEmergencyEmail, tfAddress, tfContactNumber, tfEmail, tfPassword;
    private Label labelID;
    private DatePicker dpDOB;
    private ComboBox<String> cbRole, cbAssignedTo, cbSpecialization;
    ArrayList<String> doctorEmails = new ArrayList<>();

    // Helper method to safely remove nodes from the grid
    private void safeRemove(GridPane grid, Node node) {
        if (grid.getChildren().contains(node)) {
            grid.getChildren().remove(node);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        // Initialize combo box for assigned doctors and fetch doctor emails
        cbAssignedTo = new ComboBox<>();
        fetchDoctorEmails();
        long id = Helper.randomNumberGenerator(); // Generate a random user ID

        // Setting up UI components
        Label heading = new Label("User Registration");
        heading.setFont(new Font("Serif", 30));
        heading.setMaxWidth(Double.MAX_VALUE);
        GridPane.setColumnSpan(heading, 2);
        GridPane.setHalignment(heading, HPos.CENTER);

        labelID = new Label(Long.toString(id)); // Display the generated ID
        labelID.setFont(new Font("Serif", 18));

        // Initialize text fields and date picker for user input
        tfName = new TextField();
        tfEmail = new TextField();
        tfEmergencyEmail = new TextField();
        tfPassword = new PasswordField();
        tfContactNumber = new TextField();
        tfAddress = new TextField();
        dpDOB = new DatePicker();

        // Combo boxes for role, specialization, and assigned doctor
        cbRole = new ComboBox<>();
        cbRole.getItems().addAll("Admin", "Patient", "Doctor");
        cbRole.setMinWidth(110);

        cbSpecialization = new ComboBox<>();
        cbSpecialization.getItems().addAll("Cardiologist", "Neurologist",
                "Orthopedic", "Dermatologist", "Psychiatrist", "Surgeon", "Dentist", "Radiologist");
        cbSpecialization.setMinWidth(110);
        Label lblSpecialization = new Label("Specialization");

        Label lblAssignedTo = new Label("AssignedTo");
        cbAssignedTo.setMinWidth(110);

        // Set up the layout grid
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(25);
        grid.setVgap(22);

        // Adding the UI components to the grid
        grid.addRow(0, heading);
        GridPane.setColumnSpan(heading, 2);
        GridPane.setHalignment(heading, javafx.geometry.HPos.CENTER);

        // Adding labels and fields to the grid
        grid.add(new Label("ID"), 0, 1); grid.add(labelID, 1, 1);
        grid.add(new Label("Name"), 0, 2); grid.add(tfName, 1, 2);
        grid.add(new Label("Email"), 0, 3); grid.add(tfEmail, 1, 3);
        grid.add(new Label("Password"), 0, 4); grid.add(tfPassword, 1, 4);
        grid.add(new Label("Contact Number"), 0, 5); grid.add(tfContactNumber, 1, 5);
        grid.add(new Label("DOB"), 0, 6); grid.add(dpDOB, 1, 6);
        grid.add(new Label("Role"), 0, 7); grid.add(cbRole, 1, 7);
        grid.add(new Label("Address"), 0, 8); grid.add(tfAddress, 1, 8);

        // Dynamically change the form fields based on the selected role
        cbRole.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.equals("Patient")) {
                safeRemove(grid, lblSpecialization);
                safeRemove(grid, cbSpecialization);
                grid.add(new Label("Emergency Email"), 0, 9); grid.add(tfEmergencyEmail, 1, 9);
                grid.add(lblAssignedTo, 0, 10); grid.add(cbAssignedTo, 1, 10);
            } else if (newVal.equals("Doctor")) {
                safeRemove(grid, lblAssignedTo);
                safeRemove(grid, cbAssignedTo);
                grid.add(lblSpecialization, 0, 9); grid.add(cbSpecialization, 1, 9);
            } else {
                safeRemove(grid, lblAssignedTo);
                safeRemove(grid, cbAssignedTo);
                safeRemove(grid, lblSpecialization);
                safeRemove(grid, cbSpecialization);
            }
        });

        // Buttons for submitting the form and canceling the registration
        Button submit = new Button("Submit");
        Button cancel = new Button("Cancel");

        submit.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        cancel.setStyle("-fx-background-color: black; -fx-text-fill: white;");

        // Layout for the buttons
        HBox buttons = new HBox(20, submit, cancel);
        buttons.setAlignment(Pos.CENTER);

        VBox layout = new VBox(20, grid, buttons);
        layout.setPadding(new Insets(30));

        // Event handlers for the buttons
        submit.setOnAction(e -> handleSubmit(e));
        cancel.setOnAction(e -> primaryStage.close());

        // Setting the scene and displaying the window
        Scene scene = new Scene(layout, 600, 638);
        primaryStage.setTitle("Register User");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Fetch all doctor emails to populate the "Assigned To" combo box
    public void fetchDoctorEmails() {
        String query = "select email from Doctor";
        try (Conn conn = new Conn()) {
            ResultSet rs = conn.runQuery(query);
            while (rs.next()) {
                doctorEmails.add(rs.getString("email"));
            }
            cbAssignedTo.getItems().setAll(doctorEmails); // Populate the combo box with doctor emails
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Handle the form submission to insert a new user into the database
    private void handleSubmit(ActionEvent e) {
        // Extract user input values from the form
        String name = tfName.getText();
        String idInString = labelID.getText();
        long id = Long.parseLong(idInString);
        String dob = dpDOB.getValue() != null ? dpDOB.getValue().toString() : "";
        String address = tfAddress.getText();
        String contactNumber = tfContactNumber.getText();
        String email = tfEmail.getText();
        String emergencyEmail = tfEmergencyEmail.getText();
        String role = cbRole.getValue();
        String specialization = cbSpecialization.getValue();
        String assignedToInString = cbAssignedTo.getValue();
        long assignedTo = Helper.fetchDoctorID(assignedToInString, "Doctor");
        String password = tfPassword.getText();
        String hashedPassword = Helper.hashPassword(password); // Hash the password for security

        int i = 0;
        String tableName = "Login";

        // Loop to handle multiple insert attempts for different roles
        while (i < 2) {
            StringBuilder query = new StringBuilder();

            // Construct the SQL query based on the role
            if (role.equals("Doctor")) {
                query.append("INSERT INTO ").append(tableName)
                        .append(" (id, name, email, password, contactNumber, specialization, dob, role, address) VALUES (");
                query.append(id).append(", '")
                        .append(name).append("', '")
                        .append(email).append("', '")
                        .append(hashedPassword).append("', '")
                        .append(contactNumber).append("', '")
                        .append(specialization).append("', '")
                        .append(dob).append("', '")
                        .append(role).append("', '")
                        .append(address).append("')");
            } else if (role.equals("Patient")) {
                query.append("INSERT INTO ").append(tableName)
                        .append(" (id, name, email, password, contactNumber, dob, role, address, assignedTo, emergencyEmail) VALUES (");
                query.append(id).append(", '")
                        .append(name).append("', '")
                        .append(email).append("', '")
                        .append(hashedPassword).append("', '")
                        .append(contactNumber).append("', '")
                        .append(dob).append("', '")
                        .append(role).append("', '")
                        .append(address).append("', ")
                        .append(assignedTo).append(", '")
                        .append(emergencyEmail).append("')");
            } else {
                query.append("INSERT INTO ").append(tableName)
                        .append(" (id, name, email, password, contactNumber, dob, role, address) VALUES (");
                query.append(id).append(", '")
                        .append(name).append("', '")
                        .append(email).append("', '")
                        .append(hashedPassword).append("', '")
                        .append(contactNumber).append("', '")
                        .append(dob).append("', '")
                        .append(role).append("', '")
                        .append(address).append("')");
            }

            // Execute the query and handle success or failure
            try (Conn conn = new Conn()) {
                conn.runUpdate(query.toString());
                if (i > 0) {
                    // Show success message and log the registration event
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("User Created Successfully");
                    Helper.insertLog(idInString, "New User Registration",
                            "New User Registered with ID " + id + " at " + Helper.currentTimestamp());
                    alert.showAndWait();
                    ((Stage)((Node)e.getSource()).getScene().getWindow()).close();
                }
            } catch (SQLException se) {
                // Handle errors, including duplicate email error
                se.printStackTrace();
                Helper.insertLog(null, "Failed Registration Attempt",
                        "Failed New " + "User Registration Attempt for email " +
                                email + " at " + Helper.currentTimestamp());
                Alert alert = new Alert(Alert.AlertType.ERROR, (se.getErrorCode() == 1062)
                        ? "Duplicate email error: Email already exists." : se.getMessage(), ButtonType.OK);
                alert.showAndWait();
                ((Stage)((Node)e.getSource()).getScene().getWindow()).close();
                break;
            }
            i++;
            tableName = role;
        }
    }
}
