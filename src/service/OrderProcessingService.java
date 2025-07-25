package service;

import dao.OrderDAO;
import model.Order;
import model.OrderGroup;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID; // For generating order group ID

public class OrderProcessingService {
    private final OrderDAO orderDAO;
    private final StockAvailabilityService stockAvailabilityService; // To trigger recalculation
    private final ProductStockService productStockService; // To trigger product status update

    public OrderProcessingService() {
        this.orderDAO = new OrderDAO();
        // Assuming these services are properly initialized and available
        this.stockAvailabilityService = new StockAvailabilityService();
        this.productStockService = new ProductStockService();
    }

    /**
     * Processes a new order group, including stock validation and deduction.
     * This method orchestrates the database operations and subsequent stock/product status updates.
     *
     * @param orders The list of individual orders in this group.
     * @param tableId The ID of the table for this order group.
     * @param tableNumber The number of the table for this order group.
     * @return An OrderGroup object with updated status and potential stock warnings.
     * @throws SQLException If a database error occurs or stock is insufficient.
     */
    public OrderGroup processNewOrderGroup(List<Order> orders, int tableId, String tableNumber) throws SQLException {
        String orderGroupId = UUID.randomUUID().toString(); // Generate a unique ID for the order group

        // Set common properties for all orders in the group
        for (Order order : orders) {
            order.setTableId(tableId);
            // Default status and payment method if not already set
            if (order.getStatus() == null || order.getStatus().isEmpty()) {
                order.setStatus("PENDING");
            }
            if (order.getPaymentMethod() == null || order.getPaymentMethod().isEmpty()) {
                order.setPaymentMethod("CASH");
            }
            // Ensure total_price is calculated in Order model's setQty/setUnitPrice
            // If not, you might calculate it here: order.setTotalPrice(order.getQty() * order.getUnitPrice());
        }

        OrderGroup newOrderGroup = new OrderGroup(orderGroupId, tableId, tableNumber, orders, "PENDING", "CASH");
        newOrderGroup.setStockValidated(false); // Reset for new processing
        newOrderGroup.setStockWarnings(null);

        try {
            // The addOrderGroup method in OrderDAO now handles stock validation and deduction
            boolean success = orderDAO.addOrderGroup(orders, orderGroupId);

            if (success) {
                newOrderGroup.setStatus("PENDING"); // Or whatever initial status after successful creation
                newOrderGroup.setStockValidated(true);
                // Trigger stock recalculation and product status updates
                stockAvailabilityService.updateAllStockAvailability();
                productStockService.updateAllProductStatuses();
                System.out.println("Order Group " + orderGroupId + " processed successfully. Stock updated.");
            } else {
                // This path might not be hit if addOrderGroup throws an exception on failure
                newOrderGroup.setStatus("FAILED");
                newOrderGroup.setStockWarnings("Order failed due to an unknown issue during database insertion.");
                System.err.println("Order Group " + orderGroupId + " failed to process.");
            }
        } catch (SQLException e) {
            newOrderGroup.setStatus("FAILED");
            newOrderGroup.setStockValidated(false);
            newOrderGroup.setStockWarnings("Order failed: " + e.getMessage());
            System.err.println("Order Group " + orderGroupId + " failed due to stock or DB error: " + e.getMessage());
            throw e; // Re-throw the exception to inform the calling UI/controller
        }
        return newOrderGroup;
    }

    // You can add other order-related business logic methods here,
    // e.g., updateOrderGroupStatus, cancelOrderGroup, etc.
}
