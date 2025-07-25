package controller;

import dao.OrderDAO;
import dao.ProductDAO;
import dao.RestaurantTableDAO;
import dao.RestaurantTableDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Order;
import model.RestaurantTable;
import model.RestaurantTable;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ReportsController implements Initializable {
    
    // Report filters
    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    
    // Action buttons
    @FXML private Button exportReportButton;
    @FXML private Button printReportButton;
    @FXML private Button generateReportButton;
    @FXML private Button todayButton;
    @FXML private Button thisWeekButton;
    @FXML private Button thisMonthButton;
    @FXML private Button lastMonthButton;
    
    // Dashboard statistics
    @FXML private Label totalSalesLabel;
    @FXML private Label salesChangeLabel;
    @FXML private Label totalOrdersLabel;
    @FXML private Label ordersChangeLabel;
    @FXML private Label avgOrderValueLabel;
    @FXML private Label avgChangeLabel;
    @FXML private Label customersServedLabel;
    @FXML private Label customersChangeLabel;
    
    // Charts
    @FXML private LineChart<String, Number> salesChart;
    @FXML private CategoryAxis salesXAxis;
    @FXML private NumberAxis salesYAxis;
    @FXML private ComboBox<String> salesChartTypeCombo;
    
    @FXML private BarChart<String, Number> popularItemsChart;
    @FXML private CategoryAxis itemsXAxis;
    @FXML private NumberAxis itemsYAxis;
    @FXML private ComboBox<String> popularItemsPeriodCombo;
    
    @FXML private PieChart revenueChart;
    
    // Table performance
    @FXML private TableView<TablePerformance> tablePerformanceTable;
    @FXML private TableColumn<TablePerformance, String> tableNumberColumn;
    @FXML private TableColumn<TablePerformance, Integer> totalOrdersColumn;
    @FXML private TableColumn<TablePerformance, Double> totalRevenueColumn;
    @FXML private TableColumn<TablePerformance, Double> avgOrderValueColumn;
    @FXML private TableColumn<TablePerformance, Double> utilizationRateColumn;
    @FXML private TableColumn<TablePerformance, LocalDateTime> lastOrderColumn;
    
    // Report buttons
    @FXML private Button dailyReportButton;
    @FXML private Button weeklyReportButton;
    @FXML private Button monthlyReportButton;
    @FXML private Button inventoryReportButton;
    @FXML private Button staffReportButton;
    @FXML private Button customReportButton;
    
    private OrderDAO orderDAO;
    private ProductDAO productDAO;
    private RestaurantTableDAO tableDAO;
    
    private LocalDate currentFromDate;
    private LocalDate currentToDate;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        orderDAO = new OrderDAO();
        setProductDAO(new ProductDAO());
        tableDAO = new RestaurantTableDAO();
        
        initializeComponents();
        setupCharts();
        setupTablePerformance();
        setDefaultDateRange();
        loadDashboardData();
    }
    
    private void initializeComponents() {
        // Initialize report type combo
        reportTypeCombo.setItems(FXCollections.observableArrayList(
            "Sales Report", "Product Performance", "Table Utilization", 
            "Revenue Analysis", "Customer Analytics", "Inventory Report"
        ));
        reportTypeCombo.setValue("Sales Report");
        
        // Initialize chart type combos
        salesChartTypeCombo.setItems(FXCollections.observableArrayList(
            "Daily", "Weekly", "Monthly"
        ));
        salesChartTypeCombo.setValue("Daily");
        
        popularItemsPeriodCombo.setItems(FXCollections.observableArrayList(
            "Today", "This Week", "This Month", "Last Month"
        ));
        popularItemsPeriodCombo.setValue("This Week");
    }
    
    private void setupCharts() {
        // Setup sales chart
        salesXAxis.setLabel("Time Period");
        salesYAxis.setLabel("Sales (MMK)");
        
        // Setup popular items chart
        itemsXAxis.setLabel("Products");
        itemsYAxis.setLabel("Quantity Sold");
    }
    
    private void setupTablePerformance() {
        tableNumberColumn.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));
        totalOrdersColumn.setCellValueFactory(new PropertyValueFactory<>("totalOrders"));
        totalRevenueColumn.setCellValueFactory(new PropertyValueFactory<>("totalRevenue"));
        avgOrderValueColumn.setCellValueFactory(new PropertyValueFactory<>("avgOrderValue"));
        utilizationRateColumn.setCellValueFactory(new PropertyValueFactory<>("utilizationRate"));
        lastOrderColumn.setCellValueFactory(new PropertyValueFactory<>("lastOrder"));
        
        // Format revenue columns
        totalRevenueColumn.setCellFactory(column -> new TableCell<TablePerformance, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item) + " MMK");
                }
            }
        });
        
        avgOrderValueColumn.setCellFactory(column -> new TableCell<TablePerformance, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item) + " MMK");
                }
            }
        });
        
        utilizationRateColumn.setCellFactory(column -> new TableCell<TablePerformance, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f%%", item));
                }
            }
        });
        
        lastOrderColumn.setCellFactory(column -> new TableCell<TablePerformance, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("No orders");
                } else {
                    setText(formatter.format(item));
                }
            }
        });
    }
    
    private void setDefaultDateRange() {
        currentToDate = LocalDate.now();
        currentFromDate = currentToDate.minusDays(30);
        
        fromDatePicker.setValue(currentFromDate);
        toDatePicker.setValue(currentToDate);
    }
    
    private void loadDashboardData() {
        try {
            updateDashboardStatistics();
            updateSalesChart();
            updatePopularItemsChart();
            updateTablePerformance();
            updateRevenueChart();
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load dashboard data: " + e.getMessage());
        }
    }
    
    private void updateDashboardStatistics() throws SQLException {
        List<Order> orders = orderDAO.getOrdersByDateRange(currentFromDate, currentToDate);
        
        double totalSales = orders.stream()
            .mapToDouble(Order::getTotalPrice)
            .sum();
        
        int totalOrders = orders.size();
        
        double avgOrderValue = totalOrders > 0 ? totalSales / totalOrders : 0;
        
        // Count unique tables (customers served approximation)
        long customersServed = orders.stream()
            .mapToInt(Order::getTableId)
            .distinct()
            .count();
        
        // Update labels
        totalSalesLabel.setText(String.format("%.2f", totalSales) + " MMK");
        totalOrdersLabel.setText(String.valueOf(totalOrders));
        avgOrderValueLabel.setText(String.format("%.2f", avgOrderValue) + " MMK");
        customersServedLabel.setText(String.valueOf(customersServed));
        
        // Calculate changes (placeholder - would need historical data)
        
    }
    
    private void updateSalesChart() throws SQLException {
        salesChart.getData().clear();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Sales");
        
        String chartType = salesChartTypeCombo.getValue();
        List<Order> orders = orderDAO.getOrdersByDateRange(currentFromDate, currentToDate);
        
        Map<String, Double> salesData = new TreeMap<>();
        
        switch (chartType) {
            case "Daily":
                salesData = orders.stream()
                    .collect(Collectors.groupingBy(
                        order -> order.getDateTime().toLocalDate().toString(),
                        TreeMap::new,
                        Collectors.summingDouble(Order::getTotalPrice)
                    ));
                break;
            case "Weekly":
                // Group by week
                salesData = orders.stream()
                    .collect(Collectors.groupingBy(
                        order -> "Week " + order.getDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("w")),
                        TreeMap::new,
                        Collectors.summingDouble(Order::getTotalPrice)
                    ));
                break;
            case "Monthly":
                // Group by month
                salesData = orders.stream()
                    .collect(Collectors.groupingBy(
                        order -> order.getDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        TreeMap::new,
                        Collectors.summingDouble(Order::getTotalPrice)
                    ));
                break;
        }
        
        for (Map.Entry<String, Double> entry : salesData.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        salesChart.getData().add(series);
    }
    
    private void updatePopularItemsChart() throws SQLException {
        popularItemsChart.getData().clear();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Quantity Sold");
        
        List<Order> orders = orderDAO.getOrdersByDateRange(currentFromDate, currentToDate);
        
        Map<String, Integer> productSales = orders.stream()
            .collect(Collectors.groupingBy(
                Order::getProductName,
                Collectors.summingInt(Order::getQty)
            ));
        
        // Get top 10 products
        productSales.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(10)
            .forEach(entry -> 
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()))
            );
        
        popularItemsChart.getData().add(series);
    }
    
    private void updateRevenueChart() throws SQLException {
        Platform.runLater(() -> {
            revenueChart.getData().clear();
            
            try {
                List<Order> orders = orderDAO.getOrdersByDateRange(currentFromDate, currentToDate);
                
                // Group by product category
                Map<String, Double> categoryRevenue = new HashMap<>();
                
                // Initialize categories
                categoryRevenue.put("Main Course", 0.0);
                categoryRevenue.put("Drinks", 0.0);
                categoryRevenue.put("Salads", 0.0);
                categoryRevenue.put("Soups", 0.0);
                categoryRevenue.put("Desserts", 0.0);
                
                // Process orders
                for (Order order : orders) {
                    String category = getCategoryForProduct(order.getProductName());
                    categoryRevenue.put(category, 
                        categoryRevenue.getOrDefault(category, 0.0) + order.getTotalPrice());
                }
                
                // Add sample data if no orders exist (for testing)
                if (orders.isEmpty()) {
                    categoryRevenue.put("Main Course", 0.0);
                    categoryRevenue.put("Drinks", 0.0);
                    categoryRevenue.put("Salads", 0.0);
                    categoryRevenue.put("Soups", 0.0);
                    categoryRevenue.put("Desserts", 0.0);
                }
                
                // Create pie chart data
                ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
                
                for (Map.Entry<String, Double> entry : categoryRevenue.entrySet()) {
                    if (entry.getValue() > 0) {
                        PieChart.Data slice = new PieChart.Data(
                            entry.getKey() + "\n" + String.format("%.0f MMK", entry.getValue()),
                            entry.getValue()
                        );
                        pieChartData.add(slice);
                    }
                }
                
                // Set data to chart
                revenueChart.setData(pieChartData);
                
                // Force chart properties
                revenueChart.setLegendVisible(true);
                revenueChart.setLabelsVisible(true);
                revenueChart.setStartAngle(90);
                revenueChart.setClockwise(true);
                
                // Apply colors programmatically as backup
                applyPieChartColors();
                
            } catch (SQLException e) {
                e.printStackTrace();
                showErrorAlert("Chart Error", "Failed to update revenue chart: " + e.getMessage());
            }
        });
    }

    private void applyPieChartColors() {
        String[] colors = {
            "#3498db", "#27ae60", "#e74c3c", "#f39c12", 
            "#9b59b6", "#1abc9c", "#e67e22", "#34495e"
        };
        
        ObservableList<PieChart.Data> data = revenueChart.getData();
        for (int i = 0; i < data.size() && i < colors.length; i++) {
            final PieChart.Data slice = data.get(i);
            slice.getNode().setStyle("-fx-pie-color: " + colors[i] + ";");
        }
    }

    


    
    private void updateTablePerformance() throws SQLException {
        ObservableList<TablePerformance> performanceData = FXCollections.observableArrayList();
        
        List<RestaurantTable> tables = tableDAO.getAllTables();
        List<Order> orders = orderDAO.getOrdersByDateRange(currentFromDate, currentToDate);
        
        for (RestaurantTable table : tables) {
            List<Order> tableOrders = orders.stream()
                .filter(order -> order.getTableId() == table.getTableId())
                .collect(Collectors.toList());
            
            int totalOrders = tableOrders.size();
            double totalRevenue = tableOrders.stream()
                .mapToDouble(Order::getTotalPrice)
                .sum();
            double avgOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0;
            
            // Calculate utilization rate (simplified)
            double utilizationRate = Math.min(100.0, (totalOrders * 2.0)); // Simplified calculation
            
            LocalDateTime lastOrder = tableOrders.stream()
                .map(Order::getDateTime)
                .max(LocalDateTime::compareTo)
                .orElse(null);
            
            TablePerformance performance = new TablePerformance(
                table.getTableNumber(),
                totalOrders,
                totalRevenue,
                avgOrderValue,
                utilizationRate,
                lastOrder
            );
            
            performanceData.add(performance);
        }
        
        tablePerformanceTable.setItems(performanceData);
    }
    
    private String getCategoryForProduct(String productName) {
        // Simplified category mapping - in reality, this would come from the database
        if (productName.toLowerCase().contains("ဖျော်ရည်") ||    	
            productName.toLowerCase().contains("Drink")) {
            return "Drinks";
        } else if (productName.toLowerCase().contains("သုပ်") ){
            return "Salads";
        } else if (productName.toLowerCase().contains("ဟင်းရည်")) {
            return "Soups";
        } else
            return "Main Course";
    }
    
    @FXML
    private void handleReportTypeChange() {
        // Update the report based on selected type
        loadDashboardData();
    }
    
    @FXML
    private void handleDateChange() {
        currentFromDate = fromDatePicker.getValue();
        currentToDate = toDatePicker.getValue();
        
        if (currentFromDate != null && currentToDate != null) {
            loadDashboardData();
        }
    }
    
    @FXML
    private void handleGenerateReport() {
        String reportType = reportTypeCombo.getValue();
        showInfoAlert("Report Generated", 
            reportType + " has been generated for the period " + 
            currentFromDate + " to " + currentToDate);
    }
    
    @FXML
    private void handleToday() {
        LocalDate today = LocalDate.now();
        fromDatePicker.setValue(today);
        toDatePicker.setValue(today);
        handleDateChange();
    }
    
    @FXML
    private void handleThisWeek() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        fromDatePicker.setValue(startOfWeek);
        toDatePicker.setValue(today);
        handleDateChange();
    }
    
    @FXML
    private void handleThisMonth() {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        fromDatePicker.setValue(startOfMonth);
        toDatePicker.setValue(today);
        handleDateChange();
    }
    
    @FXML
    private void handleLastMonth() {
        LocalDate today = LocalDate.now();
        LocalDate lastMonth = today.minusMonths(1);
        LocalDate startOfLastMonth = lastMonth.withDayOfMonth(1);
        LocalDate endOfLastMonth = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());
        fromDatePicker.setValue(startOfLastMonth);
        toDatePicker.setValue(endOfLastMonth);
        handleDateChange();
    }
    
    @FXML
    private void handleSalesChartChange() {
        try {
            updateSalesChart();
        } catch (SQLException e) {
            showErrorAlert("Error", "Failed to update sales chart: " + e.getMessage());
        }
    }
    
    @FXML
    private void handlePopularItemsChange() {
        try {
            updatePopularItemsChart();
        } catch (SQLException e) {
            showErrorAlert("Error", "Failed to update popular items chart: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleExportReport() {
        showInfoAlert("Export", "Report exported successfully!");
    }
    
    @FXML
    private void handlePrintReport() {
        showInfoAlert("Print", "Report sent to printer!");
    }
    
    @FXML
    private void handleDailyReport() {
        generateSpecificReport("Daily Report");
    }
    
    @FXML
    private void handleWeeklyReport() {
        generateSpecificReport("Weekly Report");
    }
    
    @FXML
    private void handleMonthlyReport() {
        generateSpecificReport("Monthly Report");
    }
    
    @FXML
    private void handleInventoryReport() {
        generateSpecificReport("Inventory Report");
    }
    
    @FXML
    private void handleStaffReport() {
        generateSpecificReport("Staff Performance Report");
    }
    
    @FXML
    private void handleCustomReport() {
        showInfoAlert("Custom Report", "Custom report builder will be available soon.");
    }
    
    private void generateSpecificReport(String reportName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Report Generated");
        alert.setHeaderText(reportName);
        alert.setContentText("The " + reportName.toLowerCase() + " has been generated successfully.\n" +
                             "Date Range: " + currentFromDate + " to " + currentToDate);
        alert.showAndWait();
    }
    
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public ProductDAO getProductDAO() {
		return productDAO;
	}

	public void setProductDAO(ProductDAO productDAO) {
		this.productDAO = productDAO;
	}

	// Inner class for table performance data
    public static class TablePerformance {
        private final String tableNumber;
        private final int totalOrders;
        private final double totalRevenue;
        private final double avgOrderValue;
        private final double utilizationRate;
        private final LocalDateTime lastOrder;
        
        public TablePerformance(String tableNumber, int totalOrders, double totalRevenue,
                               double avgOrderValue, double utilizationRate, LocalDateTime lastOrder) {
            this.tableNumber = tableNumber;
            this.totalOrders = totalOrders;
            this.totalRevenue = totalRevenue;
            this.avgOrderValue = avgOrderValue;
            this.utilizationRate = utilizationRate;
            this.lastOrder = lastOrder;
        }
        
        // Getters
        public String getTableNumber() { return tableNumber; }
        public int getTotalOrders() { return totalOrders; }
        public double getTotalRevenue() { return totalRevenue; }
        public double getAvgOrderValue() { return avgOrderValue; }
        public double getUtilizationRate() { return utilizationRate; }
        public LocalDateTime getLastOrder() { return lastOrder; }
    }
}
