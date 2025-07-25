package controller;

import dao.RestaurantTableDAO;
import dao.OrderDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import model.RestaurantTable;
import model.Order;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class TableController implements Initializable {
    
    // Form controls
    @FXML private TextField tableNumberField;
    @FXML private Spinner<Integer> capacitySpinner;
    @FXML private TextField locationField;
    @FXML private ComboBox<String> statusComboBox;
    
    // Action buttons
    @FXML private Button addTableButton;
    @FXML private Button updateTableButton;
    @FXML private Button deleteTableButton;
    @FXML private Button clearFormButton;
    
    // Quick action buttons
    @FXML private Button markAvailableButton;
    @FXML private Button markOccupiedButton;
    @FXML private Button markReservedButton;
    @FXML private Button markMaintenanceButton;
    @FXML private Button refreshButton;
    
    // Tables grid and list
    @FXML private GridPane tablesGrid;
    @FXML private TableView<RestaurantTable> tablesTable;
    @FXML private TableColumn<RestaurantTable, Integer> tableIdColumn;
    @FXML private TableColumn<RestaurantTable, String> tableNumberColumn;
    @FXML private TableColumn<RestaurantTable, Integer> capacityColumn;
    @FXML private TableColumn<RestaurantTable, String> locationColumn;
    @FXML private TableColumn<RestaurantTable, String> statusColumn;
    @FXML private TableColumn<RestaurantTable, String> currentOrderColumn;
    @FXML private TableColumn<RestaurantTable, LocalDateTime> lastUpdatedColumn;
    
    // Context menu items
    @FXML private MenuItem editTableMenuItem;
    @FXML private MenuItem viewOrdersMenuItem;
    @FXML private MenuItem changeStatusMenuItem;
    @FXML private MenuItem deleteTableMenuItem;
    
    // Statistics labels
    @FXML private Label totalTablesLabel;
    @FXML private Label availableTablesLabel;
    @FXML private Label occupiedTablesLabel;
    @FXML private Label occupancyRateLabel;
    
    // Legend circles
    @FXML private Circle availableCircle;
    @FXML private Circle occupiedCircle;
    @FXML private Circle reservedCircle;
    @FXML private Circle maintenanceCircle;
    
    private RestaurantTableDAO tableDAO;
    private OrderDAO orderDAO;
    private ObservableList<RestaurantTable> tablesData;
    private RestaurantTable selectedTable;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tableDAO = new RestaurantTableDAO();
        orderDAO = new OrderDAO();
        tablesData = FXCollections.observableArrayList();
        
        initializeComponents();
        setupTableColumns();
        setupEventHandlers();
        loadData();
        updateStatistics();
    }
    
    private void initializeComponents() {
        // Initialize capacity spinner
        capacitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 4));
        
        // Initialize status combo box
        statusComboBox.setItems(FXCollections.observableArrayList(
            "AVAILABLE", "OCCUPIED", "RESERVED", "MAINTENANCE"
        ));
        statusComboBox.setValue("AVAILABLE");
        
        // Initialize form state
        updateTableButton.setDisable(true);
        deleteTableButton.setDisable(true);
        
        // Disable quick action buttons initially
        markAvailableButton.setDisable(true);
        markOccupiedButton.setDisable(true);
        markReservedButton.setDisable(true);
        markMaintenanceButton.setDisable(true);
        
        // Set legend colors
        if (availableCircle != null) availableCircle.setFill(Color.web("#27ae60"));
        if (occupiedCircle != null) occupiedCircle.setFill(Color.web("#e74c3c"));
        if (reservedCircle != null) reservedCircle.setFill(Color.web("#f39c12"));
        if (maintenanceCircle != null) maintenanceCircle.setFill(Color.web("#95a5a6"));
    }
    
    private void setupTableColumns() {
        tableIdColumn.setCellValueFactory(new PropertyValueFactory<>("tableId"));
        tableNumberColumn.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Format status column with colors
        statusColumn.setCellFactory(column -> new TableCell<RestaurantTable, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "AVAILABLE":
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            break;
                        case "OCCUPIED":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        case "RESERVED":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            break;
                        case "MAINTENANCE":
                            setStyle("-fx-text-fill: #95a5a6; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
        
        // Current order column
        currentOrderColumn.setCellValueFactory(cellData -> {
            try {
                List<Order> orders = orderDAO.getActiveOrdersByTable(cellData.getValue().getTableId());
                if (!orders.isEmpty()) {
                    return new javafx.beans.property.SimpleStringProperty(
                        orders.size() + " active order(s)"
                    );
                } else {
                    return new javafx.beans.property.SimpleStringProperty("No orders");
                }
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty("Error");
            }
        });
        
        tablesTable.setItems(tablesData);
    }
    
    private void setupEventHandlers() {
        // Table selection handler
        tablesTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                selectedTable = newValue;
                if (newValue != null) {
                    populateForm(newValue);
                    updateTableButton.setDisable(false);
                    deleteTableButton.setDisable(false);
                    
                    // Enable quick action buttons
                    markAvailableButton.setDisable(false);
                    markOccupiedButton.setDisable(false);
                    markReservedButton.setDisable(false);
                    markMaintenanceButton.setDisable(false);
                } else {
                    updateTableButton.setDisable(true);
                    deleteTableButton.setDisable(true);
                    
                    // Disable quick action buttons
                    markAvailableButton.setDisable(true);
                    markOccupiedButton.setDisable(true);
                    markReservedButton.setDisable(true);
                    markMaintenanceButton.setDisable(true);
                }
            }
        );
    }
    
    private void loadData() {
        loadTables();
        updateTablesGrid();
    }
    
    private void loadTables() {
        try {
            List<RestaurantTable> tables = RestaurantTableDAO.getAllTables();
            tablesData.clear();
            tablesData.addAll(tables);
            updateStatistics();
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load tables: " + e.getMessage());
        }
    }
    
    private void updateTablesGrid() {
        if (tablesGrid != null) {
            tablesGrid.getChildren().clear();
            
            int col = 0, row = 0;
            int maxCols = 6; // Maximum tables per row
            
            for (RestaurantTable table : tablesData) {
                VBox tableCard = createTableCard(table);
                tablesGrid.add(tableCard, col, row);
                
                col++;
                if (col >= maxCols) {
                    col = 0;
                    row++;
                }
            }
        }
    }
    
    private VBox createTableCard(RestaurantTable table) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(120, 100);
        card.setStyle(getTableCardStyle(table.getStatus()));
        
        Label tableNumber = new Label(table.getTableNumber());
        tableNumber.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;-fx-text-fill: black;");
        
        Label capacity = new Label("Cap: " + table.getCapacity());
        Label location = new Label(table.getLocation());
        Label status = new Label(table.getStatus());
        location.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;-fx-text-fill: black;");
        capacity.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;-fx-text-fill: black;");
        status.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;-fx-text-fill: black;");
        
        card.getChildren().addAll(tableNumber, capacity, location, status);
        
        // Add click handler
        card.setOnMouseClicked(event -> {
            tablesTable.getSelectionModel().select(table);
        });
        
        return card;
    }
    
    private String getTableCardStyle(String status) {
        String baseStyle = "-fx-background-color: white; -fx-border-color: %s; " +
                          "-fx-border-width: 3px; -fx-border-radius: 8px; " +
                          "-fx-background-radius: 8px; -fx-padding: 10px; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2); " +
                          "-fx-cursor: hand;" ;
        
        switch (status) {
            case "AVAILABLE":
                return String.format(baseStyle, "#27ae60");
            case "OCCUPIED":
                return String.format(baseStyle, "#e74c3c");
            case "RESERVED":
                return String.format(baseStyle, "#f39c12");
            case "MAINTENANCE":
                return String.format(baseStyle, "#95a5a6");
            default:
                return String.format(baseStyle, "#bdc3c7");
        }
    }
    
    @FXML
    private void handleAddTable() {
        if (!validateForm()) {
            return;
        }
        
        try {
            RestaurantTable newTable = createTableFromForm();
            
            if (tableDAO.addTable(newTable)) {
                showInfoAlert("Success", "Table added successfully!");
                loadData();
                clearForm();
            } else {
                showErrorAlert("Error", "Failed to add table");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to add table: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleUpdateTable() {
        if (selectedTable == null) {
            showWarningAlert("Selection Required", "Please select a table to update");
            return;
        }
        
        if (!validateForm()) {
            return;
        }
        
        try {
            RestaurantTable updatedTable = createTableFromForm();
            updatedTable.setTableId(selectedTable.getTableId());
            
            if (tableDAO.updateTable(updatedTable)) {
                showInfoAlert("Success", "Table updated successfully!");
                loadData();
                clearForm();
            } else {
                showErrorAlert("Error", "Failed to update table");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to update table: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDeleteTable() {
        if (selectedTable == null) {
            showWarningAlert("Selection Required", "Please select a table to delete");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Table");
        confirmAlert.setContentText("Are you sure you want to delete table '" + 
                                   selectedTable.getTableNumber() + "'?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (tableDAO.deleteTable(selectedTable.getTableId())) {
                    showInfoAlert("Success", "Table deleted successfully!");
                    loadData();
                    clearForm();
                } else {
                    showErrorAlert("Error", "Failed to delete table");
                }
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to delete table: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleClearForm() {
        clearForm();
    }
    
    @FXML
    private void handleMarkAvailable() {
        updateTableStatus("AVAILABLE");
    }
    
    @FXML
    private void handleMarkOccupied() {
        updateTableStatus("OCCUPIED");
    }
    
    @FXML
    private void handleMarkReserved() {
        updateTableStatus("RESERVED");
    }
    
    @FXML
    private void handleMarkMaintenance() {
        updateTableStatus("MAINTENANCE");
    }
    
    @FXML
    private void handleRefresh() {
        loadData();
    }
    
    @FXML
    private void handleEditTable() {
        // This is handled by the update button
    }
    
    @FXML
    private void handleViewOrders() {
        if (selectedTable == null) {
            showWarningAlert("Selection Required", "Please select a table to view orders");
            return;
        }
        
        try {
            List<Order> orders = orderDAO.getOrdersByTable(selectedTable.getTableId());
            
            StringBuilder orderInfo = new StringBuilder();
            orderInfo.append("Orders for Table ").append(selectedTable.getTableNumber()).append(":\n\n");
            
            if (orders.isEmpty()) {
                orderInfo.append("No orders found for this table.");
            } else {
                for (Order order : orders) {
                    orderInfo.append("Order #").append(order.getOrderId())
                            .append(" - ").append(order.getProductName())
                            .append(" (Qty: ").append(order.getQty()).append(")")
                            .append(" - Status: ").append(order.getStatus())
                            .append("\n");
                }
            }
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Table Orders");
            alert.setHeaderText("Order History");
            alert.setContentText(orderInfo.toString());
            alert.showAndWait();
            
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load orders: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleChangeStatus() {
        if (selectedTable == null) {
            showWarningAlert("Selection Required", "Please select a table to change status");
            return;
        }
        
        ChoiceDialog<String> dialog = new ChoiceDialog<>(selectedTable.getStatus(),
            "AVAILABLE", "OCCUPIED", "RESERVED", "MAINTENANCE");
        dialog.setTitle("Change Table Status");
        dialog.setHeaderText("Change status for " + selectedTable.getTableNumber());
        dialog.setContentText("New Status:");
        
        dialog.showAndWait().ifPresent(this::updateTableStatus);
    }
    
    private void updateTableStatus(String newStatus) {
        if (selectedTable == null) {
            return;
        }
        
        try {
            if (tableDAO.updateTableStatus(selectedTable.getTableId(), newStatus)) {
                showInfoAlert("Success", "Table status updated to " + newStatus);
                loadData();
            } else {
                showErrorAlert("Error", "Failed to update table status");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to update table status: " + e.getMessage());
        }
    }
    
    private RestaurantTable createTableFromForm() {
        RestaurantTable table = new RestaurantTable();
        table.setTableNumber(tableNumberField.getText().trim());
        table.setCapacity(capacitySpinner.getValue());
        table.setLocation(locationField.getText().trim());
        table.setStatus(statusComboBox.getValue());
        return table;
    }
    
    private void populateForm(RestaurantTable table) {
        tableNumberField.setText(table.getTableNumber());
        capacitySpinner.getValueFactory().setValue(table.getCapacity());
        locationField.setText(table.getLocation());
        statusComboBox.setValue(table.getStatus());
    }
    
    private void clearForm() {
        tableNumberField.clear();
        capacitySpinner.getValueFactory().setValue(4);
        locationField.clear();
        statusComboBox.setValue("AVAILABLE");
        
        tablesTable.getSelectionModel().clearSelection();
        selectedTable = null;
        updateTableButton.setDisable(true);
        deleteTableButton.setDisable(true);
        
        markAvailableButton.setDisable(true);
        markOccupiedButton.setDisable(true);
        markReservedButton.setDisable(true);
        markMaintenanceButton.setDisable(true);
    }
    
    private boolean validateForm() {
        if (tableNumberField.getText().trim().isEmpty()) {
            showWarningAlert("Validation Error", "Table number is required");
            return false;
        }
        
        if (locationField.getText().trim().isEmpty()) {
            showWarningAlert("Validation Error", "Location is required");
            return false;
        }
        
        return true;
    }
    
    private void updateStatistics() {
        int total = tablesData.size();
        int available = (int) tablesData.stream()
            .filter(t -> "AVAILABLE".equals(t.getStatus()))
            .count();
        int occupied = (int) tablesData.stream()
            .filter(t -> "OCCUPIED".equals(t.getStatus()))
            .count();
        
        double occupancyRate = total > 0 ? (double) occupied / total * 100 : 0;
        
        totalTablesLabel.setText(String.valueOf(total));
        availableTablesLabel.setText(String.valueOf(available));
        occupiedTablesLabel.setText(String.valueOf(occupied));
        occupancyRateLabel.setText(String.format("%.1f%%", occupancyRate));
    }
    
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
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
}
