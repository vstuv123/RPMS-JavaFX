package UserManagement;

import Connection.Conn;
import Helper.Helper;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.input.KeyCombination;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class LoginFX extends Application {

    private TextField tfEmail;            // TextField for email input
    private PasswordField tfPassword;     // PasswordField for password input

    @Override
    public void start(Stage stage) {
        // Heading label
        Label heading = new Label("Login");
        heading.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: black;");

        // Creating email input label and text field
        Label lblUsername = new Label("Email");
        tfEmail = new TextField();
        tfEmail.setPromptText("Enter email");

        // Creating password input label and password field
        Label lblPassword = new Label("Password");
        tfPassword = new PasswordField();
        tfPassword.setPromptText("Enter password");

        // Login and Cancel buttons
        Button login = new Button("Login");
        login.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        login.setOnAction(e -> handleLogin());
        login.setDefaultButton(true);

        Button cancel = new Button("Cancel");
        cancel.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        cancel.setOnAction(e -> System.exit(0));

        HBox buttonBox = new HBox(18, login, cancel);
        buttonBox.setAlignment(Pos.CENTER);

        // VBox form with heading at top
        VBox formBox = new VBox(15, heading, lblUsername, tfEmail, lblPassword, tfPassword, buttonBox);
        formBox.setAlignment(Pos.CENTER_LEFT);
        formBox.setPadding(new Insets(20));
        VBox.setVgrow(tfEmail, Priority.ALWAYS);
        VBox.setVgrow(tfPassword, Priority.ALWAYS);

        // Image and layout setup (unchanged)
        ImageView imageView = new ImageView(new Image("resources/icons/second.jpg"));
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setFitWidth(Region.USE_COMPUTED_SIZE);

        HBox mainLayout = new HBox(38);
        mainLayout.setPadding(new Insets(23));
        mainLayout.setStyle("-fx-background-color: white;");
        imageView.fitHeightProperty().bind(mainLayout.heightProperty());

        HBox.setHgrow(formBox, Priority.ALWAYS);
        HBox.setHgrow(imageView, Priority.ALWAYS);
        formBox.setMaxWidth(Double.MAX_VALUE);

        mainLayout.getChildren().addAll(formBox, imageView);

        Scene scene = new Scene(mainLayout, 700, 400);
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.setX(340);
        stage.setY(150);
        stage.show();
        stage.setResizable(true);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
    }

    // Method to handle login logic
    private void handleLogin() {
        String email = tfEmail.getText();          // Get entered email
        String password = tfPassword.getText();    // Get entered password

        String query = "SELECT * FROM Login WHERE email='" + email + "'"; // SQL to fetch user by email

        try (Conn conn = new Conn();  // Auto-closeable database connection
             ResultSet rs = conn.runQuery(query)) {

            if (rs.next()) {
                // User exists, fetch user details
                long userId = Helper.fetchDoctorID(email, "Login");
                String id = String.valueOf(userId);
                String hashedPassword = rs.getString("password"); // Get hashed password from DB

                if (Helper.checkPassword(password, hashedPassword)){ // Check entered password with hash

                    long assignedTo = rs.getLong("assignedTo");
                    String role = rs.getString("role"); // Get user role

                    // Create appropriate user object based on role
                    if (role.equals("Admin")) {
                        SessionStorage.loggedInUser = new Administrator(Long.parseLong(rs.getString("id")), rs.getString
                                ("name"), rs.getString("email"), rs.getString
                                ("contactNumber"), rs.getString("dob"),
                                rs.getString("role"), rs.getString("address"));
                    } else if (role.equals("Patient")) {
                        // Get doctor's email assigned to patient
                        String doctorEmail = Objects.requireNonNull(Helper.fetchDoctor(assignedTo)).getEmail();
                        SessionStorage.loggedInUser = new Patient(Long.parseLong(rs.getString("id")), rs.getString
                                ("name"), rs.getString("email"), rs.getString
                                ("contactNumber"), rs.getString("dob"),
                                rs.getString("role"), rs.getString("address"), rs.getString("emergencyEmail"), rs.getLong("assignedTo"),
                                assignedTo != 0 ? doctorEmail : null);
                    } else {
                        SessionStorage.loggedInUser = new Doctor(Long.parseLong(rs.getString("id")), rs.getString
                                ("name"), rs.getString("email"), rs.getString
                                ("contactNumber"), rs.getString("specialization"), rs.getString("dob"),
                                rs.getString("role"), rs.getString("address"));
                    }

                    // Show login success alert
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Login successful!", ButtonType.OK);
                    alert.showAndWait();

                    // Close login window
                    ((Stage) tfEmail.getScene().getWindow()).close();

                    // Log successful login
                    Helper.insertLog(id, "Login", "User Logged into System with" +
                            " ID " + id + " at " + Helper.currentTimestamp());

                    // Open appropriate dashboard
                    if (SessionStorage.loggedInUser instanceof Administrator) {
                        new Administrator().openDashboard();
                    } else if (SessionStorage.loggedInUser instanceof Patient) {
                        new Patient().openDashboard();
                    } else {
                        new Doctor().openDashboard();
                    }
                } else {
                    // Password mismatch
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid email or password", ButtonType.OK);
                    alert.showAndWait();
                    Helper.insertLog(id, "Failed Login Attempt", "Failed Login Attempt for User" +
                            " ID " + id + " at " + Helper.currentTimestamp());
                }
            } else {
                // No user found with provided email
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid email or password", ButtonType.OK);
                alert.showAndWait();
                Helper.insertLog(null, "Failed Login Attempt", "Failed Login Attempt for email " +
                        email + " at " + Helper.currentTimestamp());
                System.exit(0); // Terminate application
            }
        } catch (SQLException e) {
            // Handle SQL exceptions
            e.printStackTrace();
            Helper.insertLog(null, "Failed Login Attempt", "Failed Login Attempt for email " +
                    email + " at " + Helper.currentTimestamp());
        }
    }
}
