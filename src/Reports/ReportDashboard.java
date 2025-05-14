package Reports;

import Connection.Conn;
import UserManagement.Doctor;
import UserManagement.SessionStorage;
import com.itextpdf.text.Element;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.ResultSet;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

public class ReportDashboard extends Application {
    private ComboBox<String> patientSelector;  // ComboBox for selecting patient ID (visible for doctors)
    private Button downloadPdfButton;  // Button to download the current tab content as PDF
    private Doctor doctor;  // The currently logged-in doctor
    private boolean isDoctor;  // Flag to check if the logged-in user is a doctor

    private Tab vitalsTab;  // Tab for viewing vitals data
    private Tab prescriptionsTab;  // Tab for viewing prescriptions data
    private Tab feedbacksTab;  // Tab for viewing doctor feedbacks
    private Tab healthTrendsTab;  // Tab for viewing health trends
    private Tab medicationTab;  // Tab for viewing medication effectiveness data
    private TabPane tabPane;  // TabPane to hold all tabs

    @Override
    public void start(Stage primaryStage) {
        // Check if the logged-in user is a doctor
        if (SessionStorage.loggedInUser instanceof Doctor) {
            doctor = (Doctor) SessionStorage.loggedInUser;  // Cast the logged-in user to a Doctor object
            isDoctor = true;  // Set the flag to true if the user is a doctor
        }

        // Set the window title
        primaryStage.setTitle("Health Data Report Viewer");

        // =============== Top Controls ==================
        HBox topControls = new HBox(10);  // Create a horizontal box for top controls
        topControls.setPadding(new Insets(10));  // Add padding around the top controls
        topControls.setAlignment(Pos.CENTER_LEFT);  // Align the top controls to the left

        // Button to download the report as a PDF
        downloadPdfButton = new Button("Download PDF");
        downloadPdfButton.setDisable(true);  // Initially disable the button until content is loaded
        topControls.getChildren().add(downloadPdfButton);  // Add the button to the top controls

        // If the user is a doctor, add a ComboBox for selecting a patient
        if (isDoctor) {
            patientSelector = new ComboBox<>();
            patientSelector.setPromptText("Select Patient Id...");  // Placeholder text
            patientSelector.setMinWidth(160);  // Set the minimum width of the ComboBox
            populatePatients();  // Populate the ComboBox with patients assigned to the doctor
            topControls.getChildren().add(patientSelector);  // Add the ComboBox to the top controls
        }

        // ================== TabPane ==================
        tabPane = new TabPane();  // Create a TabPane to hold the report tabs
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);  // Disable closing of tabs

        // Create tabs for different reports
        vitalsTab = new Tab("Vitals");
        prescriptionsTab = new Tab("Prescriptions");
        feedbacksTab = new Tab("Doctor Feedbacks");
        healthTrendsTab = new Tab("Health Trends");
        medicationTab = new Tab("Medication Effectiveness");

        tabPane.getTabs().addAll(vitalsTab, prescriptionsTab, feedbacksTab, healthTrendsTab, medicationTab);  // Add tabs to the TabPane

        // Handle tab selection and load corresponding content
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (patientSelector != null) {
                patientSelector.getSelectionModel().clearSelection();  // Clear patient selection when switching tabs
                patientSelector.setPromptText("Select Patient Id...");  // Reset prompt text
            }

