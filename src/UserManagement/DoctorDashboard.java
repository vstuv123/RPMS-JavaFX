package UserManagement;

import AppointmentScheduling.ViewAcceptedAppointment;
import AppointmentScheduling.ViewUpdatePendingAppointments;
import ChattingSystem.ChatDoctorClient;
import DoctorPatientInteraction.DoctorViewPatientDetails;
import DoctorPatientInteraction.PatientHistory;
import DoctorPatientInteraction.PrescribeMedication;
import DoctorPatientInteraction.ProvideFeedback;
import Helper.Helper;
import Reports.ReportDashboard;
import VideoConsultations.DoctorScheduleConsultations;
import VideoConsultations.DoctorViewConsultations;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class DoctorDashboard extends Application {
    // Retrieve the logged-in doctor instance from session
    private final Doctor loggedInDoctor = (Doctor) SessionStorage.loggedInUser;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f9f9f9;");

        // Header Section
        Label header = new Label("Doctor Dashboard");
        header.setFont(new Font("Arial", 28));
        header.setTextFill(Color.DARKBLUE);
        header.setPadding(new Insets(20));
        HBox topBar = new HBox(header);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: white; -fx-border-color: lightgray;");
        root.setTop(topBar);

        // Sidebar with actionable options for doctors
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(30, 10, 30, 10));
        sidebar.setStyle("-fx-background-color: white; -fx-border-color: lightgray;");
        sidebar.setPrefWidth(280);

        // Buttons for different functionalities
        Button btnViewPatients = createSidebarButton("View Patients");
        Button btnReportsAndGraphs = createSidebarButton("Reports And Graphs");
        Button btnProvideFeedback = createSidebarButton("Provide Feedback");
        Button btnWritePrescription = createSidebarButton("Prescribe Medication");
        Button btnPatientHistory = createSidebarButton("Patient History");
        Button btnPendingAppointments = createSidebarButton("View Pending Appointments");
        Button btnViewAppointments = createSidebarButton("View Appointments");
        Button btnChatPatients = createSidebarButton("Chat With Patients");
        Button btnScheduleConsultations = createSidebarButton("Schedule Video Consultations");
        Button btnViewConsultations = createSidebarButton("View Video Consultations");
        Button btnLogout = createSidebarButton("Logout");
        Button btnExit = createSidebarButton("Exit");

        // Scrollable sidebar to handle smaller screens
        ScrollPane sidebarScrollPane = new ScrollPane(sidebar);
        sidebarScrollPane.setFitToWidth(true);
        sidebarScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sidebarScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setLeft(sidebarScrollPane);

        // Adding all navigation buttons to the sidebar
        sidebar.getChildren().addAll(
                btnViewPatients,
                btnReportsAndGraphs,
                btnProvideFeedback,
                btnWritePrescription,
                btnPatientHistory,
                btnPendingAppointments,
                btnViewAppointments,
                btnChatPatients,
                btnScheduleConsultations,
                btnViewConsultations,
                btnLogout,
                btnExit
        );

        // Center profile section displaying logged-in doctor's info
        VBox profileBox = new VBox(20);
        profileBox.setAlignment(Pos.CENTER);
        profileBox.setPadding(new Insets(50));
        profileBox.setStyle("-fx-background-color: white; -fx-border-color: lightgray; -fx-background-radius: 10;");

        Label welcomeLabel = new Label("Welcome, Dr. " + loggedInDoctor.getName());
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        welcomeLabel.setTextFill(Color.DARKBLUE);

        Label idLabel = new Label("Doctor ID: " + loggedInDoctor.getUnique_id());
        Label emailLabel = new Label("Email: " + loggedInDoctor.getEmail());
        Label contactLabel = new Label("Contact Number: " + loggedInDoctor.getContactNumber());
        Label specializationLabel = new Label("Specialization: " + loggedInDoctor.getSpecialization());
        Label dobLabel = new Label("Date of Birth: " + loggedInDoctor.getDob());
        Label addressLabel = new Label("Address: " + loggedInDoctor.getAddress());

        for (Label label : new Label[]{idLabel, emailLabel, contactLabel, specializationLabel, dobLabel, addressLabel}) {
            label.setFont(new Font("Arial", 18));
            label.setTextFill(Color.GRAY);
        }

        profileBox.getChildren().addAll(
                welcomeLabel,
                idLabel,
                emailLabel,
                contactLabel,
                specializationLabel,
                dobLabel,
                addressLabel
        );

        root.setCenter(profileBox);

        // Footer Section
        Label footer = new Label("© 2025 Remote Patient Management System");
        footer.setFont(new Font("Arial", 12));
        footer.setTextFill(Color.GRAY);
        footer.setPadding(new Insets(10));
        HBox bottomBar = new HBox(footer);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setStyle("-fx-background-color: white; -fx-border-color: lightgray;");
        root.setBottom(bottomBar);

        // Button actions mapped to respective module screens
        btnViewPatients.setOnAction(e -> new DoctorViewPatientDetails().start(new Stage()));
        btnReportsAndGraphs.setOnAction(e -> new ReportDashboard().start(new Stage()));
        btnProvideFeedback.setOnAction(e -> new ProvideFeedback().start(new Stage()));
        btnWritePrescription.setOnAction(e -> new PrescribeMedication().start(new Stage()));
        btnPatientHistory.setOnAction(e -> new PatientHistory().start(new Stage()));
        btnPendingAppointments.setOnAction(e -> new ViewUpdatePendingAppointments().start(new Stage()));
        btnViewAppointments.setOnAction(e -> new ViewAcceptedAppointment("Doctor").start(new Stage()));
        btnScheduleConsultations.setOnAction(e -> new DoctorScheduleConsultations().start(new Stage()));
        btnViewConsultations.setOnAction(e -> new DoctorViewConsultations().start(new Stage()));
        btnChatPatients.setOnAction(e -> new ChatDoctorClient().start(new Stage()));

        // Logout logs the activity and redirects to login screen
        btnLogout.setOnAction(e -> {
            long id = SessionStorage.loggedInUser.getUnique_id();
            String userId = String.valueOf(id);
            Helper.insertLog(userId, "Logout", "User logged out with ID " + id + " at " + Helper.currentTimestamp());
            SessionStorage.loggedInUser = null;
            ((Stage) btnLogout.getScene().getWindow()).close();
            new LoginFX().start(new Stage());
        });

        // Exit the application window
        btnExit.setOnAction(e -> stage.close());

        // Final stage setup
        Scene scene = new Scene(root, 1290, 639);
        stage.setScene(scene);
        stage.setTitle("Doctor Dashboard");
        stage.setX(-10);
        stage.setY(1);
        stage.show();
    }

    // Utility method for creating styled sidebar buttons
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
