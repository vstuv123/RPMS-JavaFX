package Reports;

import Connection.Conn;
import UserManagement.SessionStorage;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.sql.ResultSet;

public class HealthTrendsAndGraphs {

    // Method to initialize the report area and setup charts
    public void start(BorderPane reportArea, ComboBox<String> patientSelector) {
        GridPane chartsGrid = new GridPane();  // Grid layout to hold charts
        chartsGrid.setHgap(10);  // Horizontal gap between charts
        chartsGrid.setVgap(10);  // Vertical gap between charts
        chartsGrid.setPadding(new Insets(10));  // Padding around the grid

        // Initially load empty charts before patient data is available
        loadVitals((SessionStorage.loggedInUser.getRole().equals("Doctor")) ? null : SessionStorage.loggedInUser.getUnique_id(), chartsGrid);

        // If a patient selector is provided, set up action to load data for selected patient
        if (patientSelector != null) {
            patientSelector.setOnAction(e -> {
                String selectedPatient = patientSelector.getValue();
                if (selectedPatient != null) {
                    loadVitals(Long.parseLong(selectedPatient), chartsGrid);  // Load vitals data for selected patient
                }
            });
        }

        // Set the GridPane containing the charts as the center of the report area
        reportArea.setCenter(chartsGrid);
    }

    // Method to load vitals data and populate the charts
    private void loadVitals(Long patientId, GridPane chartsGrid) {
        chartsGrid.getChildren().clear();  // Clear existing charts before loading new data

        // Create charts for different health vitals
        LineChart<Number, Number> heartRateChart = createChart("Heart Rate", "Time", "BPM");
        LineChart<Number, Number> temperatureChart = createChart("Temperature", "Time", "Fahrenheit");
        LineChart<Number, Number> oxygenChart = createChart("Oxygen Level", "Time", "%");
        LineChart<Number, Number> bloodPressureChart = createChart("Blood Pressure", "Time", "mmHg");

        // Step 1: Add empty charts first to the grid
        chartsGrid.add(heartRateChart, 0, 0);
        chartsGrid.add(temperatureChart, 1, 0);
        chartsGrid.add(oxygenChart, 0, 1);
        chartsGrid.add(bloodPressureChart, 1, 1);

        // If no patient ID is provided, exit the method
        if (patientId == null) return;

        // Create series to hold data points for each vital measurement
        XYChart.Series<Number, Number> heartRateSeries = new XYChart.Series<>();
        XYChart.Series<Number, Number> tempSeries = new XYChart.Series<>();
        XYChart.Series<Number, Number> oxygenSeries = new XYChart.Series<>();
        XYChart.Series<Number, Number> bpSystolicSeries = new XYChart.Series<>();
        XYChart.Series<Number, Number> bpDiastolicSeries = new XYChart.Series<>();

        // Set names for each series to be displayed in the legend
        heartRateSeries.setName("Heart Rate");
        tempSeries.setName("Temperature");
        oxygenSeries.setName("Oxygen Level");
        bpSystolicSeries.setName("Systolic");
        bpDiastolicSeries.setName("Diastolic");

        try (Conn conn = new Conn();
             ResultSet rs = conn.runQuery("SELECT * FROM vitals WHERE patient_id = " + patientId + " ORDER BY timestamp ASC")) {

            // Variable to count the time intervals for x-axis values
            int counter = 1;

            // Process each record returned from the database
            while (rs.next()) {
                // Retrieve values for each vital from the result set
                double heartRate = rs.getDouble("heart_rate");
                int systolic = rs.getInt("blood_pressure_systolic");
                int diastolic = rs.getInt("blood_pressure_diastolic");
                double temp = rs.getDouble("temperature_fahrenheit");
                int oxygen = rs.getInt("oxygen_level");

                // Add data points to each series
                heartRateSeries.getData().add(new XYChart.Data<>(counter, heartRate));
                tempSeries.getData().add(new XYChart.Data<>(counter, temp));
                oxygenSeries.getData().add(new XYChart.Data<>(counter, oxygen));
                bpSystolicSeries.getData().add(new XYChart.Data<>(counter, systolic));
                bpDiastolicSeries.getData().add(new XYChart.Data<>(counter, diastolic));

                counter++;  // Increment the counter for each data point
            }

        } catch (Exception e) {
            e.printStackTrace();  // Handle any exceptions that occur during database interaction
        }

        // Step 2: Add the data to the charts after a short delay to allow animation
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), ev -> {
            // Add the data series to the charts
            heartRateChart.getData().add(heartRateSeries);
            temperatureChart.getData().add(tempSeries);
            oxygenChart.getData().add(oxygenSeries);
            bloodPressureChart.getData().addAll(bpSystolicSeries, bpDiastolicSeries);

            // Add tooltips to blood pressure data points
            for (XYChart.Data<Number, Number> data : bpSystolicSeries.getData()) {
                Tooltip.install(data.getNode(), new Tooltip(data.getYValue() + " mmHg (Systolic)"));
            }
            for (XYChart.Data<Number, Number> data : bpDiastolicSeries.getData()) {
                Tooltip.install(data.getNode(), new Tooltip(data.getYValue() + " mmHg (Diastolic)"));
            }
        }));

        // Play the animation to add data to the charts
        timeline.play();
    }

    // Method to create a line chart with the specified title and axis labels
    private LineChart<Number, Number> createChart(String title, String xLabel, String yLabel) {
        NumberAxis xAxis = new NumberAxis();  // x-axis for the chart (time)
        NumberAxis yAxis = new NumberAxis();  // y-axis for the chart (vital value)

        // Set axis labels
        xAxis.setLabel(xLabel);
        yAxis.setLabel(yLabel);

        // Create the line chart with the specified axes
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);  // Set the title of the chart
        chart.setPrefHeight(300);  // Set preferred height for the chart
        chart.setPrefWidth(460);   // Set preferred width for the chart
        chart.setLegendVisible(true);  // Make the legend visible
        chart.setCreateSymbols(true);  // Allow chart to display symbols on the lines
        return chart;  // Return the created chart
    }
}