            // Load content based on the selected tab
            if (newTab == vitalsTab) {
                loadVitalsReport();
            } else if (newTab == prescriptionsTab) {
                loadPrescriptionsReport();
            } else if (newTab == feedbacksTab) {
                loadFeedbacksTab();
            } else if (newTab == healthTrendsTab) {
                loadHealthTrendsTab();
            } else if (newTab == medicationTab) {
                loadMedicationEffectivenessTab();
            }
        });

        // =============== Layout =================
        BorderPane mainLayout = new BorderPane();  // Create the main layout container
        mainLayout.setTop(topControls);  // Set top controls at the top of the layout
        mainLayout.setCenter(tabPane);  // Set the TabPane at the center of the layout

        // =============== Button Actions ================
        downloadPdfButton.setOnAction(e -> exportCurrentTabToPDF());  // Export the selected tab content to a PDF when clicked

        // =============== Show Scene ====================
        Scene scene = new Scene(mainLayout, 1290, 639);  // Create the scene with the layout
        primaryStage.setScene(scene);  // Set the scene in the primaryStage
        primaryStage.setX(-10);  // Set the initial X position of the window
        primaryStage.setY(1);  // Set the initial Y position of the window
        primaryStage.show();  // Show the stage

        loadVitalsReport(); // Load the Vitals report by default when the application starts
    }

    // Method to populate the ComboBox with patients assigned to the current doctor
    private void populatePatients() {
        try (Conn conn = new Conn();
             ResultSet rs = conn.runQuery("SELECT id FROM patient WHERE assignedTo = " + doctor.getUnique_id())) {
            ObservableList<String> patients = FXCollections.observableArrayList();
            while (rs.next()) {
                patients.add(rs.getString("id"));  // Add patient IDs to the list
            }
            patientSelector.setItems(patients);  // Set the list of patients to the ComboBox
        } catch (Exception e) {
            e.printStackTrace();  // Print any exceptions for debugging
        }
    }

    // Methods to load content for each tab (Vitals, Prescriptions, Feedbacks, etc.)
    private void loadVitalsReport() {
        downloadPdfButton.setDisable(false);  // Enable the PDF download button
        BorderPane content = new BorderPane();  // Create a new BorderPane to hold the content
        new VitalsTrend().start(content, isDoctor ? patientSelector : null);  // Load Vitals data
        vitalsTab.setContent(content);  // Set the content for the Vitals tab
    }

    private void loadPrescriptionsReport() {
        downloadPdfButton.setDisable(false);  // Enable the PDF download button
        BorderPane content = new BorderPane();  // Create a new BorderPane to hold the content
        new PrescriptionsTrend().start(content, isDoctor ? patientSelector : null);  // Load Prescriptions data
        prescriptionsTab.setContent(content);  // Set the content for the Prescriptions tab
    }

    private void loadFeedbacksTab() {
        downloadPdfButton.setDisable(false);  // Enable the PDF download button
        BorderPane content = new BorderPane();  // Create a new BorderPane to hold the content
        new FeedbacksTrend().start(content, isDoctor ? patientSelector : null);  // Load Feedbacks data
        feedbacksTab.setContent(content);  // Set the content for the Feedbacks tab
    }

    private void loadHealthTrendsTab() {
        downloadPdfButton.setDisable(false);  // Enable the PDF download button
        BorderPane content = new BorderPane();  // Create a new BorderPane to hold the content
        new HealthTrendsAndGraphs().start(content, isDoctor ? patientSelector : null);  // Load Health Trends data
        healthTrendsTab.setContent(content);  // Set the content for the Health Trends tab
    }

    private void loadMedicationEffectivenessTab() {
        downloadPdfButton.setDisable(false);  // Enable the PDF download button
        BorderPane content = new BorderPane();  // Create a new BorderPane to hold the content
        new MedicationEffectiveness().start(content, isDoctor ? patientSelector : null);  // Load Medication Effectiveness data
        medicationTab.setContent(content);  // Set the content for the Medication Effectiveness tab
    }

    // Method to export the currently selected tab content to a PDF
    private void exportCurrentTabToPDF() {
        try {
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();  // Get the selected tab
            if (selectedTab == null || selectedTab.getContent() == null) {
                return;  // Exit if no tab is selected or content is empty
            }

            // Capture the snapshot of the tab content and convert it to a BufferedImage
            WritableImage snapshot = selectedTab.getContent().snapshot(new SnapshotParameters(), null);
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);

            // Open a file chooser dialog to select the save location and filename for the PDF
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save PDF Report");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            fileChooser.setInitialFileName("HealthReport.pdf");

            File saveFile = fileChooser.showSaveDialog(null);  // Show the save dialog
            if (saveFile == null) return;  // Exit if no file is selected

            // Save the snapshot as a temporary image file
            File imageFile = new File("report_snapshot.png");
            ImageIO.write(bufferedImage, "png", imageFile);

            // Create a new PDF document
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(saveFile));
            document.open();

            // Add the image to the PDF document and close it
            Image image = Image.getInstance("report_snapshot.png");
            image.scaleToFit(PageSize.A4.getWidth() - 50, PageSize.A4.getHeight() - 50);  // Scale the image to fit the A4 page
            image.setAlignment(Element.ALIGN_CENTER);  // Align the image at the center
            document.add(image);  // Add the image to the document
            document.close();

            // Delete the temporary image file
            imageFile.delete();

            System.out.println("PDF exported to: " + saveFile.getAbsolutePath());  // Print the save location
        } catch (Exception e) {
            e.printStackTrace();  // Print any exceptions for debugging
        }
    }
}