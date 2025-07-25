package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class OrderGroup {
    private String orderGroupId;
    private int tableId;
    private String tableNumber;
    private String productNames; // Comma-separated product names
    private String quantities;   // Comma-separated quantities
    private String productsWithQuantities; // Combined format for display
    private double totalPrice;
    private String status;
    private String paymentMethod;
    private LocalDateTime dateTime;
    private List<Order> orders; // Individual orders in this group
    private boolean stockValidated; // Flag to track if stock was validated
    private String stockWarnings; // Any stock-related warnings

    // Constructors
    public OrderGroup() {
        this.orders = new ArrayList<>();
        this.stockValidated = false;
    }

    public OrderGroup(String orderGroupId, int tableId, String tableNumber,
                      String productNames, String quantities, double totalPrice,
                      String status, String paymentMethod, LocalDateTime dateTime) {
        this.orderGroupId = orderGroupId;
        this.tableId = tableId;
        this.tableNumber = tableNumber;
        this.productNames = productNames;
        this.quantities = quantities;
        this.totalPrice = totalPrice;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.dateTime = dateTime;
        this.orders = new ArrayList<>(); // Initialize even if not passed
        this.stockValidated = false;

        // Generate combined products with quantities
        this.productsWithQuantities = generateProductsWithQuantities();
    }

    // Enhanced constructor with orders list
    public OrderGroup(String orderGroupId, int tableId, String tableNumber,
                      List<Order> orders, String status, String paymentMethod) {
        this.orderGroupId = orderGroupId;
        this.tableId = tableId;
        this.tableNumber = tableNumber;
        this.orders = orders != null ? orders : new ArrayList<>();
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.dateTime = LocalDateTime.now();
        this.stockValidated = false;

        // Calculate total price and generate product strings
        calculateTotalPrice();
        generateProductStrings();
        this.productsWithQuantities = generateProductsWithQuantities();
    }

    // Getters and Setters
    public String getOrderGroupId() { return orderGroupId; }
    public void setOrderGroupId(String orderGroupId) { this.orderGroupId = orderGroupId; }

    public int getTableId() { return tableId; }
    public void setTableId(int tableId) { this.tableId = tableId; }

    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }

    public String getProductNames() { return productNames; }
    public void setProductNames(String productNames) {
        this.productNames = productNames;
        this.productsWithQuantities = generateProductsWithQuantities();
    }

    public String getQuantities() { return quantities; }
    public void setQuantities(String quantities) {
        this.quantities = quantities;
        this.productsWithQuantities = generateProductsWithQuantities();
    }

    public String getProductsWithQuantities() {
        if (productsWithQuantities == null || productsWithQuantities.isEmpty()) {
            productsWithQuantities = generateProductsWithQuantities();
        }
        return productsWithQuantities;
    }
    public void setProductsWithQuantities(String productsWithQuantities) {
        this.productsWithQuantities = productsWithQuantities;
    }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) {
        this.orders = orders != null ? orders : new ArrayList<>();
        calculateTotalPrice();
        generateProductStrings();
        this.productsWithQuantities = generateProductsWithQuantities();
    }

    public boolean isStockValidated() { return stockValidated; }
    public void setStockValidated(boolean stockValidated) { this.stockValidated = stockValidated; }

    public String getStockWarnings() { return stockWarnings; }
    public void setStockWarnings(String stockWarnings) { this.stockWarnings = stockWarnings; }

    // Helper Methods

    /**
     * Generates the combined product names and quantities string for display
     */
    private String generateProductsWithQuantities() {
        if (orders != null && !orders.isEmpty()) {
            return orders.stream()
                    .map(order -> order.getProductName() + " (" + order.getQty() + ")")
                    .collect(Collectors.joining(", "));
        }
        // Fallback to comma-separated strings if orders list is empty/null
        if (productNames == null || quantities == null || productNames.isEmpty() || quantities.isEmpty()) {
            return "";
        }

        String[] products = productNames.split(",");
        String[] qtys = quantities.split(",");

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < products.length && i < qtys.length; i++) {
            if (i > 0) result.append(", ");
            result.append(products[i].trim()).append(" (").append(qtys[i].trim()).append(")");
        }
        return result.toString();
    }

    /**
     * Calculates total price from orders list
     */
    private void calculateTotalPrice() {
        if (orders == null || orders.isEmpty()) {
            this.totalPrice = 0.0;
            return;
        }

        this.totalPrice = orders.stream()
                .mapToDouble(order -> order.getUnitPrice() * order.getQty())
                .sum();
    }

    /**
     * Generates product names and quantities strings from orders list
     */
    private void generateProductStrings() {
        if (orders == null || orders.isEmpty()) {
            this.productNames = "";
            this.quantities = "";
            return;
        }

        StringBuilder productNamesBuilder = new StringBuilder();
        StringBuilder quantitiesBuilder = new StringBuilder();

        for (int i = 0; i < orders.size(); i++) {
            if (i > 0) {
                productNamesBuilder.append(", ");
                quantitiesBuilder.append(", ");
            }
            Order order = orders.get(i);
            productNamesBuilder.append(order.getProductName());
            quantitiesBuilder.append(order.getQty());
        }

        this.productNames = productNamesBuilder.toString();
        this.quantities = quantitiesBuilder.toString();
    }

    /**
     * Adds an order to this group
     */
    public void addOrder(Order order) {
        if (this.orders == null) {
            this.orders = new ArrayList<>();
        }
        this.orders.add(order);
        calculateTotalPrice();
        generateProductStrings();
        this.productsWithQuantities = generateProductsWithQuantities();
    }

    /**
     * Removes an order from this group
     */
    public void removeOrder(Order order) {
        if (this.orders != null) {
            this.orders.remove(order);
            calculateTotalPrice();
            generateProductStrings();
            this.productsWithQuantities = generateProductsWithQuantities();
        }
    }

    /**
     * Gets the number of items in this order group
     */
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    /**
     * Gets the total quantity of all items
     */
    public int getTotalQuantity() {
        if (orders == null || orders.isEmpty()) return 0;
        return orders.stream().mapToInt(Order::getQty).sum();
    }

    /**
     * Checks if this order group contains a specific product
     */
    public boolean containsProduct(int productId) {
        if (orders == null) return false;
        return orders.stream().anyMatch(order -> order.getProductId() == productId);
    }

    /**
     * Gets the quantity of a specific product in this order group
     */
    public int getProductQuantity(int productId) {
        if (orders == null) return 0;
        return orders.stream()
                .filter(order -> order.getProductId() == productId)
                .mapToInt(Order::getQty)
                .sum();
    }

    /**
     * Gets formatted date time string
     */
    public String getFormattedDateTime() {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    /**
     * Gets formatted date string
     */
    public String getFormattedDate() {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * Gets formatted time string
     */
    public String getFormattedTime() {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    /**
     * Checks if this order group is active (not served or cancelled)
     */
    public boolean isActive() {
        return status != null &&
                !status.equalsIgnoreCase("SERVED") &&
                !status.equalsIgnoreCase("CANCELLED");
    }

    /**
     * Checks if this order group is completed
     */
    public boolean isCompleted() {
        return "SERVED".equalsIgnoreCase(status);
    }

    /**
     * Checks if this order group is cancelled
     */
    public boolean isCancelled() {
        return "CANCELLED".equalsIgnoreCase(status);
    }

    /**
     * Checks if payment is completed
     */
    public boolean isPaymentCompleted() {
        return "DONE".equalsIgnoreCase(paymentMethod) ||
                "PAID".equalsIgnoreCase(paymentMethod);
    }

    /**
     * Gets status color for UI display
     */
    public String getStatusColor() {
        if (status == null) return "#6c757d"; // Gray for unknown

        switch (status.toUpperCase()) {
            case "PENDING":
                return "#ffc107"; // Yellow
            case "PREPARING":
                return "#007bff"; // Blue
            case "READY":
                return "#28a745"; // Green
            case "SERVED":
                return "#6c757d"; // Gray
            case "CANCELLED":
                return "#dc3545"; // Red
            default:
                return "#6c757d"; // Gray
        }
    }

    /**
     * Gets a summary string for this order group
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Order #").append(orderGroupId)
                .append(" - Table ").append(tableNumber)
                .append(" - ").append(getItemCount()).append(" items")
                .append(" - ").append(String.format("%.2f MMK", totalPrice))
                .append(" - ").append(status);
        return summary.toString();
    }

    /**
     * Creates a copy of this order group
     */
    public OrderGroup copy() {
        OrderGroup copy = new OrderGroup();
        copy.orderGroupId = this.orderGroupId;
        copy.tableId = this.tableId;
        copy.tableNumber = this.tableNumber;
        copy.productNames = this.productNames;
        copy.quantities = this.quantities;
        copy.productsWithQuantities = this.productsWithQuantities;
        copy.totalPrice = this.totalPrice;
        copy.status = this.status;
        copy.paymentMethod = this.paymentMethod;
        copy.dateTime = this.dateTime;
        copy.stockValidated = this.stockValidated;
        copy.stockWarnings = this.stockWarnings;

        // Deep copy orders list
        if (this.orders != null) {
            copy.orders = new ArrayList<>();
            for (Order order : this.orders) {
                // Assuming Order has a copy method or constructor
                // For simplicity, we'll just add the reference, but a true deep copy
                // would require Order to be Cloneable or have a copy constructor.
                copy.orders.add(order);
            }
        }

        return copy;
    }

    @Override
    public String toString() {
        return "OrderGroup{" +
                "orderGroupId='" + orderGroupId + '\'' +
                ", tableNumber='" + tableNumber + '\'' +
                ", itemCount=" + getItemCount() +
                ", totalPrice=" + totalPrice +
                ", status='" + status + '\'' +
                ", dateTime=" + dateTime +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        OrderGroup that = (OrderGroup) obj;
        return orderGroupId != null ? orderGroupId.equals(that.orderGroupId) : that.orderGroupId == null;
    }

    @Override
    public int hashCode() {
        return orderGroupId != null ? orderGroupId.hashCode() : 0;
    }
}
