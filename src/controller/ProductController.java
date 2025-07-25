package controller;

import dao.ProductDAO;
import dao.StockDAO;
import dao.CategoryDAO;
import service.ProductStockService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import model.Product;
import model.Stock;
import model.Category;
import service.ProductStockService.ProductAvailabilityInfo;
import service.ProductStockService.IngredientAvailability;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProductController implements Initializable {
    
    // Form controls
    @FXML private TextField productNameField;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> statusComboBox;
    
    // Action buttons
    @FXML private Button addProductButton;
    @FXML private Button updateProductButton;
    @FXML private Button deleteProductButton;
    @FXML private Button clearFormButton;
    @FXML private Button checkAvailabilityButton;
    @FXML private Button updateAllStatusButton;
    
    // Search and filter
    @FXML private TextField searchField;
    @FXML private ComboBox<Category> filterCategoryCombo;
    @FXML private ComboBox<String> filterStatusCombo;
    @FXML private Button refreshButton;
    
    // Table and columns
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, Integer> productIdColumn;
    @FXML private TableColumn<Product, String> productNameColumn;
    @FXML private TableColumn<Product, String> categoryIdColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, String> statusColumn;
    
    private ProductDAO productDAO;
    private CategoryDAO categoryDAO;
    private ProductStockService productStockService;
    private ObservableList<Product> productsData;
    private FilteredList<Product> filteredData;
    private Product selectedProduct;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        productDAO = new ProductDAO();
        categoryDAO = new CategoryDAO();
        productStockService = new ProductStockService();
        productsData = FXCollections.observableArrayList();
        
        initializeComponents();
        setupTableColumns();
        setupEventHandlers();
        loadData();
        
        // Auto-update product statuses on initialization
        updateAllProductStatuses();
    }
    
    private void initializeComponents() {
        // Initialize status combo box
        statusComboBox.setItems(FXCollections.observableArrayList(
            "AVAILABLE", "UNAVAILABLE", "DISCONTINUED"
        ));
        statusComboBox.setValue("AVAILABLE");
        
        // Initialize status filter combo box
        filterStatusCombo.setItems(FXCollections.observableArrayList(
            "All Status", "AVAILABLE", "UNAVAILABLE", "DISCONTINUED"
        ));
        filterStatusCombo.setValue("All Status");
        
        // Setup filtered list for search functionality
        filteredData = new FilteredList<>(productsData, p -> true);
        productsTable.setItems(filteredData);
        
        // Initialize form state
        updateProductButton.setDisable(true);
        deleteProductButton.setDisable(true);
        checkAvailabilityButton.setDisable(true);
    }
    
    private void setupTableColumns() {
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        categoryIdColumn.setCellValueFactory(cellData -> {
            try {
                Category category = categoryDAO.getCategoryById(cellData.getValue().getCategoryId());
                return new javafx.beans.property.SimpleStringProperty(
                    category != null ? category.getCategoryName() : "Unknown"
                );
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty("Error");
            }
        });
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("productPrice"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Format price column
        priceColumn.setCellFactory(column -> new TableCell<Product, Double>() {
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
        
        // Format status column with colors
        statusColumn.setCellFactory(column -> new TableCell<Product, String>() {
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
                        case "UNAVAILABLE":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        case "DISCONTINUED":
                            setStyle("-fx-text-fill: #95a5a6; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
    }
    
    private void setupEventHandlers() {
        // Table selection handler
        productsTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                selectedProduct = newValue;
                if (newValue != null) {
                    populateForm(newValue);
                    updateProductButton.setDisable(false);
                    deleteProductButton.setDisable(false);
                    checkAvailabilityButton.setDisable(false);
                } else {
                    updateProductButton.setDisable(true);
                    deleteProductButton.setDisable(true);
                    checkAvailabilityButton.setDisable(true);
                }
            }
        );
        
        // Search functionality - real-time search as user types
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }
    
    private void applyFilters() {
        filteredData.setPredicate(product -> {
            // Get search text
            String searchText = searchField.getText();
            boolean matchesSearch = true;
            
            if (searchText != null && !searchText.trim().isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase().trim();
                matchesSearch = product.getProductName().toLowerCase().contains(lowerCaseFilter);
            }
            
            // Get selected category filter
            Category selectedCategory = filterCategoryCombo.getValue();
            boolean matchesCategory = true;
            
            if (selectedCategory != null && selectedCategory.getCategoryId() != 0) {
                matchesCategory = product.getCategoryId() == selectedCategory.getCategoryId();
            }
            
            // Get selected status filter
            String selectedStatus = filterStatusCombo.getValue();
            boolean matchesStatus = true;
            
            if (selectedStatus != null && !selectedStatus.equals("All Status")) {
                matchesStatus = product.getStatus().equals(selectedStatus);
            }
            
            return matchesSearch && matchesCategory && matchesStatus;
        });
    }
    
    private void loadData() {
        loadCategories();
        loadProducts();
    }
    
    private void loadCategories() {
        try {
            List<Category> categories = categoryDAO.getAllCategories();
            ObservableList<Category> categoryList = FXCollections.observableArrayList(categories);
            
            categoryComboBox.setItems(categoryList);
            
            // Add "All Categories" option for filter
            ObservableList<Category> filterList = FXCollections.observableArrayList();
            filterList.add(new Category(0, "All Categories"));
            filterList.addAll(categories);
            filterCategoryCombo.setItems(filterList);
            filterCategoryCombo.setValue(filterList.get(0));
            
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load categories: " + e.getMessage());
        }
    }
    
    private void loadProducts() {
        try {
            List<Product> products = productDAO.getAllProducts();
            productsData.clear();
            productsData.addAll(products);
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load products: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleAddProduct() {
        if (!validateForm()) {
            return;
        }
        
        try {
            Product newProduct = createProductFromForm();
            
            if (productDAO.addProduct(newProduct)) {
                showInfoAlert("Success", "Product added successfully!");
                loadProducts();
                clearForm();
                // Update status after adding
                updateAllProductStatuses();
            } else {
                showErrorAlert("Error", "Failed to add product");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to add product: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleUpdateProduct() {
        if (selectedProduct == null) {
            showWarningAlert("Selection Required", "Please select a product to update");
            return;
        }
        
        if (!validateForm()) {
            return;
        }
        
        try {
            Product updatedProduct = createProductFromForm();
            updatedProduct.setProductId(selectedProduct.getProductId());
            
            if (productDAO.updateProduct(updatedProduct)) {
                showInfoAlert("Success", "Product updated successfully!");
                loadProducts();
                clearForm();
                // Update status after updating
                updateAllProductStatuses();
            } else {
                showErrorAlert("Error", "Failed to update product");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to update product: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDeleteProduct() {
        if (selectedProduct == null) {
            showWarningAlert("Selection Required", "Please select a product to delete");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Product");
        confirmAlert.setContentText("Are you sure you want to delete '" + selectedProduct.getProductName() + "'?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (productDAO.deleteProduct(selectedProduct.getProductId())) {
                    showInfoAlert("Success", "Product deleted successfully!");
                    loadProducts();
                    clearForm();
                } else {
                    showErrorAlert("Error", "Failed to delete product");
                }
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to delete product: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleClearForm() {
        clearForm();
    }
    
    @FXML
    private void handleCategoryFilter() {
        applyFilters();
    }
    
    @FXML
    private void handleStatusFilter() {
        applyFilters();
    }
    
    @FXML
    private void handleRefresh() {
        loadData();
        searchField.clear();
        if (filterCategoryCombo.getItems().size() > 0) {
            filterCategoryCombo.setValue(filterCategoryCombo.getItems().get(0));
        }
        filterStatusCombo.setValue("All Status");
        // Update statuses on refresh
        updateAllProductStatuses();
    }
    
    @FXML
    private void handleCheckAvailability() {
        if (selectedProduct == null) {
            showWarningAlert("Selection Required", "Please select a product to check availability");
            return;
        }
        
        try {
            ProductAvailabilityInfo info = productStockService.getProductAvailabilityInfo(selectedProduct.getProductId());
            showAvailabilityDialog(info);
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to check availability: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleUpdateAllStatus() {
        updateAllProductStatuses();
    }
    
    /**
     * Updates all product statuses based on stock availability
     */
    public void updateAllProductStatuses() {
        try {
            productStockService.updateAllProductStatuses();
            loadProducts(); // Refresh the table to show updated statuses
            showInfoAlert("Success", "All product statuses updated based on stock availability!");
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to update product statuses: " + e.getMessage());
        }
    }
    
    
    public void updateProductStatus(int productId) {
        try {
            productStockService.updateProductStatus(productId);
            loadProducts(); // Refresh the table
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to update product status: " + e.getMessage());
        }
    }
    
    private void showAvailabilityDialog(ProductAvailabilityInfo info) {
        if (info == null) {
            showErrorAlert("Error", "Product information not found");
            return;
        }
        
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Product Availability - " + info.getProduct().getProductName());
        
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        
        // Product info
        Label productLabel = new Label("Product: " + info.getProduct().getProductName());
        productLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        Label statusLabel = new Label("Current Status: " + info.getProduct().getStatus());
        statusLabel.setStyle("-fx-font-size: 14px;");
        
        Label availabilityLabel = new Label("Can be made: " + (info.isAvailable() ? "YES" : "NO"));
        availabilityLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; " +
            (info.isAvailable() ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;"));
        
        // Ingredients table
        if (!info.getIngredientAvailabilities().isEmpty()) {
            Label ingredientsLabel = new Label("Required Ingredients:");
            ingredientsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            
            TableView<IngredientAvailability> ingredientsTable = new TableView<>();
            ingredientsTable.setPrefHeight(200);
            
            TableColumn<IngredientAvailability, String> nameCol = new TableColumn<>("Ingredient");
            nameCol.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getIngredient().getName()));
            
            TableColumn<IngredientAvailability, String> requiredCol = new TableColumn<>("Required");
            requiredCol.setCellValueFactory(cellData -> 
                new SimpleStringProperty(
                    cellData.getValue().getRequiredQuantity() + " " + 
                    cellData.getValue().getIngredient().getUnit()));
            
            TableColumn<IngredientAvailability, String> availableCol = new TableColumn<>("Available");
            availableCol.setCellValueFactory(cellData -> 
                new SimpleStringProperty(
                    cellData.getValue().getAvailableQuantity() + " " + 
                    cellData.getValue().getIngredient().getUnit()));
            
            TableColumn<IngredientAvailability, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(cellData -> 
                new SimpleStringProperty(
                    cellData.getValue().isSufficient() ? "✓ Sufficient" : "✗ Insufficient"));
            
            // Style the status column
            statusCol.setCellFactory(column -> new TableCell<IngredientAvailability, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        if (item.contains("✓")) {
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        }
                    }
                }
            });
            
            ingredientsTable.getColumns().addAll(nameCol, requiredCol, availableCol, statusCol);
            
            ObservableList<IngredientAvailability> ingredientData = 
                FXCollections.observableArrayList(info.getIngredientAvailabilities());
            ingredientsTable.setItems(ingredientData);
            
            root.getChildren().addAll(ingredientsLabel, ingredientsTable);
        }
        
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> dialogStage.close());
        
        root.getChildren().addAll(productLabel, statusLabel, availabilityLabel, closeButton);
        
        Scene scene = new Scene(root, 600, 400);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }
    
    private Product createProductFromForm() {
        Product product = new Product();
        product.setProductName(productNameField.getText().trim());
        product.setCategoryId(categoryComboBox.getValue().getCategoryId());
        product.setProductPrice(Double.parseDouble(priceField.getText().trim()));
        product.setStatus(statusComboBox.getValue());
        return product;
    }
    
    private void populateForm(Product product) {
        productNameField.setText(product.getProductName());
        priceField.setText(String.valueOf(product.getProductPrice()));
        statusComboBox.setValue(product.getStatus());
        
        // Set category
        try {
            Category category = categoryDAO.getCategoryById(product.getCategoryId());
            categoryComboBox.setValue(category);
        } catch (SQLException e) {
            showErrorAlert("Error", "Failed to load category information");
        }
    }
    
    private void clearForm() {
        productNameField.clear();
        categoryComboBox.setValue(null);
        priceField.clear();
        statusComboBox.setValue("AVAILABLE");
        
        productsTable.getSelectionModel().clearSelection();
        selectedProduct = null;
        updateProductButton.setDisable(true);
        deleteProductButton.setDisable(true);
        checkAvailabilityButton.setDisable(true);
    }
    
    private boolean validateForm() {
        if (productNameField.getText().trim().isEmpty()) {
            showWarningAlert("Validation Error", "Product name is required");
            return false;
        }
        
        if (categoryComboBox.getValue() == null) {
            showWarningAlert("Validation Error", "Please select a category");
            return false;
        }
        
        try {
            double price = Double.parseDouble(priceField.getText().trim());
            if (price < 0) {
                showWarningAlert("Validation Error", "Price must be a positive number");
                return false;
            }
        } catch (NumberFormatException e) {
            showWarningAlert("Validation Error", "Please enter a valid price");
            return false;
        }
        
        return true;
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
    }
}
