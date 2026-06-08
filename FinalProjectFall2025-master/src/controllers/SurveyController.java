package controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;

import java.sql.*;


public class SurveyController
{

    // Database configuration constants
    private static final String DATABASE_URL ="jdbc:mysql://www.papademas.net:3307/510sp?autoReconnect=true&useSSL=false";
    private static final String DB_USERNAME ="sp510";
    private static final String DB_PASS ="iit1";

    // Severity level constants
    private static final String SEVERITY_HIGH ="High";
    private static final String SEVERITY_MEDIUM ="Medium";

    // UI Components
    @FXML private BarChart<String, Number>alertsChart;

    // Initialize chart when FXML loads Automatically called by JavaFX
     
    @FXML
    private void initialize()
    {
        loadChartData();
    }

    //Load and display suspicious activity data in chart Groups data by activity type and severity level
     
    private void loadChartData()
    {
        String query = "SELECT activity_type,severity,COUNT(*) AS cnt "+ "FROM suspicious_activity "+"GROUP BY activity_type, severity";

        XYChart.Series<String,Number> highSeveritySeries =createChartSeries(SEVERITY_HIGH);
        XYChart.Series<String,Number> mediumSeveritySeries =createChartSeries(SEVERITY_MEDIUM);

        try (Connection conn = DriverManager.getConnection(DATABASE_URL,DB_USERNAME,DB_PASS);
             PreparedStatement prep = conn.prepareStatement(query);
             ResultSet results = prep.executeQuery()) 
        {

            while (results.next()) 
            {
                String activityType =results.getString("activity_type");
                String severityLevel =results.getString("severity");
                int activityCount =results.getInt("cnt");

                addDataToSeries(activityType,severityLevel,activityCount,highSeveritySeries,mediumSeveritySeries);
            }

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }

        updateChart(highSeveritySeries, mediumSeveritySeries);
    }

    // Create a new chart series with given name
     
    private XYChart.Series<String, Number> createChartSeries(String seriesName)
    {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);
        return series;
    }

    //Add data point to appropriate severity series
     
    private void addDataToSeries(String activityType,String severity,int count,XYChart.Series<String, Number> highSeries,XYChart.Series<String, Number> mediumSeries)
    {
        if (severity.equalsIgnoreCase(SEVERITY_HIGH)) 
        {
            highSeries.getData().add(new XYChart.Data<>(activityType,count));
        } else if (severity.equalsIgnoreCase(SEVERITY_MEDIUM)) {
            mediumSeries.getData().add(new XYChart.Data<>(activityType,count));
        }
    }

   
    private void updateChart(XYChart.Series<String, Number>highSeries,XYChart.Series<String, Number>mediumSeries)
    {
        alertsChart.getData().clear();
        alertsChart.getData().addAll(highSeries,mediumSeries);
    }
}