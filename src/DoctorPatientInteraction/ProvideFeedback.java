package DoctorPatientInteraction;

import Connection.Conn;
import Helper.Helper;
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
import java.sql.SQLException;

public class ProvideFeedback extends Application {

    // UI components declaration
    private TextField tfId;  // Patient ID input field
    private Label labelDoctorID;  // Doctor ID label
    private TextArea taNotes;  // Text area for feedback notes

    @Override
    public void start(Stage primaryStage) {
        // Fetch the logged-in doctor's ID
        long id = SessionStorage.loggedInUser.getUnique_id();

        // Heading for the window
        Label heading = new Label("Provide Feedback");
        heading.setFont(new Font("Serif", 30));
        heading.setMaxWidth(Double.MAX_VALUE);
        GridPane.setColumnSpan(heading, 2);
        GridPane.setHalignment(heading, HPos.CENTER);

        // Doctor ID label (for internal reference)
        labelDoctorID = new Label(Long.toString(id));
        labelDoctorID.setFont(new Font("Serif", 18));

        // Initializing the text fields and text areas
        tfId = new TextField();
        taNotes = new TextArea();

        // Setting up the grid for the form layout
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(25);
        grid.setVgap(22);

        // Defining column constraints for the grid
        grid.getColumnConstraints().addAll(
                new ColumnConstraints(150),
                new ColumnConstraints(260)
        );

        // Labels for form fields
        Label labelDoctorId = new Label("Doctor ID");
        labelDoctorId.setFont(Font.font("Serif", FontWeight.BOLD, 18));

        Label labelPatientId = new Label("Patient ID");
        labelPatientId.setFont(Font.font("Serif", FontWeight.BOLD, 18));

        Label labelNotes = new Label("Notes");
        labelNotes.setFont(Font.font("Serif", FontWeight.BOLD, 18));

        // Adding components to the grid
        grid.addRow(0, heading);
        GridPane.setColumnSpan(heading, 2);
        GridPane.setHalignment(heading, javafx.geometry.HPos.CENTER);

        grid.add(labelDoctorId, 0, 1); grid.add(labelDoctorID, 1, 1);
        grid.add(labelPatientId, 0, 2); grid.add(tfId, 1, 2);
        grid.add(labelNotes, 0, 3); grid.add(taNotes, 1, 3);

        // Submit and Cancel buttons
        Button submit = new Button("Submit");
        Button cancel = new Button("Cancel");

        submit.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        cancel.setStyle("-fx-background-color: black; -fx-text-fill: white;");

        // Buttons container
        HBox buttons = new HBox(20, submit, cancel);
        buttons.setAlignment(Pos.CENTER);

        // Main layout container
        VBox layout = new VBox(20, grid, buttons);
        layout.setPadding(new Insets(30));

        // Button event handlers
        submit.setOnAction(e -> handleSubmit(e));
        cancel.setOnAction(e -> primaryStage.close());

        // Scene setup
        Scene scene = new Scene(layout, 540, 480);
        primaryStage.setTitle("Doctor Feedback");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Submit button event handler
    private void handleSubmit(ActionEvent e) {
        // Retrieve input values from form fields
        long doctorId = Long.parseLong(labelDoctorID.getText());
        long patientId = Long.parseLong(tfId.getText());
        String notes = taNotes.getText();
        String timestamp = Helper.currentTimestamp();

        // Escape single quotes in notes text to prevent SQL injection
        notes = notes.replace("'", "''");

        // Validate that the patient ID is correct
        if (Helper.isValidPatientId(patientId)) {
            // Build the SQL query for inserting feedback into the database
            StringBuilder query = new StringBuilder();
            query.append("insert into feedback values(null, ")
                    .append(doctorId).append(", ")
                    .append(patientId).append(", '")
                    .append(notes).append("', '")
                    .append(timestamp).append("')");

            try (Conn conn = new Conn()) {
                // Execute the query
                conn.runUpdate(query.toString());

                // Display success message
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Feedback created Successfully");
                alert.showAndWait();

                // Close the feedback form after submission
                ((Stage)((Node)e.getSource()).getScene().getWindow()).close();
            } catch (SQLException se) {
                // If an error occurs, display the error message
                se.printStackTrace();
                generateErrorAlert(se.getMessage(), e);
            }
        } else {
            // If the patient ID is invalid, show an error alert
            generateErrorAlert("No such Patient ID exists. Please enter a correct one.", e);
        }
    }

    // Method to generate and show error alerts
    private void generateErrorAlert(String message, ActionEvent e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
        ((Stage)((Node)e.getSource()).getScene().getWindow()).close(); // Close the form if there's an error
    }
}