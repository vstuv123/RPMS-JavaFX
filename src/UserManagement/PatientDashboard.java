package UserManagement;

import AppointmentScheduling.RequestAppointment;
import AppointmentScheduling.ViewAcceptedAppointment;
import ChattingSystem.ChatPatientClient;
import EmergencyAlertSystem.PanicButton;
import HealthDataHandling.UploadVitals;
import Helper.Helper;
import Reports.ReportDashboard;
import VideoConsultations.ViewConsultationPatient;
import javafx.animation.PauseTransition;
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
import javafx.util.Duration;

public class PatientDashboard extends Application {

    // Get the currently logged-in patient from session storage
    private final Patient loggedInPatient = (Patient) SessionStorage.loggedInUser;
    public static VBox emergencyNotificationBox;

    @Override
    public void start(Stage stage) {
        // Create the root layout with BorderPane
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f9f9f9;");

        // Set up the header section
        Label header = new Label("Patient Dashboard");
        header.setFont(new Font("Arial", 28));
        header.setTextFill(Color.DARKBLUE);
        header.setPadding(new Insets(20));
        HBox topBar = new HBox(header);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: white; -fx-border-color: lightgray;");
        root.setTop(topBar);

        // Create the emergency notification box
        emergencyNotificationBox = new VBox();
        emergencyNotificationBox.setAlignment(Pos.CENTER);
        emergencyNotificationBox.getChildren().add(topBar);
        root.setTop(emergencyNotificationBox);

        // Create the sidebar for navigation
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(30, 10, 30, 10));
        sidebar.setStyle("-fx-background-color: white; -fx-border-color: lightgray;");
        sidebar.setPrefWidth(280);

        // Add buttons to the sidebar
        Button btnUploadVitals = createSidebarButton("Upload Vitals");
        Button btnReportsAndGraphs = createSidebarButton("Reports And Graphs");
        Button btnViewAppointments = createSidebarButton("View Appointments");
        Button btnRequestAppointment = createSidebarButton("Request Appointment");
        Button btnViewUpcomingConsultation = createSidebarButton("View Upcoming Consultation");
        Button btnChatDoctor = createSidebarButton("Chat With Doctor");
        Button btnPanicButton = createSidebarButton("Panic Button");
        Button btnLogout = createSidebarButton("Logout");
        Button btnExit = createSidebarButton("Exit");

        // Add all sidebar buttons to the sidebar VBox
        sidebar.getChildren().addAll(
                btnUploadVitals,
                btnReportsAndGraphs,
                btnViewAppointments,
                btnRequestAppointment,
                btnPanicButton,
                btnViewUpcomingConsultation,
                btnChatDoctor,
                btnLogout,
                btnExit
        );

        // Set the sidebar to the left side of the root layout
        root.setLeft(sidebar);

        // Create the profile box in the center of the dashboard
        VBox profileBox = new VBox(20);
        profileBox.setAlignment(Pos.CENTER);
        profileBox.setPadding(new Insets(50));
        profileBox.setStyle("-fx-background-color: white; -fx-border-color: lightgray; -fx-background-radius: 10;");

        // Display the logged-in patient's details
        Label welcomeLabel = new Label("Welcome, " + loggedInPatient.getName());
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        welcomeLabel.setTextFill(Color.DARKBLUE);

        Label idLabel = new Label("Patient ID: " + loggedInPatient.getUnique_id());
        idLabel.setFont(new Font("Arial", 18));
        idLabel.setTextFill(Color.GRAY);

        Label emailLabel = new Label("Email: " + loggedInPatient.getEmail());
        emailLabel.setFont(new Font("Arial", 18));
        emailLabel.setTextFill(Color.GRAY);

        Label contactLabel = new Label("Phone: " + loggedInPatient.getContactNumber());
        contactLabel.setFont(new Font("Arial", 18));
        contactLabel.setTextFill(Color.GRAY);

        Label dobLabel = new Label("Date of Birth: " + loggedInPatient.getDob());
        dobLabel.setFont(new Font("Arial", 18));
        dobLabel.setTextFill(Color.GRAY);

        Label addressLabel = new Label("Address: " + loggedInPatient.getAddress());
        addressLabel.setFont(new Font("Arial", 18));
        addressLabel.setTextFill(Color.GRAY);

        Label assignedDoctor = new Label("Assigned Doctor: " + loggedInPatient.getAssignedToEmail());
        assignedDoctor.setFont(new Font("Arial", 18));
        assignedDoctor.setTextFill(Color.GRAY);

        // Add all patient details to the profile box
        profileBox.getChildren().addAll(
                welcomeLabel,
                idLabel,
                emailLabel,
                contactLabel,
                dobLabel,
                addressLabel,
                assignedDoctor
        );

        // Set the profile box to the center of the layout
        root.setCenter(profileBox);

        // Create footer for the dashboard
        Label footer = new Label("© 2025 Remote Patient Management System");
        footer.setFont(new Font("Arial", 12));
        footer.setTextFill(Color.GRAY);
        footer.setPadding(new Insets(10));
        HBox bottomBar = new HBox(footer);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setStyle("-fx-background-color: white; -fx-border-color: lightgray;");
        root.setBottom(bottomBar);

        // Set up button actions
        btnUploadVitals.setOnAction(e -> {
            new UploadVitals().start(new Stage());
        });

        btnReportsAndGraphs.setOnAction(e -> {
            new ReportDashboard().start(new Stage());
        });

        btnViewAppointments.setOnAction(e -> {
            new ViewAcceptedAppointment("Patient").start(new Stage());
        });

        btnRequestAppointment.setOnAction(e -> {
            new RequestAppointment().start(new Stage());
        });

        btnPanicButton.setOnAction(e -> {
            new PanicButton().start(new Stage());
        });

        btnViewUpcomingConsultation.setOnAction(e -> {
            new ViewConsultationPatient().start(new Stage());
        });

        btnChatDoctor.setOnAction(e -> {
            new ChatPatientClient().start(new Stage());
        });

        btnLogout.setOnAction(e -> {
            long id = SessionStorage.loggedInUser.getUnique_id();
            String userId = String.valueOf(id);
            Helper.insertLog(userId, "Logout", "Patient logged out with ID " + id + " at " + Helper.currentTimestamp());
            SessionStorage.loggedInUser = null;
            ((Stage) btnLogout.getScene().getWindow()).close();
            new LoginFX().start(new Stage());
        });

        btnExit.setOnAction(e -> stage.close());

        // Set the scene and display the dashboard window
        Scene scene = new Scene(root, 1290, 639);
        stage.setScene(scene);
        stage.setTitle("Patient Dashboard");
        stage.setX(-10);
        stage.setY(1);
        stage.show();
    }

    // Method to show an emergency notification at the top of the dashboard
    public static void showEmergencyNotification(VBox emergencyNotificationBox) {
        Label emergencyLabel = new Label("⚠ EMERGENCY: Critical vitals detected! Immediate medical attention required.");
        emergencyLabel.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 15px;");
        emergencyLabel.setMaxWidth(Double.MAX_VALUE); // Stretch full width

        // Add the emergency notification at the top of the notification box
        emergencyNotificationBox.getChildren().add(0, emergencyLabel);

        // Remove the notification after 15 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(15));
        pause.setOnFinished(event -> emergencyNotificationBox.getChildren().remove(emergencyLabel));
        pause.play();
    }

    // Method to create buttons for the sidebar with custom styles
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
