package UserManagement;

import Helper.Helper;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class AdminDashboard extends Application {

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f9f9f9;");

        // Top Header Bar
        Label header = new Label("Admin Dashboard");
        header.setFont(new Font("Arial", 28));
        header.setTextFill(Color.DARKBLUE);
        header.setPadding(new Insets(20));
        HBox topBar = new HBox(header);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: white; -fx-border-color: lightgray;");
        root.setTop(topBar);

        // Left Sidebar with Navigation Buttons
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(30, 10, 30, 10));
        sidebar.setStyle("-fx-background-color: white; -fx-border-color: lightgray;");
        sidebar.setPrefWidth(280);

        // Sidebar buttons for different admin operations
        Button btnNewUser = createSidebarButton("New User Registration");
        Button btnViewPatients = createSidebarButton("View Patient Records");
        Button btnViewDoctors = createSidebarButton("View Doctor Records");
        Button btnViewAdmins = createSidebarButton("View Admin Records");
        Button btnViewLogs = createSidebarButton("View Logs");
        Button btnUpdatePatient = createSidebarButton("Update Patient");
        Button btnUpdateDoctor = createSidebarButton("Update Doctor");
        Button btnLogout = createSidebarButton("Logout");
        Button btnExit = createSidebarButton("Exit");

        sidebar.getChildren().addAll(
                btnNewUser,
                btnViewPatients,
                btnViewDoctors,
                btnViewAdmins,
                btnViewLogs,
                btnUpdatePatient,
                btnUpdateDoctor,
                btnLogout,
                btnExit
        );
        root.setLeft(sidebar);

        // Center section displaying logged-in admin's profile
        VBox profileBox = new VBox(20);
        profileBox.setAlignment(Pos.CENTER);
        profileBox.setPadding(new Insets(50));
        profileBox.setStyle("-fx-background-color: white; -fx-border-color: lightgray; -fx-background-radius: 10;");

        // Display user details from session
        Label welcomeLabel = new Label("Welcome, " + SessionStorage.loggedInUser.getName());
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        welcomeLabel.setTextFill(Color.DARKBLUE);

        Label idLabel = new Label("ID: " + SessionStorage.loggedInUser.getUnique_id());
        Label emailLabel = new Label("Email: " + SessionStorage.loggedInUser.getEmail());
        Label roleLabel = new Label("Role: " + SessionStorage.loggedInUser.getRole());
        Label contactLabel = new Label("Contact Number: " + SessionStorage.loggedInUser.getContactNumber());
        Label dobLabel = new Label("Date of Birth: " + SessionStorage.loggedInUser.getDob());
        Label addressLabel = new Label("Address: " + SessionStorage.loggedInUser.getAddress());

        idLabel.setFont(new Font("Arial", 18));
        emailLabel.setFont(new Font("Arial", 18));
        roleLabel.setFont(new Font("Arial", 18));
        contactLabel.setFont(new Font("Arial", 18));
        dobLabel.setFont(new Font("Arial", 18));
        addressLabel.setFont(new Font("Arial", 18));

        idLabel.setTextFill(Color.GRAY);
        emailLabel.setTextFill(Color.GRAY);
        roleLabel.setTextFill(Color.GRAY);
        contactLabel.setTextFill(Color.GRAY);
        dobLabel.setTextFill(Color.GRAY);
        addressLabel.setTextFill(Color.GRAY);

        profileBox.getChildren().addAll(
                welcomeLabel,
                idLabel,
                emailLabel,
                roleLabel,
                contactLabel,
                dobLabel,
                addressLabel
        );

        root.setCenter(profileBox);

        // Footer with system name
        Label footer = new Label("© 2025 Remote Patient Management System");
        footer.setFont(new Font("Arial", 12));
        footer.setTextFill(Color.GRAY);
        footer.setPadding(new Insets(10));
        HBox bottomBar = new HBox(footer);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setStyle("-fx-background-color: white; -fx-border-color: lightgray;");
        root.setBottom(bottomBar);

        // Action Listeners for Navigation
        btnNewUser.setOnAction(e -> new NewUserRegistration().start(new Stage()));
        btnViewPatients.setOnAction(e -> new RecordDetails<Patient>("Patient").start(new Stage()));
        btnViewDoctors.setOnAction(e -> new RecordDetails<Doctor>("Doctor").start(new Stage()));
        btnViewAdmins.setOnAction(e -> new RecordDetails<Administrator>("Admin").start(new Stage()));
        btnViewLogs.setOnAction(e -> new ViewLogs().start(new Stage()));
        btnUpdatePatient.setOnAction(e -> new UpdatePatientDoctor("Patient").start(new Stage()));
        btnUpdateDoctor.setOnAction(e -> new UpdatePatientDoctor("Doctor").start(new Stage()));

        // Logout with session clearing and log entry
        btnLogout.setOnAction(e -> {
            long id = SessionStorage.loggedInUser.getUnique_id();
            String userId = String.valueOf(id);
            Helper.insertLog(userId, "Logout", "User Logged out from System with ID "
                    + id + " at " + Helper.currentTimestamp());
            SessionStorage.loggedInUser = null;
            ((Stage) btnLogout.getScene().getWindow()).close();
            new LoginFX().start(new Stage());
        });

        // Exit the application
        btnExit.setOnAction(e -> stage.close());

        // Set scene and show dashboard window
        Scene scene = new Scene(root, 1290, 639);
        stage.setScene(scene);
        stage.setTitle("Admin Dashboard");
        stage.setX(-10);
        stage.setY(1);
        stage.show();
    }

    // Creates styled sidebar buttons with hover effects
    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(260);
        btn.setFont(new Font("Arial", 16));
        btn.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: lightgray;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #e0f7fa; -fx-text-fill: black; -fx-border-color: lightgray;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: lightgray;"));
        return btn;
    }
}
