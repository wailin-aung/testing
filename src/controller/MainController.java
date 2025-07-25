package controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    
    // FXML Components
    @FXML private Label statusLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Label screenResolutionLabel;
    @FXML private Label memoryUsageLabel;
    @FXML private Label connectionStatus;
    @FXML private Label sidebarTitle;
    
    @FXML private VBox sidebarContainer;
    @FXML private Button sidebarToggle;
    @FXML private ScrollPane tabScrollPane;
    @FXML private VBox tabButtonContainer;
    
    @FXML private ToggleButton tablesBtn;
    @FXML private ToggleButton productsBtn;
    @FXML private ToggleButton ordersBtn;
    @FXML private ToggleButton dashboardBtn;
    @FXML private ToggleButton stockBtn;
    @FXML private ToggleButton inventoryBtn; // Add this new button
    
    @FXML private StackPane contentStackPane;
    @FXML private ScrollPane mainContentScroll;
    @FXML private VBox activeContentContainer;
    @FXML private VBox loadingOverlay;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label loadingLabel;
    
    @FXML private Button minimizeBtn;
    @FXML private Button maximizeBtn;
    @FXML private Button closeBtn;
    
    // Properties
    private final BooleanProperty sidebarCollapsed = new SimpleBooleanProperty(false);
    private final Map<String, Node> contentCache = new HashMap<>();
    private Timeline clockTimeline;
    private Timeline statusTimeline;
    private Timeline memoryTimeline;
    private ToggleGroup navTabs;
    
    // Content Loading - Updated to include inventory
    private final Map<String, String> contentFiles = Map.of(
        "tables", "/fxmldesign/TableManagement.fxml",
        "products", "/fxmldesign/ProductManagement.fxml",
        "orders", "/fxmldesign/OrderManagement.fxml",
        "dashboard", "/fxmldesign/ReportsView.fxml",
        "stock", "/fxmldesign/InventoryView.fxml",
        "inventory", "/fxmldesign/InventoryView.fxml" // Add inventory mapping
    );
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupNavigationToggleGroup();
        setupSidebarToggle();
        initializeTimelines();
        setupContentLoading();
        // Load default content
        Platform.runLater(() -> loadContent("dashboard"));
    }
    
    private void setupNavigationToggleGroup() {
        navTabs = new ToggleGroup();
        tablesBtn.setToggleGroup(navTabs);
        productsBtn.setToggleGroup(navTabs);
        ordersBtn.setToggleGroup(navTabs);
        dashboardBtn.setToggleGroup(navTabs);
        stockBtn.setToggleGroup(navTabs);
        inventoryBtn.setToggleGroup(navTabs); // Add inventory button to group
        
        // Set default selection
        dashboardBtn.setSelected(true);
        
        // Add selection listener
        navTabs.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                ToggleButton selectedBtn = (ToggleButton) newToggle;
                String contentKey = (String) selectedBtn.getUserData();
                loadContent(contentKey);
            }
        });
    }
    
    // Rest of your existing methods remain the same...
    private void setupSidebarToggle() {
        sidebarToggle.setOnAction(e -> toggleSidebar());
        
        // Bind sidebar title visibility to collapsed state
        sidebarTitle.visibleProperty().bind(sidebarCollapsed.not());
        sidebarTitle.managedProperty().bind(sidebarCollapsed.not());
        
        // Update tab button content based on sidebar state
        sidebarCollapsed.addListener((obs, oldVal, newVal) -> {
            updateTabButtonsForCollapsedState(newVal);
            updateSidebarStyles(newVal);
        });
    }
    

    
    private void initializeTimelines() {
        // Clock timeline
        clockTimeline = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> updateDateTime())
        );
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();
        
        // Status timeline
        statusTimeline = new Timeline(
            new KeyFrame(Duration.seconds(30), e -> updateSystemStatus())
        );
        statusTimeline.setCycleCount(Timeline.INDEFINITE);
        statusTimeline.play();
        
        // Memory usage timeline
        memoryTimeline = new Timeline(
            new KeyFrame(Duration.seconds(5), e -> updateMemoryUsage())
        );
        memoryTimeline.setCycleCount(Timeline.INDEFINITE);
        memoryTimeline.play();
        
        // Initial updates
        updateDateTime();
        updateSystemStatus();
        updateMemoryUsage();
    }
    
    private void setupContentLoading() {
        // Pre-load critical content
        Platform.runLater(() -> {
            preloadContent("dashboard");
            preloadContent("tables");
            preloadContent("inventory"); // Pre-load inventory
        });
    }
    
    private void toggleSidebar() {
        boolean newCollapsedState = !sidebarCollapsed.get();
        
        // Create smooth transition
        Timeline transition = new Timeline();
        
        double targetWidth = newCollapsedState ? 60 : 280;
        
        KeyValue widthValue = new KeyValue(
            sidebarContainer.prefWidthProperty(), 
            targetWidth, 
            Interpolator.EASE_BOTH
        );
        
        KeyFrame frame = new KeyFrame(Duration.millis(300), widthValue);
        transition.getKeyFrames().add(frame);
        
        transition.setOnFinished(e -> sidebarCollapsed.set(newCollapsedState));
        transition.play();
        
        // Update toggle button text
        sidebarToggle.setText(newCollapsedState ? "☰" : "←");
    }
    
    private void updateTabButtonsForCollapsedState(boolean collapsed) {
        tabButtonContainer.getChildren().forEach(node -> {
            if (node instanceof ToggleButton) {
                ToggleButton btn = (ToggleButton) node;
                HBox content = (HBox) btn.getGraphic();
                
                if (content != null && content.getChildren().size() > 1) {
                    VBox textContainer = (VBox) content.getChildren().get(1);
                    textContainer.setVisible(!collapsed);
                    textContainer.setManaged(!collapsed);
                }
            }
        });
    }
    
    private void updateSidebarStyles(boolean collapsed) {
        if (collapsed) {
            sidebarContainer.getStyleClass().add("collapsed");
        } else {
            sidebarContainer.getStyleClass().remove("collapsed");
        }
    }
    
    private void loadContent(String contentKey) {
        if (contentKey == null) return;
        
        showLoadingOverlay(true);
        
        // Simulate loading delay for better UX
        Timeline loadingDelay = new Timeline(
            new KeyFrame(Duration.millis(300), e -> {
                Node content = getOrCreateContent(contentKey);
                if (content != null) {
                    displayContent(content);
                }
                showLoadingOverlay(false);
            })
        );
        loadingDelay.play();
    }
    
    private Node getOrCreateContent(String contentKey) {
        // Check cache first
        if (contentCache.containsKey(contentKey)) {
            return contentCache.get(contentKey);
        }
        
        // Load from your FXML files
        String fxmlFile = contentFiles.get(contentKey);
        if (fxmlFile != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Node content = loader.load();
                
                // Store the controller reference if needed
                Object controller = loader.getController();
                if (controller != null) {
                    // You can store controller references for later use
                    System.out.println("Loaded controller: " + controller.getClass().getSimpleName());
                }
                
                contentCache.put(contentKey, content);
                return content;
            } catch (IOException e) {
                System.err.println("Failed to load content: " + fxmlFile);
                e.printStackTrace();
                
                // Fall back to default content if your FXML file fails to load
                return createDefaultContent(contentKey);
            }
        }
        
        // Return default content if no FXML file is specified
        return createDefaultContent(contentKey);
    }
    
    private Node createDefaultContent(String contentKey) {
        VBox defaultContent = new VBox(20);
        defaultContent.getStyleClass().add("default-content");
        defaultContent.setStyle("-fx-alignment: center; -fx-padding: 50px;");
        
        Label title = new Label("Content: " + contentKey.toUpperCase());
        title.getStyleClass().add("content-title");
        
        Label subtitle = new Label("This content is being developed...");
        subtitle.getStyleClass().add("content-subtitle");
        
        defaultContent.getChildren().addAll(title, subtitle);
        return defaultContent;
    }
    
    private void preloadContent(String contentKey) {
        // Load content in background thread
        Platform.runLater(() -> getOrCreateContent(contentKey));
    }
    
    private void displayContent(Node content) {
        activeContentContainer.getChildren().clear();
        activeContentContainer.getChildren().add(content);
        
        // Add fade-in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), content);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
    
    private void showLoadingOverlay(boolean show) {
        loadingOverlay.setVisible(show);
        
        if (show) {
            loadingIndicator.setProgress(-1); // Indeterminate
            FadeTransition fadeIn = new FadeTransition(Duration.millis(150), loadingOverlay);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        } else {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), loadingOverlay);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> loadingOverlay.setVisible(false));
            fadeOut.play();
        }
    }
    
    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
        dateTimeLabel.setText(now.format(formatter));
    }
    
    private void updateSystemStatus() {
        String[] statuses = {
            "System Ready", "Processing Orders", "Updating Inventory", 
            "All Systems Operational", "Syncing Data"
        };
        int randomIndex = (int) (Math.random() * statuses.length);
        statusLabel.setText(statuses[randomIndex]);
        
        // Update connection status
        connectionStatus.setText("●");
        connectionStatus.setStyle("-fx-text-fill: #27ae60;"); // Green
    }
    
    private void updateMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        double usagePercentage = (double) usedMemory / totalMemory * 100;
        memoryUsageLabel.setText(String.format("Memory: %.0f%%", usagePercentage));
    }
    
    // Add method to clear cache if needed (useful for development/testing)
    public void clearContentCache() {
        contentCache.clear();
    }
    
    // Add method to reload specific content
    public void reloadContent(String contentKey) {
        contentCache.remove(contentKey);
        if (getCurrentSelectedTab().equals(contentKey)) {
            loadContent(contentKey);
        }
    }
    
    // Helper method to get current selected tab
    private String getCurrentSelectedTab() {
        if (navTabs.getSelectedToggle() != null) {
            ToggleButton selectedBtn = (ToggleButton) navTabs.getSelectedToggle();
            return (String) selectedBtn.getUserData();
        }
        return "tables"; // default
    }
    
    public void cleanup() {
        if (clockTimeline != null) clockTimeline.stop();
        if (statusTimeline != null) statusTimeline.stop();
        if (memoryTimeline != null) memoryTimeline.stop();
        
        // Clear content cache
        contentCache.clear();
    }
}
