package VideoConsultations;

import Connection.Conn;
import UserManagement.SessionStorage;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class DoctorViewConsultations extends Application {
    private TableView table;  // Table to display consultations
    private DatePicker datePicker;  // Date picker for selecting a date

    @Override
    public void start(Stage primaryStage) {
        // Creating a label for the heading
        Label heading = new Label("Search by Date");
        heading.setFont(new Font("Arial", 14));

        // Setting up the date picker for selecting date
        datePicker = new DatePicker();

        // HBox layout to hold the heading and date picker
        HBox topBox = new HBox(10, heading, datePicker);
        topBox.setPadding(new Insets(10));
        topBox.setAlignment(Pos.CENTER_LEFT);

        // Creating the table for displaying consultations
        table = new TableView<>();
        setupTable();  // Set up columns in the table
        loadAllConsultations();  // Load consultations from the database

        // Buttons for searching, printing, and closing
        Button searchBtn = new Button("Search");
        searchBtn.setOnAction(e -> searchConsultations());  // Search consultations based on selected date

        Button printBtn = new Button("Print");
        printBtn.setOnAction(e -> {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(table.getScene().getWindow())) {
                boolean success = job.printPage(table);
                if (success) {
                    job.endJob();
                }
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> primaryStage.close());  // Close the window

        // HBox layout for holding buttons
        HBox buttonBox = new HBox(10, searchBtn, printBtn, cancelBtn);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        // VBox layout for the entire UI
        VBox root = new VBox(10, topBox, buttonBox, table);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: white;");

        // Setting the scene and title for the primary stage
        Scene scene = new Scene(root, 700, 550);
        primaryStage.setScene(scene);
        primaryStage.setTitle("View All Consultations");
        primaryStage.show();
    }

    @SuppressWarnings("unchecked")
    public void getResults(String query, ObservableList<VideoCall> consultations) {
        // Method to fetch results from the database based on the query
        try (Conn conn = new Conn();
             ResultSet rs = conn.runQuery(query)) {
            while (rs.next()) {
                consultations.add(new VideoCall(
                        rs.getLong("id"),
                        rs.getLong("doctor_id"),
                        rs.getLong("patient_id"),
                        rs.getString("meeting_link"),
                        rs.getTimestamp("scheduled_datetime").toLocalDateTime(),
                        rs.getString("notes"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        table.setItems(consultations);  // Set the table data
    }

    private void loadAllConsultations() {
        // Method to load all consultations for the logged-in doctor
        ObservableList<VideoCall> consultations = FXCollections.observableArrayList();
        String query = "select * from videoconsultations where doctor_id = " + SessionStorage.loggedInUser.getUnique_id() + " order by scheduled_datetime desc";
        getResults(query, consultations);
    }

    private void searchConsultations() {
        // Method to search consultations based on the selected date
        LocalDate date = datePicker.getValue();
        String startOfDay = date + " 00:00:00";
        String endOfDay = date + " 23:59:59";

        ObservableList<VideoCall> consultations = FXCollections.observableArrayList();
        String query = "select * from videoconsultations where doctor_id = " + SessionStorage.loggedInUser.getUnique_id() +
                " AND scheduled_datetime BETWEEN '" + startOfDay + "' AND '" + endOfDay + "'" +" order by scheduled_datetime desc";
        getResults(query, consultations);
    }

    private <T> TableColumn<T, ?> createColumn(String title, String property) {
        // Helper method to create table columns
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }

    private void setupTable() {
        // Setting up columns for the table
        table.getColumns().addAll(
                createColumn("Patient ID", "patientId"),
                createColumn("Scheduled Time", "scheduledDateTime"),
                createColumn("Notes", "note"),
                createColumn("Status", "status"),
                createColumn("Meeting Link", "meetingLink")
        );

        // Adding actions column for copying, starting, completing, and cancelling consultations
        TableColumn<VideoCall, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new TableCell<VideoCall, Void>() {
            private final Button copyBtn = new Button();
            private final Button startBtn = new Button();
            private final Button completeBtn = new Button();
            private final Button cancelBtn = new Button();

            {
                // Copy Button
                ImageView copyIcon = new ImageView(new Image("resources/icons/copyIconRed.png"));
                copyIcon.setFitWidth(20); copyIcon.setFitHeight(20);
                copyBtn.setGraphic(copyIcon);
                copyBtn.setTooltip(new Tooltip("Copy Meeting Link"));
                copyBtn.setStyle("-fx-background-color: transparent;");
                copyBtn.setOnAction(e -> {
                    VideoCall vc = getTableView().getItems().get(getIndex());
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    ClipboardContent content = new ClipboardContent();
                    content.putString(vc.getMeetingLink());
                    clipboard.setContent(content);
                });

                // Start Button
                ImageView startIcon = new ImageView(new Image("resources/icons/VideoCallIcon.png"));
                startIcon.setFitWidth(20); startIcon.setFitHeight(20);
                startBtn.setGraphic(startIcon);
                startBtn.setTooltip(new Tooltip("Start Consultation"));
                startBtn.setStyle("-fx-background-color: transparent;");
                startBtn.setOnAction(e -> {
                    VideoCall vc = getTableView().getItems().get(getIndex());
                    getHostServices().showDocument(vc.getMeetingLink());
                });

                // Complete Button
                ImageView completeIcon = new ImageView(new Image("resources/icons/checkIcon.png"));
                completeIcon.setFitWidth(20); completeIcon.setFitHeight(20);
                completeBtn.setGraphic(completeIcon);
                completeBtn.setTooltip(new Tooltip("Mark as Completed"));
                completeBtn.setStyle("-fx-background-color: transparent;");
                completeBtn.setOnAction(e -> {
                    VideoCall vc = getTableView().getItems().get(getIndex());
                    updateStatus(vc, "COMPLETED");
                });

                // Cancel Button
                ImageView cancelIcon = new ImageView(new Image("resources/icons/cancelIcon.png"));
                cancelIcon.setFitWidth(20); cancelIcon.setFitHeight(20);
                cancelBtn.setGraphic(cancelIcon);
                cancelBtn.setTooltip(new Tooltip("Mark as Cancelled"));
                cancelBtn.setStyle("-fx-background-color: transparent;");
                cancelBtn.setOnAction(e -> {
                    VideoCall vc = getTableView().getItems().get(getIndex());
                    updateStatus(vc, "CANCELLED");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox actions = new HBox(5, copyBtn, startBtn, completeBtn, cancelBtn);
                    actions.setAlignment(Pos.CENTER);
                    setGraphic(actions);
                }
            }
        });
        table.getColumns().add(actionCol);  // Add actions column to the table
    }

    private void updateStatus(VideoCall vc, String newStatus) {
        // Update the status of a consultation (e.g., completed or cancelled)
        String updateQuery = "UPDATE videoconsultations SET status = '" + newStatus +
                "' WHERE id = " + vc.getId();
        try (Conn conn = new Conn()) {
            conn.runUpdate(updateQuery);  // Execute the update query
            loadAllConsultations();  // Reload the consultations after update
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}