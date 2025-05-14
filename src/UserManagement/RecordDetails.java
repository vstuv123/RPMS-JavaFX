package UserManagement;

import Helper.Helper;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.*;
import javafx.scene.text.Font;
import Connection.Conn;
import javafx.scene.image.*;
import javafx.print.PrinterJob;
import javafx.scene.control.TableView;

import java.sql.*;
import java.util.Objects;

public class RecordDetails<T> extends Application {

    private ComboBox<String> idComboBox;
    private TableView<T> table;
    private String tableName;

    // Constructor accepts tableName to determine which table to work with (e.g., Patient, Doctor)
    public RecordDetails(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public void start(Stage primaryStage) {
        // Setting up the header with search functionality
        Label heading = new Label("Search by ID");
        heading.setFont(new Font("Arial", 14));

        // ComboBox to select ID for searching specific records
        idComboBox = new ComboBox<>();
        loadIds();  // Populating the ComboBox with available IDs

        // Creating a top bar with the heading and the ComboBox for ID selection
        HBox topBox = new HBox(10, heading, idComboBox);
        topBox.setPadding(new Insets(10));
        topBox.setAlignment(Pos.CENTER_LEFT);

        // Setting up the table to display records
        table = new TableView<>();
        setupTable();  // Defining the columns for the table
        loadAllRecords();  // Loading all records initially

        // Buttons for actions: search, print, add, and update (if needed)
        Button searchBtn = new Button("Search");
        searchBtn.setOnAction(e -> searchRecord());  // Search functionality

        Button printBtn = new Button("Print");
        printBtn.setOnAction(e -> {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(table.getScene().getWindow())) {
                boolean success = job.printPage(table);  // Printing the table contents
                if (success) {
                    job.endJob();  // End the print job if successful
                }
            }
        });

        Button addBtn = new Button("Add");
        addBtn.setOnAction(e -> {
            new NewUserRegistration().start(new Stage());  // Open new user registration window
        });

        // Setting up a box for the action buttons
        HBox buttonBox = new HBox(10, searchBtn, printBtn, addBtn);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        // If the table is not "Admin", allow the option to update records
        if (!tableName.equals("Admin")) {
            Button updateBtn = new Button("Update");
            updateBtn.setOnAction(e -> {
                if (tableName.equals("Patient")) {
                    new UpdatePatientDoctor("Patient").start(new Stage());  // Open update for patient
                } else if (tableName.equals("Doctor")) {
                    new UpdatePatientDoctor("Doctor").start(new Stage());  // Open update for doctor
                }
            });
            buttonBox.getChildren().add(updateBtn);  // Add the update button
        }

        // Adding the Cancel button functionality
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> primaryStage.close());  // Close the window
        buttonBox.getChildren().add(cancelBtn);

        // Set up the main layout with the top bar, button box, and table
        VBox root = new VBox(10, topBox, buttonBox, table);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: white;");

