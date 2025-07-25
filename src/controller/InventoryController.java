package controller;

import dao.IngredientDAO;
import dao.RecipeDAO;
import dao.StockDAO;
import model.Ingredient;
import model.Recipe;
import model.Stock;
import model.Report;
import service.StockAvailabilityService;
import service.ReportService;
import service.ProductStockService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class InventoryController implements Initializable {
    
    // Tab Controls
    @FXML private TabPane mainTabPane;
    @FXML private Tab ingredientsTab;
    @FXML private Tab stocksTab;
    @FXML private Tab recipesTab;
    @FXML private Tab reportsTab;
    
    // Ingredients Controls
    @FXML private TextField ingredientNameField;
    @FXML private TextField ingredientQuantityField;
    @FXML private TextField ingredientUnitField;
    @FXML private Button addIngredientBtn;
    @FXML private Button updateIngredientBtn;
    @FXML private ListView<Ingredient> ingredientsList;
    
    // Stock Controls
    @FXML private TextField stockNameField;
    @FXML private TextField stockDescriptionField;
    @FXML private ListView<Stock> stockList;
    
    // Recipe Controls
    @FXML private ComboBox<Stock> recipeStockCombo;
    @FXML private ComboBox<Ingredient> recipeIngredientCombo;
    @FXML private TextField recipeQuantityField;
    @FXML private ListView<String> selectedIngredientsList;
    @FXML private Button addMoreIngredientBtn;
    @FXML private Button addToRecipeBtn;
    @FXML private ListView<Stock> recipeList;
    
    // Report Controls
    @FXML private TableView<Report> reportTable;
    @FXML private TableColumn<Report, String> dateColumn;
    @FXML private TableColumn<Report, String> typeColumn;
    @FXML private TableColumn<Report, String> nameColumn;
    @FXML private TableColumn<Report, String> actionColumn;
    @FXML private TableColumn<Report, String> oldValueColumn;
    @FXML private TableColumn<Report, String> newValueColumn;
    @FXML private TableColumn<Report, String> descriptionColumn;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> filterTypeCombo;
    @FXML private Button filterButton;
    @FXML private Button clearFilterButton;
    @FXML private Button refreshReportButton;
    @FXML private Button clearOldReportsButton;
    
    // Report Date Filter Controls
    @FXML private Button todayBtn;
    @FXML private Button yesterdayBtn;
    @FXML private Button thisWeekBtn;
    @FXML private Button allBtn;
    @FXML private Label reportFilterLabel;
    
    private Recipe currentEditingRecipe = null;
    
    // DAOs and Services
    private IngredientDAO ingredientDAO;
    private StockDAO stockDAO;
    private RecipeDAO recipeDAO;
    private StockAvailabilityService stockService;
    private ReportService reportService;
    private ProductStockService productStockService; // Added for product status updates
    
    // Observable Lists
    private ObservableList<Ingredient> ingredientsObservableList;
    private ObservableList<Stock> stockObservableList;
    private ObservableList<Recipe> recipeObservableList;
    private ObservableList<String> selectedIngredientsObservableList;
    private ObservableList<Stock> recipeStockObservableList;
    private ObservableList<Report> reportObservableList;
    private ObservableList<Report> allReportsObservableList; // Store all reports
    
    // Current editing ingredient
    private Ingredient currentEditingIngredient = null;
    
    // Selected ingredients for recipe
    private List<RecipeIngredient> selectedRecipeIngredients = new ArrayList<>();
    
    // Current report filter state
    private String currentReportFilter = "ALL";
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize DAOs and Services
        ingredientDAO = new IngredientDAO();
        stockDAO = new StockDAO();
        recipeDAO = new RecipeDAO();
        stockService = new StockAvailabilityService();
        reportService = new ReportService();
        productStockService = new ProductStockService(); // Initialize product stock service
        
        // Initialize Observable Lists
        ingredientsObservableList = FXCollections.observableArrayList();
        stockObservableList = FXCollections.observableArrayList();
        recipeObservableList = FXCollections.observableArrayList();
        selectedIngredientsObservableList = FXCollections.observableArrayList();
        recipeStockObservableList = FXCollections.observableArrayList();
        reportObservableList = FXCollections.observableArrayList();
        allReportsObservableList = FXCollections.observableArrayList();
        
        // Set up ListViews
        ingredientsList.setItems(ingredientsObservableList);
        stockList.setItems(stockObservableList);
        recipeList.setItems(recipeStockObservableList);
        selectedIngredientsList.setItems(selectedIngredientsObservableList);
        
        // Set up ComboBoxes
        recipeStockCombo.setItems(stockObservableList);
        recipeIngredientCombo.setItems(ingredientsObservableList);
        
        // Set up Report Table
        setupReportTable();
        
        // Load initial data
        loadAllData();
        
        // Set up update button initially disabled
        updateIngredientBtn.setDisable(true);
        
        // Set up report filter buttons
        setupReportFilterButtons();
        
        // Disable ingredient selection until stock is selected
        recipeIngredientCombo.setDisable(true);
        recipeQuantityField.setDisable(true);
        addMoreIngredientBtn.setDisable(true);
        addToRecipeBtn.setDisable(true);
        
        // Enable ingredient selection when stock is selected
        recipeStockCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            recipeIngredientCombo.setDisable(!hasSelection);
            recipeQuantityField.setDisable(!hasSelection);
            addMoreIngredientBtn.setDisable(!hasSelection);
            
            if (!hasSelection) {
                clearSelectedIngredients();
            }
            updateAddToRecipeButton();
        });
        
        // Tab selection listener to load reports when reports tab is selected
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == reportsTab) {
                loadAllReports();
            }
        });
    }
    
    // Method to update product statuses after inventory changes
    private void updateProductStatuses() {
        try {
            productStockService.updateAllProductStatuses();
            System.out.println("Product statuses updated after inventory change");
        } catch (SQLException e) {
            System.err.println("Failed to update product statuses: " + e.getMessage());
        }
    }
    
    private void setupReportFilterButtons() {
        // Set initial filter label
        reportFilterLabel.setText("Showing: All Reports");
        
        // Style the filter buttons
        String activeStyle = "-fx-background-color: #3498db; -fx-text-fill: white;";
        String inactiveStyle = "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50;";
        
        allBtn.setStyle(activeStyle);
        todayBtn.setStyle(inactiveStyle);
        yesterdayBtn.setStyle(inactiveStyle);
        thisWeekBtn.setStyle(inactiveStyle);
    }
    
    // Report Date Filter Methods
    @FXML
    private void showTodayReports() {
        filterReportsByDate("TODAY");
        updateReportFilterButtonStyles("TODAY");
    }
    
    @FXML
    private void showYesterdayReports() {
        filterReportsByDate("YESTERDAY");
        updateReportFilterButtonStyles("YESTERDAY");
    }
    
    @FXML
    private void showThisWeekReports() {
        filterReportsByDate("THIS_WEEK");
        updateReportFilterButtonStyles("THIS_WEEK");
    }
    
    @FXML
    private void showAllReports() {
        filterReportsByDate("ALL");
        updateReportFilterButtonStyles("ALL");
    }
    
    private void filterReportsByDate(String filterType) {
        currentReportFilter = filterType;
        
        List<Report> filteredReports = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        switch (filterType) {
            case "TODAY":
                filteredReports = allReportsObservableList.stream()
                    .filter(report -> {
                        if (report.getActionDate() != null) {
                            return report.getActionDate().toLocalDate().equals(now.toLocalDate());
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
                reportFilterLabel.setText("Showing: Today's Reports (" + filteredReports.size() + ")");
                break;
                
            case "YESTERDAY":
                LocalDate yesterday = now.toLocalDate().minusDays(1);
                filteredReports = allReportsObservableList.stream()
                    .filter(report -> {
                        if (report.getActionDate() != null) {
                            return report.getActionDate().toLocalDate().equals(yesterday);
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
                reportFilterLabel.setText("Showing: Yesterday's Reports (" + filteredReports.size() + ")");
                break;
                
            case "THIS_WEEK":
                LocalDate startOfWeek = now.toLocalDate().minusDays(now.getDayOfWeek().getValue() - 1);
                filteredReports = allReportsObservableList.stream()
                    .filter(report -> {
                        if (report.getActionDate() != null) {
                            LocalDate reportDate = report.getActionDate().toLocalDate();
                            return !reportDate.isBefore(startOfWeek) && !reportDate.isAfter(now.toLocalDate());
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
                reportFilterLabel.setText("Showing: This Week's Reports (" + filteredReports.size() + ")");
                break;
                
            case "ALL":
            default:
                filteredReports = new ArrayList<>(allReportsObservableList);
                reportFilterLabel.setText("Showing: All Reports (" + filteredReports.size() + ")");
                break;
        }
        
        reportObservableList.clear();
        reportObservableList.addAll(filteredReports);
    }
    
    private void updateReportFilterButtonStyles(String activeFilter) {
        String activeStyle = "-fx-background-color: #3498db; -fx-text-fill: white;";
        String inactiveStyle = "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50;";
        
        // Reset all buttons to inactive style
        allBtn.setStyle(inactiveStyle);
        todayBtn.setStyle(inactiveStyle);
        yesterdayBtn.setStyle(inactiveStyle);
        thisWeekBtn.setStyle(inactiveStyle);
        
        // Set active button style
        switch (activeFilter) {
            case "TODAY":
                todayBtn.setStyle(activeStyle);
                break;
            case "YESTERDAY":
                yesterdayBtn.setStyle(activeStyle);
                break;
            case "THIS_WEEK":
                thisWeekBtn.setStyle(activeStyle);
                break;
            case "ALL":
            default:
                allBtn.setStyle(activeStyle);
                break;
        }
    }
    
    private void setupReportTable() {
        // Set up table columns
        dateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
               cellData.getValue().getActionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            )
        );
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("itemType"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        oldValueColumn.setCellValueFactory(new PropertyValueFactory<>("oldValue"));
        newValueColumn.setCellValueFactory(new PropertyValueFactory<>("newValue"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        reportTable.setItems(reportObservableList);
        
        // Set up filter combo
        filterTypeCombo.getItems().addAll("ALL", "INGREDIENT", "STOCK");
        filterTypeCombo.setValue("ALL");
        
        // Set default date range (last 30 days)
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(30));
    }
    
    private void updateAddToRecipeButton() {
        Stock selectedStock = recipeStockCombo.getSelectionModel().getSelectedItem();
        boolean hasStock = selectedStock != null;
        boolean hasIngredients = !selectedRecipeIngredients.isEmpty();
        addToRecipeBtn.setDisable(!(hasStock && hasIngredients));
    }
    
    private void loadAllData() {
        loadIngredients();
        loadStock();
        loadRecipeStocks();
    }
    
    private void loadIngredients() {
        try {
            List<Ingredient> ingredients = ingredientDAO.getAllIngredients();
            ingredientsObservableList.clear();
            ingredientsObservableList.addAll(ingredients);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load ingredients: " + e.getMessage());
        }
    }
    
    private void loadStock() {
        try {
            List<Stock> stocks = stockDAO.getAllStock();
            stockObservableList.clear();
            stockObservableList.addAll(stocks);
            stockService.updateAllStockAvailability();
            // Reload after updating availability
            stocks = stockDAO.getAllStock();
            stockObservableList.clear();
            stockObservableList.addAll(stocks);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load stock: " + e.getMessage());
        }
    }
    
    private void loadRecipeStocks() {
        try {
            // Get stocks that have recipes
            List<Stock> stocksWithRecipes = new ArrayList<>();
            List<Stock> allStocks = stockDAO.getAllStock();
            
            for (Stock stock : allStocks) {
                List<Recipe> recipes = recipeDAO.getRecipesByStockId(stock.getId());
                if (!recipes.isEmpty()) {
                    stocksWithRecipes.add(stock);
                }
            }
            
            recipeStockObservableList.clear();
            recipeStockObservableList.addAll(stocksWithRecipes);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load recipe stocks: " + e.getMessage());
        }
    }
    
    // Report Methods
    @FXML
    private void loadAllReports() {
        try {
            List<Report> reports = reportService.getAllReports();
            allReportsObservableList.clear();
            allReportsObservableList.addAll(reports);
            
            // Apply current filter
            filterReportsByDate(currentReportFilter);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load reports: " + e.getMessage());
        }
    }
    
    @FXML
    private void filterReports() {
        try {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            String filterType = filterTypeCombo.getValue();
            
            List<Report> reports;
            
            if (startDate != null && endDate != null) {
                LocalDateTime startDateTime = startDate.atStartOfDay();
                LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
                reports = reportService.getReportsByDate(startDateTime, endDateTime);
            } else {
                reports = reportService.getAllReports();
            }
            
            // Filter by type if not "ALL"
            if (!"ALL".equals(filterType)) {
                reports = reports.stream()
                    .filter(report -> filterType.equals(report.getItemType()))
                    .toList();
            }
            
            allReportsObservableList.clear();
            allReportsObservableList.addAll(reports);
            
            // Apply current date filter
            filterReportsByDate(currentReportFilter);
            
        } catch (SQLException e) {
            showAlert("Error", "Failed to filter reports: " + e.getMessage());
        }
    }
    
    @FXML
    private void clearReportFilter() {
        startDatePicker.setValue(LocalDate.now().minusDays(30));
        endDatePicker.setValue(LocalDate.now());
        filterTypeCombo.setValue("ALL");
        currentReportFilter = "ALL";
        updateReportFilterButtonStyles("ALL");
        loadAllReports();
    }
    
    @FXML
    private void clearOldReports() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Clear");
        confirmAlert.setHeaderText("Clear Old Reports");
        confirmAlert.setContentText("This will delete reports older than 90 days. Continue?");
        
        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            try {
                reportService.clearOldReports(90);
                loadAllReports();
                showAlert("Success", "Old reports cleared successfully");
            } catch (SQLException e) {
                showAlert("Error", "Failed to clear old reports: " + e.getMessage());
            }
        }
    }
    
    // Ingredient Methods
    @FXML
    private void addIngredient() {
        String name = ingredientNameField.getText().trim();
        String quantityText = ingredientQuantityField.getText().trim();
        String unit = ingredientUnitField.getText().trim();
        
        if (name.isEmpty() || quantityText.isEmpty() || unit.isEmpty()) {
            showAlert("Error", "Please fill all fields");
            return;
        }
        
        try {
            double quantity = Double.parseDouble(quantityText);
            Ingredient ingredient = new Ingredient(name, quantity, unit);
            ingredientDAO.addIngredient(ingredient);
            loadIngredients();
            clearIngredientForm();
            refreshStock(); // Update stock availability
            updateProductStatuses(); // Update product statuses after ingredient change
            showAlert("Success", "Ingredient added successfully");
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid quantity");
        } catch (SQLException e) {
            showAlert("Error", "Failed to add ingredient: " + e.getMessage());
        }
    }
    
    @FXML
    private void updateIngredient() {
        if (currentEditingIngredient == null) {
            showAlert("Error", "No ingredient selected for update");
            return;
        }
        
        String name = ingredientNameField.getText().trim();
        String quantityText = ingredientQuantityField.getText().trim();
        String unit = ingredientUnitField.getText().trim();
        
        if (name.isEmpty() || quantityText.isEmpty() || unit.isEmpty()) {
            showAlert("Error", "Please fill all fields");
            return;
        }
        
        try {
            double quantity = Double.parseDouble(quantityText);
            currentEditingIngredient.setName(name);
            currentEditingIngredient.setQuantity(quantity);
            currentEditingIngredient.setUnit(unit);
            
            ingredientDAO.updateIngredient(currentEditingIngredient);
            loadIngredients();
            clearIngredientForm();
            refreshStock(); // Update stock availability
            updateProductStatuses(); // Update product statuses after ingredient change
            showAlert("Success", "Ingredient updated successfully");
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid quantity");
        } catch (SQLException e) {
            showAlert("Error", "Failed to update ingredient: " + e.getMessage());
        }
    }
    
    @FXML
    private void editSelectedIngredient() {
        Ingredient selected = ingredientsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select an ingredient to edit");
            return;
        }
        
        currentEditingIngredient = selected;
        ingredientNameField.setText(selected.getName());
        ingredientQuantityField.setText(String.valueOf(selected.getQuantity()));
        ingredientUnitField.setText(selected.getUnit());
        
        addIngredientBtn.setDisable(true);
        updateIngredientBtn.setDisable(false);
    }
    
    @FXML
    private void deleteSelectedIngredient() {
        Ingredient selected = ingredientsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select an ingredient to delete");
            return;
        }
        
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Ingredient");
        confirmAlert.setContentText("Are you sure you want to delete " + selected.getName() + "?");
        
        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            try {
                ingredientDAO.deleteIngredient(selected.getId());
                loadIngredients();
                refreshStock(); // Update stock availability
                updateProductStatuses(); // Update product statuses after ingredient deletion
                showAlert("Success", "Ingredient deleted successfully");
            } catch (SQLException e) {
                showAlert("Error", "Failed to delete ingredient: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void clearIngredientForm() {
        ingredientNameField.clear();
        ingredientQuantityField.clear();
        ingredientUnitField.clear();
        currentEditingIngredient = null;
        addIngredientBtn.setDisable(false);
        updateIngredientBtn.setDisable(true);
    }
    
    // Stock Methods
    @FXML
    private void addStock() {
        String productName = stockNameField.getText().trim();
        String description = stockDescriptionField != null ? stockDescriptionField.getText().trim() : "";
        
        if (productName.isEmpty()) {
            showAlert("Error", "Please enter product name");
            return;
        }
        
        try {
            Stock stock = new Stock(productName, description.isEmpty() ? productName : description);
            stockDAO.addStock(stock);
            loadStock();
            stockNameField.clear();
            if (stockDescriptionField != null) {
                stockDescriptionField.clear();
            }
            updateProductStatuses(); // Update product statuses after stock addition
            showAlert("Success", "Product added successfully");
        } catch (SQLException e) {
            showAlert("Error", "Failed to add product: " + e.getMessage());
        }
    }
    
    @FXML
    private void deleteSelectedStock() {
        Stock selected = stockList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a product to delete");
            return;
        }
        
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Product");
        confirmAlert.setContentText("Are you sure you want to delete " + selected.getProductName() + "?");
        
        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            try {
                recipeDAO.deleteRecipesByStockId(selected.getId());
                stockDAO.deleteStock(selected.getId());
                loadStock();
                loadRecipeStocks();
                updateProductStatuses(); // Update product statuses after stock deletion
                showAlert("Success", "Product deleted successfully");
            } catch (SQLException e) {
                showAlert("Error", "Failed to delete product: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void refreshStock() {
        try {
            stockService.updateAllStockAvailability();
            loadStock();
            updateProductStatuses(); // Update product statuses after stock refresh
        } catch (SQLException e) {
            showAlert("Error", "Failed to refresh stock: " + e.getMessage());
        }
    }
    
    @FXML
    private void refreshIngredient() {
    	loadIngredients();
    }
    
    // Recipe Methods
    @FXML
    private void addMoreIngredient() {
        Stock selectedStock = recipeStockCombo.getSelectionModel().getSelectedItem();
        Ingredient selectedIngredient = recipeIngredientCombo.getSelectionModel().getSelectedItem();
        String quantityText = recipeQuantityField.getText().trim();
        
        if (selectedStock == null) {
            showAlert("Error", "Please select a stock first");
            return;
        }
        
        if (selectedIngredient == null || quantityText.isEmpty()) {
            showAlert("Error", "Please select ingredient and enter quantity");
            return;
        }
        
        try {
            double quantity = Double.parseDouble(quantityText);
            
            // Check if ingredient already exists in the list
            boolean exists = selectedRecipeIngredients.stream()
                    .anyMatch(ri -> ri.getIngredient().getId() == selectedIngredient.getId());
            
            if (exists) {
                showAlert("Error", "This ingredient is already added to the recipe");
                return;
            }
            
            RecipeIngredient recipeIngredient = new RecipeIngredient(selectedIngredient, quantity);
            selectedRecipeIngredients.add(recipeIngredient);
            
            String displayText = selectedIngredient.getName() + " - " + quantity + " " + selectedIngredient.getUnit();
            selectedIngredientsObservableList.add(displayText);
            
            // Clear fields
            recipeIngredientCombo.getSelectionModel().clearSelection();
            recipeQuantityField.clear();
            
            updateAddToRecipeButton();
            
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid quantity");
        }
    }
    
    @FXML
    private void removeSelectedIngredient() {
        int selectedIndex = selectedIngredientsList.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            selectedRecipeIngredients.remove(selectedIndex);
            selectedIngredientsObservableList.remove(selectedIndex);
            updateAddToRecipeButton();
        }
    }
    
    @FXML
    private void clearSelectedIngredients() {
        selectedRecipeIngredients.clear();
        selectedIngredientsObservableList.clear();
        recipeIngredientCombo.getSelectionModel().clearSelection();
        recipeQuantityField.clear();
        updateAddToRecipeButton();
    }
    
    @FXML
    private void addToRecipe() {
        Stock selectedStock = recipeStockCombo.getSelectionModel().getSelectedItem();
        
        if (selectedStock == null) {
            showAlert("Error", "Please select a stock");
            return;
        }
        
        if (selectedRecipeIngredients.isEmpty()) {
            showAlert("Error", "Please add at least one ingredient to the recipe");
            return;
        }
        
        try {
            for (RecipeIngredient recipeIngredient : selectedRecipeIngredients) {
                Recipe recipe = new Recipe(selectedStock.getId(),
                                          recipeIngredient.getIngredient().getId(),
                                          recipeIngredient.getQuantity());
                recipeDAO.addRecipe(recipe);
            }
            
            // Clear the selected ingredients
            clearSelectedIngredients();
            recipeStockCombo.getSelectionModel().clearSelection();
            
            // Reload data
            loadRecipeStocks();
            refreshStock();
            updateProductStatuses(); // Update product statuses after recipe addition
            
            showAlert("Success", "Recipe added successfully to " + selectedStock.getProductName());
            
        } catch (SQLException e) {
            showAlert("Error", "Failed to save recipe: " + e.getMessage());
        }
    }
    
    @FXML
    private void viewSelectedStock() {
        Stock selectedStock = recipeList.getSelectionModel().getSelectedItem();
        if (selectedStock == null) {
            showAlert("Error", "Please select a stock to view");
            return;
        }
        
        openRecipeViewDialog(selectedStock, false);
    }
    
    @FXML
    private void editSelectedStock() {
        Stock selectedStock = recipeList.getSelectionModel().getSelectedItem();
        if (selectedStock == null) {
            showAlert("Error", "Please select a stock to edit");
            return;
        }
        
        openRecipeViewDialog(selectedStock, true);
    }
    
    @FXML
    private void deleteSelectedRecipeStock() {
        Stock selectedStock = recipeList.getSelectionModel().getSelectedItem();
        if (selectedStock == null) {
            showAlert("Error", "Please select a stock to delete recipes");
            return;
        }
        
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete All Recipes");
        confirmAlert.setContentText("Are you sure you want to delete all recipes for " + selectedStock.getProductName() + "?");
        
        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            try {
                recipeDAO.deleteRecipesByStockId(selectedStock.getId());
                loadRecipeStocks();
                refreshStock();
                updateProductStatuses(); // Update product statuses after recipe deletion
                showAlert("Success", "All recipes deleted successfully");
            } catch (SQLException e) {
                showAlert("Error", "Failed to delete recipes: " + e.getMessage());
            }
        }
    }
    
    private void openRecipeViewDialog(Stock stock, boolean editMode) {
        try {
            Stage dialogStage = new Stage();
            dialogStage.setTitle((editMode ? "Edit" : "View") + " Recipes for " + stock.getProductName());
            
            VBox root = new VBox(10);
            root.setPadding(new Insets(20));
            
            Label titleLabel = new Label("Recipes for: " + stock.getProductName());
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
            
            ListView<Recipe> recipeListView = new ListView<>();
            recipeListView.setPrefHeight(300);
            recipeListView.setPrefWidth(500);
            
            // Load recipes for this stock
            try {
                List<Recipe> recipes = recipeDAO.getRecipesByStockId(stock.getId());
                ObservableList<Recipe> recipeObsList = FXCollections.observableArrayList(recipes);
                recipeListView.setItems(recipeObsList);
            } catch (SQLException e) {
                showAlert("Error", "Failed to load recipes: " + e.getMessage());
            }
            
            VBox editBox = new VBox(5);
            editBox.setStyle("-fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-padding: 10;");
            
            Label editLabel = new Label("Recipe Details:");
            ComboBox<Ingredient> ingredientCombo = new ComboBox<>();
            ingredientCombo.setItems(ingredientsObservableList);
            ingredientCombo.setPromptText("Select Ingredient");
            
            TextField quantityField = new TextField();
            quantityField.setPromptText("Required Quantity");
            
            HBox editButtonBox = new HBox(10);
            Button addButton = new Button("Add");
            addButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
            
            Button updateButton = new Button("Update");
            updateButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
            updateButton.setDisable(true);
            
            Button deleteButton = new Button("Delete");
            deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            deleteButton.setDisable(true);
            
            editButtonBox.getChildren().addAll(addButton, updateButton, deleteButton);
            editBox.getChildren().addAll(editLabel, ingredientCombo, quantityField, editButtonBox);
            
            Button closeButton = new Button("Close");
            
            root.getChildren().addAll(titleLabel, recipeListView, editBox, closeButton);
            
            // Show/hide edit controls based on mode
            editBox.setVisible(editMode);
            
            final Recipe[] currentEditingRecipe = {null};
            
            // Selection listener
            recipeListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null && editMode) {
                    currentEditingRecipe[0] = newSelection;
                    
                    // Find and select the ingredient
                    for (Ingredient ingredient : ingredientsObservableList) {
                        if (ingredient.getId() == newSelection.getIngredientId()) {
                            ingredientCombo.getSelectionModel().select(ingredient);
                            break;
                        }
                    }
                    quantityField.setText(String.valueOf(newSelection.getRequiredQuantity()));
                }
                updateButton.setDisable(newSelection == null || !editMode);
                deleteButton.setDisable(newSelection == null || !editMode);
            });
            
            // Button actions
            addButton.setOnAction(e -> {
                Ingredient selectedIngredient = ingredientCombo.getSelectionModel().getSelectedItem();
                String quantityText = quantityField.getText().trim();
                
                if (selectedIngredient == null || quantityText.isEmpty()) {
                    showAlert("Error", "Please select ingredient and enter quantity");
                    return;
                }
                
                try {
                    double quantity = Double.parseDouble(quantityText);
                    Recipe recipe = new Recipe(stock.getId(), selectedIngredient.getId(), quantity);
                    recipeDAO.addRecipe(recipe);
                    
                    // Refresh the list
                    List<Recipe> recipes = recipeDAO.getRecipesByStockId(stock.getId());
                    ObservableList<Recipe> recipeObsList = FXCollections.observableArrayList(recipes);
                    recipeListView.setItems(recipeObsList);
                    
                    ingredientCombo.getSelectionModel().clearSelection();
                    quantityField.clear();
                    updateProductStatuses(); // Update product statuses after recipe change
                    showAlert("Success", "Recipe added successfully");
                    
                } catch (NumberFormatException ex) {
                    showAlert("Error", "Please enter a valid quantity");
                } catch (SQLException ex) {
                    showAlert("Error", "Failed to add recipe: " + ex.getMessage());
                }
            });
            
            updateButton.setOnAction(e -> {
                if (currentEditingRecipe[0] == null) {
                    showAlert("Error", "No recipe selected for update");
                    return;
                }
                
                Ingredient selectedIngredient = ingredientCombo.getSelectionModel().getSelectedItem();
                String quantityText = quantityField.getText().trim();
                
                if (selectedIngredient == null || quantityText.isEmpty()) {
                    showAlert("Error", "Please select ingredient and enter quantity");
                    return;
                }
                
                try {
                    double quantity = Double.parseDouble(quantityText);
                    currentEditingRecipe[0].setIngredientId(selectedIngredient.getId());
                    currentEditingRecipe[0].setRequiredQuantity(quantity);
                    
                    recipeDAO.updateRecipe(currentEditingRecipe[0]);
                    
                    // Refresh the list
                    List<Recipe> recipes = recipeDAO.getRecipesByStockId(stock.getId());
                    ObservableList<Recipe> recipeObsList = FXCollections.observableArrayList(recipes);
                    recipeListView.setItems(recipeObsList);
                    
                    ingredientCombo.getSelectionModel().clearSelection();
                    quantityField.clear();
                    currentEditingRecipe[0] = null;
                    updateButton.setDisable(true);
                    deleteButton.setDisable(true);
                    updateProductStatuses(); // Update product statuses after recipe change
                    showAlert("Success", "Recipe updated successfully");
                    
                } catch (NumberFormatException ex) {
                    showAlert("Error", "Please enter a valid quantity");
                } catch (SQLException ex) {
                    showAlert("Error", "Failed to update recipe: " + ex.getMessage());
                }
            });
            
            deleteButton.setOnAction(e -> {
                Recipe selectedRecipe = recipeListView.getSelectionModel().getSelectedItem();
                if (selectedRecipe == null) {
                    showAlert("Error", "Please select a recipe to delete");
                    return;
                }
                
                Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirm Delete");
                confirmAlert.setHeaderText("Delete Recipe");
                confirmAlert.setContentText("Are you sure you want to delete this recipe?");
                
                if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                    try {
                        recipeDAO.deleteRecipe(selectedRecipe.getId());
                        
                        // Refresh the list
                        List<Recipe> recipes = recipeDAO.getRecipesByStockId(stock.getId());
                        ObservableList<Recipe> recipeObsList = FXCollections.observableArrayList(recipes);
                        recipeListView.setItems(recipeObsList);
                        
                        ingredientCombo.getSelectionModel().clearSelection();
                        quantityField.clear();
                        currentEditingRecipe[0] = null;
                        updateButton.setDisable(true);
                        deleteButton.setDisable(true);
                        updateProductStatuses(); // Update product statuses after recipe deletion
                        showAlert("Success", "Recipe deleted successfully");
                    } catch (SQLException ex) {
                        showAlert("Error", "Failed to delete recipe: " + ex.getMessage());
                    }
                }
            });
            
            closeButton.setOnAction(e -> {
                dialogStage.close();
                // Refresh main recipe list when dialog closes
                loadRecipeStocks();
                refreshStock();
                updateProductStatuses(); // Update product statuses when dialog closes
            });
            
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            showAlert("Error", "Failed to open recipe dialog: " + e.getMessage());
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.equals("Success") ? AlertType.INFORMATION : AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Inner class for recipe ingredients
    public static class RecipeIngredient {
        private Ingredient ingredient;
        private double quantity;
        
        public RecipeIngredient(Ingredient ingredient, double quantity) {
            this.ingredient = ingredient;
            this.quantity = quantity;
        }
        
        public Ingredient getIngredient() { return ingredient; }
        public void setIngredient(Ingredient ingredient) { this.ingredient = ingredient; }
        public double getQuantity() { return quantity; }
        public void setQuantity(double quantity) { this.quantity = quantity; }
    }
}
