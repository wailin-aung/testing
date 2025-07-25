package controller;

import dao.OrderDAO;
import dao.ProductDAO;
import dao.RestaurantTableDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Order;
import model.OrderGroup;
import model.Product;
import model.RestaurantTable;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class OrderController implements Initializable {
    
    @FXML private ComboBox<RestaurantTable> tableComboBox;
    @FXML private ComboBox<Product> productComboBox;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Button addOrderButton;
    @FXML private Button refreshButton;
    
    @FXML private TableView<OrderGroup> ordersTable;
    @FXML private TableColumn<OrderGroup, String> orderGroupIdColumn;
    @FXML private TableColumn<OrderGroup, String> tableNumberColumn;
    @FXML private TableColumn<OrderGroup, String> productNamesColumn; // This will show comma-separated products
    @FXML private TableColumn<OrderGroup, Double> totalPriceColumn;
    @FXML private TableColumn<OrderGroup, String> statusColumn;
    @FXML private TableColumn<OrderGroup, LocalDateTime> dateTimeColumn;
    @FXML private TableColumn<OrderGroup, String> paymentMethodColumn;
    
    @FXML private Button updateStatusButton;
    @FXML private Button generateInvoiceButton;
    @FXML private Button viewDetailsButton;
    @FXML private Button addProductButton;
    
    private OrderDAO orderDAO;
    private ProductDAO productDAO;
    private ObservableList<Order> ordersData;
    private ObservableList<OrderGroup> orderGroupsData;
    public boolean generateInvoice;
    
    private ObservableList<OrderItem> currentOrderItems;
    @FXML private TableView<OrderItem> currentOrderTable; // You'll need to add this to your FXML
    @FXML private TableColumn<OrderItem, String> currentProductColumn;
    @FXML private TableColumn<OrderItem, Integer> currentQuantityColumn;
    @FXML private TableColumn<OrderItem, Double> currentPriceColumn;
    @FXML private Label totalAmountLabel; // To show total of current order
    
 // an inner class in OrderController for multiple product
    public static class OrderItem {
        private Product product;
        private int quantity;
        private double totalPrice;
        
        public OrderItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
            this.totalPrice = product.getProductPrice() * quantity;
        }
        
        // Getters
        public Product getProduct() { 
            return product; 
        }
        
        public int getQuantity() { 
            return quantity; 
        }
        
        public double getTotalPrice() { 
            return totalPrice; 
        }
        
        public String getProductName() { 
            return product.getProductName(); 
        }
        
        // Setters
        public void setQuantity(int quantity) { 
            this.quantity = quantity; 
            this.totalPrice = product.getProductPrice() * quantity;
        }
        
        public void setProduct(Product product) {
            this.product = product;
            this.totalPrice = product.getProductPrice() * this.quantity;
        }
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        orderDAO = new OrderDAO();
        productDAO = new ProductDAO();
        
        // Initialize ObservableList objects FIRST
        currentOrderItems = FXCollections.observableArrayList();
        orderGroupsData = FXCollections.observableArrayList(); // Add this line
        
        try {
            initializeComponents();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        setupTableColumns();
        setupCurrentOrderTable();
        loadData(); // This calls loadOrders() which uses orderGroupsData
    }
    
    private void initializeComponents() throws SQLException {
        // Initialize quantity spinner
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 1));
        
        // Load sample tables (you would load from database)
        ObservableList<RestaurantTable> tables = null;
		try {
			tables = FXCollections.observableArrayList(RestaurantTableDAO.getOccupiedTables());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        tableComboBox.setItems(tables);
    }
    
    private void setupTableColumns() {
        orderGroupIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderGroupId"));
        tableNumberColumn.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));
        
        // This column will show products with quantities like "Pizza (2), Burger (1), Coke (3)"
        productNamesColumn.setCellValueFactory(new PropertyValueFactory<>("productsWithQuantities"));
        
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateTimeColumn.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        paymentMethodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        
        // Format columns as before...
        totalPriceColumn.setCellFactory(column -> new TableCell<OrderGroup, Double>() {
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
        
        dateTimeColumn.setCellFactory(column -> new TableCell<OrderGroup, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });
        
        ordersTable.setItems(orderGroupsData);
    }
    
    private void loadData() {
        loadProducts();
        loadOrders();
        loadTables();
    }
    
    private void loadTables() {
    	try {
			List<RestaurantTable> tables = RestaurantTableDAO.getOccupiedTables();
			tableComboBox.setItems(FXCollections.observableArrayList(tables));
		} catch (SQLException e) {
			showErrorAlert("Database Error", "Failed to load products: " + e.getMessage());
		}
    	
    }
    
    private void loadProducts() {
        try {
            List<Product> products = productDAO.getAllProducts();
            productComboBox.setItems(FXCollections.observableArrayList(products));
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load products: " + e.getMessage());
        }
    }
    
    private void loadOrders() {
        try {
            List<OrderGroup> orderGroups = orderDAO.getAllOrderGroups();
            orderGroupsData.clear();
            orderGroupsData.addAll(orderGroups);
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load orders: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleAddOrder() {
        RestaurantTable selectedTable = tableComboBox.getValue();
        
        if (selectedTable == null) {
            showWarningAlert("Selection Required", "Please select a table");
            return;
        }
        
        if (currentOrderItems.isEmpty()) {
            showWarningAlert("No Products", "Please add at least one product to the order");
            return;
        }
        
        try {
            // Generate unique order group ID
            String orderGroupId = "ORD_" + System.currentTimeMillis();
            
            // Convert OrderItems to Orders
            List<Order> ordersToAdd = new ArrayList<>();
            
            for (OrderItem item : currentOrderItems) {
                Order newOrder = new Order();
                newOrder.setTableId(selectedTable.getTableId());
                newOrder.setProductId(item.getProduct().getProductId());
                newOrder.setQty(item.getQuantity());
                newOrder.setStatus("PENDING");
                newOrder.setPaymentMethod("PENDING");
                newOrder.setUnitPrice(item.getProduct().getProductPrice());
                ordersToAdd.add(newOrder);
            }
            
            // Add order group
            if (orderDAO.addOrderGroup(ordersToAdd, orderGroupId)) {
                showInfoAlert("Success", "Order with " + currentOrderItems.size() + " items added successfully!");
                loadOrders();
                clearCurrentOrder();
                clearForm();
            } else {
                showErrorAlert("Error", "Failed to add order");
            }
            
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to add order: " + e.getMessage());
        }
    }
    
    
    @FXML
    private void handleUpdateStatus() {
        OrderGroup selectedOrderGroup = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrderGroup == null) {
            showWarningAlert("Selection Required", "Please select an order to update");
            return;
        }
        
        ChoiceDialog<String> dialog = new ChoiceDialog<>("PREPARING", 
            "PENDING", "PREPARING", "READY", "SERVED", "CANCELLED");
        dialog.setTitle("Update Order Status");
        dialog.setHeaderText("Select new status for Order Group #" + selectedOrderGroup.getOrderGroupId());
        dialog.setContentText("Status:");
        
        dialog.showAndWait().ifPresent(status -> {
            try {
                if (orderDAO.updateOrderGroupStatus(selectedOrderGroup.getOrderGroupId(), status)) {
                    showInfoAlert("Success", "Order status updated successfully!");
                    loadOrders();
                } else {
                    showErrorAlert("Error", "Failed to update order status");
                }
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to update order status: " + e.getMessage());
            }
        });
    }

    
 // Replace your handleGenerateInvoice method with this updated version:

    @FXML
    private void handleGenerateInvoice() {
        OrderGroup selectedOrderGroup = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrderGroup == null) {
            showWarningAlert("Selection Required", "Please select an order to generate invoice");
            return;
        }
        
        try {
            // Get detailed order information
            List<Order> orders = orderDAO.getOrdersByGroupId(selectedOrderGroup.getOrderGroupId());
            
            StringBuilder invoiceDetails = new StringBuilder();
            invoiceDetails.append("INVOICE\n");
            invoiceDetails.append("===================\n");
            invoiceDetails.append("Order ID: ").append(selectedOrderGroup.getOrderGroupId()).append("\n");
            invoiceDetails.append("Table: ").append(selectedOrderGroup.getTableNumber()).append("\n");
            invoiceDetails.append("Date/Time: ").append(selectedOrderGroup.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
            invoiceDetails.append("Status: ").append(selectedOrderGroup.getStatus()).append("\n\n");
            
            invoiceDetails.append("ITEMS:\n");
            invoiceDetails.append("-------------------\n");
            
            double subtotal = 0.0;
            for (Order order : orders) {
                double itemTotal = order.getUnitPrice() * order.getQty();
                invoiceDetails.append(String.format("%-20s x%d @ %.2f MMK = %.2f MMK\n",
                     order.getProductName(),
                     order.getQty(),
                     order.getUnitPrice(),
                     itemTotal));
                subtotal += itemTotal;
            }
            
            invoiceDetails.append("-------------------\n");
            invoiceDetails.append(String.format("TOTAL: %.2f MMK\n", subtotal));
            invoiceDetails.append("Payment Method: ").append(selectedOrderGroup.getPaymentMethod()).append("\n");
            
            // Show invoice
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Invoice Generated");
            alert.setHeaderText("Invoice for Order #" + selectedOrderGroup.getOrderGroupId());
            alert.setContentText(invoiceDetails.toString());
            alert.getDialogPane().setPrefWidth(500);
            alert.getDialogPane().setPrefHeight(400);
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Update payment method to "Done" in the database
                if (orderDAO.updateOrderGroupPaymentMethod(selectedOrderGroup.getOrderGroupId(), "Done")) {
                    selectedOrderGroup.setPaymentMethod("Done");
                    generateInvoice = true;
                    
                    // Refresh the orders table to show the updated payment method
                    loadOrders();
                    
                    showInfoAlert("Payment Completed", "Payment updated to 'Done' successfully!");
                } else {
                    showErrorAlert("Update Error", "Failed to update payment method in database");
                }
            }
            
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to generate invoice: " + e.getMessage());
        }
    }


    @FXML
    private void handleViewDetails() {
        OrderGroup selectedOrderGroup = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrderGroup == null) {
            showWarningAlert("Selection Required", "Please select an order to view details");
            return;
        }
        
        try {
            // Get detailed order information
            List<Order> orders = orderDAO.getOrdersByGroupId(selectedOrderGroup.getOrderGroupId());
            
            StringBuilder details = new StringBuilder();
            details.append("ORDER DETAILS\n");
            details.append("===================\n");
            details.append("Order Group ID: ").append(selectedOrderGroup.getOrderGroupId()).append("\n");
            details.append("Table: ").append(selectedOrderGroup.getTableNumber()).append("\n");
            details.append("Date/Time: ").append(selectedOrderGroup.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
            details.append("Status: ").append(selectedOrderGroup.getStatus()).append("\n");
            details.append("Payment Method: ").append(selectedOrderGroup.getPaymentMethod()).append("\n");
            details.append("Total Items: ").append(orders.size()).append("\n\n");
            
            details.append("PRODUCTS:\n");
            details.append("-------------------\n");
            
            double totalAmount = 0.0;
            int itemNumber = 1;
            
            for (Order order : orders) {
                double itemTotal = order.getUnitPrice() * order.getQty();
                details.append(String.format("%d. %s\n", itemNumber++, order.getProductName()));
                details.append(String.format("   Quantity: %d\n", order.getQty()));
                details.append(String.format("   Unit Price: %.2f MMK\n", order.getUnitPrice()));
                details.append(String.format("   Subtotal: %.2f MMK\n\n", itemTotal));
                totalAmount += itemTotal;
            }
            
            details.append("-------------------\n");
            details.append(String.format("GRAND TOTAL: %.2f MMK", totalAmount));
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Order Details");
            alert.setHeaderText("Details for Order #" + selectedOrderGroup.getOrderGroupId());
            alert.setContentText(details.toString());
            
            // Make the alert dialog larger to show full details
            alert.getDialogPane().setPrefWidth(450);
            alert.getDialogPane().setPrefHeight(500);
            
            alert.showAndWait();
            
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load order details: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadData();
    }
    
    private void clearForm() {
        tableComboBox.setValue(null);
        productComboBox.setValue(null);
        quantitySpinner.getValueFactory().setValue(1);
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
    

    // Initialize the current order list in your initialize method
    

    // Add this method to setup the current order table
    private void setupCurrentOrderTable() {
        currentProductColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        currentQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        currentPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        
        // Format price column
        currentPriceColumn.setCellFactory(column -> new TableCell<OrderItem, Double>() {
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
        
        currentOrderTable.setItems(currentOrderItems);
        
        // Update total when items change
        currentOrderItems.addListener((javafx.collections.ListChangeListener<OrderItem>) change -> {
            updateOrderTotal();
        });
    }

    // Implement handleAddProduct
    @FXML
    private void handleAddProduct() {
        RestaurantTable selectedTable = tableComboBox.getValue();
        Product selectedProduct = productComboBox.getValue();
        int quantity = quantitySpinner.getValue();
        
        if (selectedTable == null) {
            showWarningAlert("Selection Required", "Please select a table first");
            return;
        }
        
        if (selectedProduct == null) {
            showWarningAlert("Selection Required", "Please select a product");
            return;
        }
        
        // Check if product already exists in current order
        boolean productExists = false;
        for (OrderItem item : currentOrderItems) {
            if (item.getProduct().getProductId() == selectedProduct.getProductId()) {
                // Update quantity if product already exists
                item.setQuantity(item.getQuantity() + quantity);
                productExists = true;
                break;
            }
        }
        
        // Add new product if it doesn't exist
        if (!productExists) {
            OrderItem newItem = new OrderItem(selectedProduct, quantity);
            currentOrderItems.add(newItem);
        }
        
        // Clear product selection and reset quantity
        productComboBox.setValue(null);
        quantitySpinner.getValueFactory().setValue(1);
        
        // Refresh the table
        currentOrderTable.refresh();
        
        showInfoAlert("Product Added", selectedProduct.getProductName() + " added to current order");
    }

    // Add method to clear current order
    private void clearCurrentOrder() {
        currentOrderItems.clear();
        updateOrderTotal();
    }

    // Add method to update order total
    private void updateOrderTotal() {
        double total = currentOrderItems.stream()
            .mapToDouble(OrderItem::getTotalPrice)
            .sum();
        
        if (totalAmountLabel != null) {
            totalAmountLabel.setText(String.format("Total: %.2f MMK", total));
        }
    }
    
    @FXML
    public void handleClearOrder() {
    	clearCurrentOrder();
    }

    // Add method to remove item from current order (optional)
    @FXML
    private void handleRemoveFromOrder() {
        OrderItem selectedItem = currentOrderTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            currentOrderItems.remove(selectedItem);
            showInfoAlert("Item Removed", selectedItem.getProductName() + " removed from current order");
        } else {
            showWarningAlert("Selection Required", "Please select an item to remove");
        }
    }

    // Clear current order when table selection changes
    private void setupTableSelectionListener() {
        tableComboBox.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (oldValue != newValue && !currentOrderItems.isEmpty()) {
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Clear Current Order");
                    confirmAlert.setHeaderText("Table Selection Changed");
                    confirmAlert.setContentText("Changing table will clear the current order. Continue?");
                    
                    confirmAlert.showAndWait().ifPresent(result -> {
                        if (result == ButtonType.OK) {
                            clearCurrentOrder();
                        } else {
                            // Revert table selection
                            tableComboBox.setValue(oldValue);
                        }
                    });
                }
            }
        );
    }
    
}
