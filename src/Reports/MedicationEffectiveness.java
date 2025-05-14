package Reports;

import Connection.Conn;
import UserManagement.SessionStorage;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

public class MedicationEffectiveness {

    // Array of vital column names to track in the report
    private final String[] vitals = {
            "heart_rate", "blood_pressure_systolic", "blood_pressure_diastolic",
            "temperature_fahrenheit", "oxygen_level", "weight_pounds"
    };

    // Map of vital names to display-friendly labels
    private final Map<String, String> vitalLabels = Map.of(
            "heart_rate", "Heart Rate (BPM)",
            "blood_pressure_systolic", "Systolic BP (mmHg)",
            "blood_pressure_diastolic", "Diastolic BP (mmHg)",
            "temperature_fahrenheit", "Temperature (°F)",
            "oxygen_level", "Oxygen Level (%)",
            "weight_pounds", "Weight (Pounds)"
    );

    private LineChart<Number, Number> chart;

    // Method to initialize the report with patient data
    public void start(BorderPane reportArea, ComboBox<String> patientSelector) {
        // Determine patient ID based on selection or use logged-in user's ID
        long patientId = (patientSelector != null && patientSelector.getValue() != null)
                ? Long.parseLong(patientSelector.getValue())
                : SessionStorage.loggedInUser.getUnique_id();

        // Create TabPane to hold individual tabs for each vital
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);  // Prevent closing tabs
        tabPane.setPadding(new Insets(10));  // Add padding to the TabPane

        // Add a Tab for each vital to the TabPane
        for (String vital : vitals) {
            Tab tab = new Tab(vitalLabels.get(vital));  // Set the label for the tab
            tabPane.getTabs().add(tab);
        }

        // Create a chart for each vital and associate it with its corresponding tab
        for (int i = 0; i < tabPane.getTabs().size(); i++) {
            Tab tab = tabPane.getTabs().get(i);
            String title = tab.getText();
            chart = createVitalChart(title, "Days from Prescription", title);
            tab.setContent(chart);
        }

        // Handle tab selection changes, loading data for the selected vital
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                String vt = returnVital(tabPane);  // Get the vital name for the selected tab
                String selectedPatient = patientSelector != null ? patientSelector.getValue() : String.valueOf(patientId);
                if (selectedPatient != null) {
                    // Create a new chart for the selected vital and load the corresponding data
                    chart = createVitalChart(newTab.getText(), "Days from Prescription", newTab.getText());
                    loadVitalData(chart, vt, Long.parseLong(selectedPatient));
                    newTab.setContent(chart);  // Set the chart content for the tab
                } else {
                    // If no patient is selected, load data for the current patient
                    chart = createVitalChart(newTab.getText(), "Days from Prescription", newTab.getText());
                    loadVitalData(chart, vt, patientId);
                    newTab.setContent(chart);
                }
            }
        });

        // Handle patient selection change and update the displayed data
        if (patientSelector != null) {
            patientSelector.setOnAction((e) -> {
                String vt = returnVital(tabPane);
                Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
                String selectedPatient = patientSelector.getValue();
                if (selectedTab != null && selectedPatient != null) {
                    // Re-create the chart and reload data for the selected patient
                    chart = createVitalChart(selectedTab.getText(), "Days from Prescription", selectedTab.getText());
                    loadVitalData(chart, vt, Long.parseLong(selectedPatient));
                    selectedTab.setContent(chart);
                }
            });
        } else {
            // If no patient selector, load data for the logged-in user
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
            String vt = getVitalFromLabel(selectedTab.getText());  // Get the vital based on the label
            chart = createVitalChart(selectedTab.getText(), "Days from Prescription", selectedTab.getText());
            loadVitalData(chart, vt, patientId);
            selectedTab.setContent(chart);  // Set the chart content for the tab
        }

        // Set the TabPane as the center content of the report area
        reportArea.setCenter(tabPane);
    }

    // Method to map a vital's label to its column name in the database
    private String getVitalFromLabel(String label) {
        for (Map.Entry<String, String> entry : vitalLabels.entrySet()) {
            if (entry.getValue().equals(label)) {
                return entry.getKey();  // Return the corresponding vital name
            }
        }
        throw new IllegalArgumentException("No vital found for label: " + label);  // Exception if no match
    }

    // Method to determine the current vital from the selected tab in the TabPane
    private String returnVital(TabPane tabPane) {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        for (String vital: vitals) {
            if (vitalLabels.get(vital).equals(selectedTab.getText())) {
                return vital;  // Return the vital name corresponding to the tab's label
            }
        }
        return null;
    }

    // Method to create a LineChart for displaying vital data
    private LineChart<Number, Number> createVitalChart(String title, String xLabel, String yLabel) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setAutoRanging(true);  // Automatically adjust x-axis range based on data
        xAxis.setLabel(xLabel);  // Set x-axis label
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yLabel);  // Set y-axis label

        // Create the chart and configure its appearance
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);  // Set chart title
        chart.setPrefHeight(400);  // Set preferred height for the chart
        chart.setLegendVisible(true);  // Make the legend visible
        chart.setCreateSymbols(true);  // Display symbols at data points
        return chart;  // Return the created chart
    }

    // Method to load and populate vital data for the selected patient
    private void loadVitalData(LineChart<Number, Number> chart, String vitalColumn, long patientId) {
        chart.getData().clear();  // Clear existing data in the chart
        try (Conn conn = new Conn()) {
            // Step 1: Fetch and cache prescriptions for the patient
            ArrayList<Map.Entry<String, LocalDateTime>> prescriptionList = new ArrayList<>();
            ResultSet prescriptions = conn.runQuery("SELECT medicine_name, timestamp FROM prescription WHERE patient_id = " + patientId);
            while (prescriptions.next()) {
                String medicine = prescriptions.getString("medicine_name");
                LocalDateTime baseTime = prescriptions.getTimestamp("timestamp").toLocalDateTime();
                prescriptionList.add(Map.entry(medicine, baseTime));  // Store medicine and prescription time
            }

            // Step 2: For each prescription, fetch vital data and populate the chart
            for (Map.Entry<String, LocalDateTime> entry : prescriptionList) {
                String medicine = entry.getKey();
                LocalDateTime baseTime = entry.getValue();

                // Query to fetch vital data for the given vital column, patient, and prescription time range
                String vitalQuery = String.format(
                        "SELECT timestamp, %s FROM vitals WHERE patient_id = %d AND timestamp BETWEEN '%s' AND '%s' ORDER BY timestamp",
                        vitalColumn, patientId,
                        baseTime.minusDays(7),  // Start date (7 days before prescription)
                        baseTime.plusDays(14)   // End date (14 days after prescription)
                );

                // Execute the query and process the result
                ResultSet vitals = conn.runQuery(vitalQuery);
                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                series.setName(medicine);  // Set the series name to the medicine name

                while (vitals.next()) {
                    Timestamp timestamp = vitals.getTimestamp("timestamp");
                    LocalDateTime time = timestamp.toLocalDateTime();
                    long offset = java.time.Duration.between(baseTime, time).toDays();  // Calculate days from prescription
                    double value = vitals.getDouble(vitalColumn);  // Get the vital value
                    series.getData().add(new XYChart.Data<>(offset, value));  // Add data point to the series
                }

                // Add the series to the chart if it contains data
                if (!series.getData().isEmpty()) {
                    Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), ev -> {
                        chart.getData().add(series);  // Add the series to the chart with animation
                    }));
                    timeline.play();  // Play the animation
                }
            }
        } catch (Exception e) {
            e.printStackTrace();  // Handle any exceptions that occur during data retrieval
        }
    }
}