        // Set the scene and show the stage
        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle(tableName + " Details");
        primaryStage.show();
    }

    // Load all available IDs from the table to populate the ComboBox
    private void loadIds() {
        String query = "select id from " + tableName;
        try (Conn conn = new Conn();
             ResultSet rs = conn.runQuery(query)) {

            // Add each ID from the result set to the ComboBox
            while (rs.next()) {
                idComboBox.getItems().add(rs.getString("id"));
            }

        } catch (SQLException e) {
            e.printStackTrace();  // Print errors if any
        }
    }

    // Load all records for the table
    @SuppressWarnings("unchecked")
    private void loadAllRecords() {
        ObservableList<T> records = FXCollections.observableArrayList();
        try (Conn conn = new Conn();
             ResultSet rs = conn.runQuery("SELECT * FROM " + tableName)) {

            // Depending on the table, create different types of objects for the records
            while (rs.next()) {
                if (tableName.equals("Patient")) {
                    long assignedTo = rs.getLong("assignedTo");
                    String doctorEmail = Objects.requireNonNull(Helper.fetchDoctor(assignedTo)).getEmail();

                    // Add Patient record to the list
                    records.add((T) new Patient(Long.parseLong(rs.getString("id")), rs.getString
                            ("name"), rs.getString("email"), rs.getString("contactNumber"),
                            rs.getString("dob"), rs.getString("role"), rs.getString
                            ("address"), rs.getString("emergencyEmail"), assignedTo, assignedTo != 0 ?  doctorEmail : null)
                    );
                } else if (tableName.equals("Doctor")) {
                    // Add Doctor record to the list
                    records.add((T) new Doctor(Long.parseLong(rs.getString("id")), rs.getString
                            ("name"), rs.getString("email"), rs.getString("contactNumber"), rs.getString("specialization"),
                            rs.getString("dob"), rs.getString("role"), rs.getString
                            ("address")
                    ));
                } else {
                    // Add Admin record to the list
                    records.add((T) new Administrator(Long.parseLong(rs.getString("id")), rs.getString
                            ("name"), rs.getString("email"), rs.getString("contactNumber"),
                            rs.getString("dob"), rs.getString("role"), rs.getString
                            ("address")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Print errors if any
        }
        table.setItems(records);  // Update the table with fetched records
    }

    // Search records by selected ID from the ComboBox
    @SuppressWarnings("unchecked")
    private void searchRecord() {
        long id = 0;
        String idInStr = idComboBox.getValue();
        if (idInStr != null) {
            id = Long.parseLong(idInStr);  // Parse the selected ID
        }
        ObservableList<T> records = FXCollections.observableArrayList();
        String query = "select * from " + tableName + " where id='" + id + "'";

        try (Conn conn = new Conn(); ResultSet rs = conn.runQuery(query)) {

            // Create and add records depending on the table
            while (rs.next()) {
                if (tableName.equals("Patient")) {
                    long assignedTo = rs.getLong("assignedTo");
                    String doctorEmail = Objects.requireNonNull(Helper.fetchDoctor(assignedTo)).getEmail();

                    records.add((T) new Patient(Long.parseLong(rs.getString("id")), rs.getString
                            ("name"), rs.getString("email"), rs.getString("contactNumber"),
                            rs.getString("dob"), rs.getString("role"), rs.getString
                            ("address"), rs.getString("emergencyEmail"), assignedTo, assignedTo != 0 ? doctorEmail : null)
                    );
                } else if (tableName.equals("Doctor")) {
                    records.add((T) new Doctor(Long.parseLong(rs.getString("id")), rs.getString
                            ("name"), rs.getString("email"), rs.getString("contactNumber"),
                            rs.getString("specialization"), rs.getString("dob"), rs.getString("role"), rs.getString
                            ("address")
                    ));
                }else {
                    records.add((T) new Administrator(Long.parseLong(rs.getString("id")), rs.getString
                            ("name"), rs.getString("email"), rs.getString("contactNumber"),
                            rs.getString("dob"), rs.getString("role"), rs.getString
                            ("address")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Print errors if any
        }
        table.setItems(records);  // Update the table with the searched records
    }

    // Helper method to create table columns
    private <T> TableColumn<T, ?> createColumn(String title, String property) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }

    // Set up the table columns based on the table name (Patient, Doctor, Admin)
    @SuppressWarnings("unchecked")
    private void setupTable() {
        table.getColumns().addAll(
                createColumn("ID", "unique_id"),
                createColumn("Name", "name"),
                createColumn("Email", "email"),
                createColumn("ContactNumber", "contactNumber")
        );
        if (tableName.equals("Doctor")) {
            table.getColumns().add(
                    createColumn("Specialization", "specialization")
            );
        }
        table.getColumns().addAll(
                createColumn("Dob", "dob"),
                createColumn("Role", "role"),
                createColumn("Address", "address")
        );
        if (tableName.equals("Patient")) {
            table.getColumns().addAll(
                    createColumn("EmergencyEmail", "emergencyEmail"),
                    createColumn("AssignedTo", "assignedTo")
            );
        }

        // If not Admin, add a column for actions (e.g., delete button)
        if (!tableName.equals("Admin")) {
            TableColumn<T, Void> actionCol = new TableColumn<>("Actions");
            actionCol.setCellFactory(col -> new TableCell<T, Void>() {
                private final Button deleteButton = new Button();

                {
                    Image deleteImage = new Image("resources/icons/delete.png");
                    ImageView deleteImageView = new ImageView(deleteImage);
                    deleteImageView.setFitHeight(20);
                    deleteImageView.setFitWidth(20);
                    deleteButton.setGraphic(deleteImageView);

                    // Delete action for the button
                    deleteButton.setOnAction(event -> {
                        T t = getTableView().getItems().get(getIndex());
                        deleteRecord(t);  // Call delete record method
                    });
                }

                @Override
                public void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);  // No action if empty
                    } else {
                        HBox hbox = new HBox(deleteButton);  // Add delete button
                        hbox.setAlignment(Pos.CENTER);
                        hbox.setPadding(new Insets(5));
                        setGraphic(hbox);  // Set the graphic as the button
                    }
                }
            });
            table.getColumns().add(actionCol);  // Add the action column
        }
    }

    // Helper method for deleting records
    private <T> void deleteRecord(T t) {
        long id = 0;
        if (t instanceof Patient) {
            id = ((Patient) t).getUnique_id();
        } else if (t instanceof Doctor) {
            id = ((Doctor) t).getUnique_id();
        }

        String idInString = String.valueOf(id);

        if (Helper.fetchDoctor(id) != null) {
            updateAssignedTo(id);  // Update assigned doctor if needed
        }

        int i = 0;
        String queryTableName = "Login";

        // Deleting from multiple tables (Login and the relevant table)
        while (i < 2) {
            String query = "delete from " + queryTableName + " where id='" + id + "'";
            try (Conn c = new Conn()) {
                c.runUpdate(query);
                if (i > 0) {
                    // Display success alert after the second deletion
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("User Deleted Successfully");
                    Helper.insertLog(idInString, "User Deletion",
                            "User Deleted with ID " + id + " at " + Helper.currentTimestamp());
                    loadAllRecords();  // Reload records after deletion
                    alert.showAndWait();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Helper.insertLog(idInString, "Failed User Deletion",
                        "User Deletion failed with ID " + id + " at " + Helper.currentTimestamp());
                Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
                alert.showAndWait();
                break;
            }
            i++;
            queryTableName = tableName;  // Switch to the relevant table
        }
    }

    // Helper method to handle assignedTo update when a record is deleted
    private void updateAssignedTo(long oldAssignedTo) {
        int i = 0;
        String tableName = "Login";
        while (i < 2) {
            String query = "Update " + tableName + " set assignedTo = null where assignedTo = " + oldAssignedTo;
            try (Conn conn = new Conn()) {
                conn.runUpdate(query);  // Update assigned doctor field
            } catch (SQLException e) {
                e.printStackTrace();
                break;
            }
            i++;
            tableName = "Patient";  // Switch table for Patient
        }
    }
}
