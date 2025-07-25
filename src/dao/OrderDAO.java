package dao;

import model.Order;
import model.OrderGroup;
import model.Recipe; // Import Recipe
import model.Ingredient; // Import Ingredient
import util.DatabaseConnection;
import service.ReportService; // Assuming ReportService is available
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    private final ReportService reportService;
    private final IngredientDAO ingredientDAO; // Need IngredientDAO
    private final RecipeDAO recipeDAO; // Need RecipeDAO

    public OrderDAO() {
        this.reportService = new ReportService();
        this.ingredientDAO = new IngredientDAO(); // Initialize
        this.recipeDAO = new RecipeDAO(); // Initialize
    }

    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = """
            SELECT o.*, p.Product_name, rt.Table_number
            FROM Orders o
            JOIN Product p ON o.Product_id = p.Product_id
            JOIN RestaurantTable rt ON o.Table_id = rt.Table_id
            ORDER BY o.Date_time DESC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("Order_id"));
                order.setTableId(rs.getInt("Table_id"));
                order.setProductId(rs.getInt("Product_id"));
                order.setQty(rs.getInt("Qty"));
                order.setDateTime(rs.getTimestamp("Date_time").toLocalDateTime());
                order.setStatus(rs.getString("Status"));
                order.setPaymentMethod(rs.getString("Payment_method"));
                order.setUnitPrice(rs.getDouble("unit_price"));
                order.setTotalPrice(rs.getDouble("total_price"));
                order.setProductName(rs.getString("Product_name"));
                order.setTableNumber(rs.getString("Table_number"));
                orders.add(order);
            }
        }
        return orders;
    }

    public Order getOrderById(int orderId) throws SQLException {
        String query = """
            SELECT o.*, p.Product_name, rt.Table_number
            FROM Orders o
            JOIN Product p ON o.Product_id = p.Product_id
            JOIN RestaurantTable rt ON o.Table_id = rt.Table_id
            WHERE o.Order_id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("Order_id"));
                    order.setTableId(rs.getInt("Table_id"));
                    order.setProductId(rs.getInt("Product_id"));
                    order.setQty(rs.getInt("Qty"));
                    order.setDateTime(rs.getTimestamp("Date_time").toLocalDateTime());
                    order.setStatus(rs.getString("Status"));
                    order.setPaymentMethod(rs.getString("Payment_method"));
                    order.setUnitPrice(rs.getDouble("unit_price"));
                    order.setTotalPrice(rs.getDouble("total_price"));
                    order.setProductName(rs.getString("Product_name"));
                    order.setTableNumber(rs.getString("Table_number"));
                    return order;
                }
            }
        }
        return null;
    }

    public boolean addOrder(Order order) throws SQLException {
        String query = "INSERT INTO Orders (Table_id, Product_id, Qty, Status, Payment_method, unit_price) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, order.getTableId());
            stmt.setInt(2, order.getProductId());
            stmt.setInt(3, order.getQty());
            stmt.setString(4, order.getStatus());
            stmt.setString(5, order.getPaymentMethod());
            stmt.setDouble(6, order.getUnitPrice());
            stmt.setDouble(7, order.getTotalPrice());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateOrder(Order order) throws SQLException {
        String query = "UPDATE Orders SET Table_id = ?, Product_id = ?, Qty = ?, Status = ?, Payment_method = ?, unit_price = ?, total_price = ? WHERE Order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, order.getTableId());
            stmt.setInt(2, order.getProductId());
            stmt.setInt(3, order.getQty());
            stmt.setString(4, order.getStatus());
            stmt.setString(5, order.getPaymentMethod());
            stmt.setDouble(6, order.getUnitPrice());
            stmt.setDouble(7, order.getTotalPrice());
            stmt.setInt(8, order.getOrderId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateOrderStatus(int orderId, String status) throws SQLException {
        String query = "UPDATE Orders SET Status = ? WHERE Order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateOrderGroupPaymentMethod(String orderGroupId, String paymentMethod) throws SQLException {
        String sql = "UPDATE orders SET payment_method = ? WHERE order_group_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, paymentMethod);
            pstmt.setString(2, orderGroupId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public boolean deleteOrder(int orderId) throws SQLException {
        String query = "DELETE FROM Orders WHERE Order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Order> getOrdersByTable(int tableId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = """
            SELECT o.*, p.Product_name, rt.Table_number
            FROM Orders o
            JOIN Product p ON o.Product_id = p.Product_id
            JOIN RestaurantTable rt ON o.Table_id = rt.Table_id
            WHERE o.Table_id = ?
            ORDER BY o.Date_time DESC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, tableId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("Order_id"));
                    order.setTableId(rs.getInt("Table_id"));
                    order.setProductId(rs.getInt("Product_id"));
                    order.setQty(rs.getInt("Qty"));
                    order.setDateTime(rs.getTimestamp("Date_time").toLocalDateTime());
                    order.setStatus(rs.getString("Status"));
                    order.setPaymentMethod(rs.getString("Payment_method"));
                    order.setUnitPrice(rs.getDouble("unit_price"));
                    order.setTotalPrice(rs.getDouble("total_price"));
                    order.setProductName(rs.getString("Product_name"));
                    order.setTableNumber(rs.getString("Table_number"));
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    public List<Order> getActiveOrdersByTable(int tableId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = """
            SELECT o.*, p.Product_name, rt.Table_number
            FROM Orders o
            JOIN Product p ON o.Product_id = p.Product_id
            JOIN RestaurantTable rt ON o.Table_id = rt.Table_id
            WHERE o.Table_id = ? AND o.Status IN ('PENDING', 'PREPARING', 'READY')
            ORDER BY o.Date_time DESC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, tableId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("Order_id"));
                    order.setTableId(rs.getInt("Table_id"));
                    order.setProductId(rs.getInt("Product_id"));
                    order.setQty(rs.getInt("Qty"));
                    order.setDateTime(rs.getTimestamp("Date_time").toLocalDateTime());
                    order.setStatus(rs.getString("Status"));
                    order.setPaymentMethod(rs.getString("Payment_method"));
                    order.setUnitPrice(rs.getDouble("unit_price"));
                    order.setTotalPrice(rs.getDouble("total_price"));
                    order.setProductName(rs.getString("Product_name"));
                    order.setTableNumber(rs.getString("Table_number"));
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    public List<Order> getOrdersByStatus(String status) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = """
            SELECT o.*, p.Product_name, rt.Table_number
            FROM Orders o
            JOIN Product p ON o.Product_id = p.Product_id
            JOIN RestaurantTable rt ON o.Table_id = rt.Table_id
            WHERE o.Status = ?
            ORDER BY o.Date_time DESC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("Order_id"));
                    order.setTableId(rs.getInt("Table_id"));
                    order.setProductId(rs.getInt("Product_id"));
                    order.setQty(rs.getInt("Qty"));
                    order.setDateTime(rs.getTimestamp("Date_time").toLocalDateTime());
                    order.setStatus(rs.getString("Status"));
                    order.setPaymentMethod(rs.getString("Payment_method"));
                    order.setUnitPrice(rs.getDouble("unit_price"));
                    order.setTotalPrice(rs.getDouble("total_price"));
                    order.setProductName(rs.getString("Product_name"));
                    order.setTableNumber(rs.getString("Table_number"));
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    public List<Order> getOrdersByDateRange(LocalDate fromDate, LocalDate toDate) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = """
            SELECT o.*, p.Product_name, rt.Table_number
            FROM Orders o
            JOIN Product p ON o.Product_id = p.Product_id
            JOIN RestaurantTable rt ON o.Table_id = rt.Table_id
            WHERE DATE(o.Date_time) BETWEEN ? AND ?
            ORDER BY o.Date_time DESC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(fromDate));
            stmt.setDate(2, Date.valueOf(toDate));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("Order_id"));
                    order.setTableId(rs.getInt("Table_id"));
                    order.setProductId(rs.getInt("Product_id"));
                    order.setQty(rs.getInt("Qty"));
                    order.setDateTime(rs.getTimestamp("Date_time").toLocalDateTime());
                    order.setStatus(rs.getString("Status"));
                    order.setPaymentMethod(rs.getString("Payment_method"));
                    order.setUnitPrice(rs.getDouble("unit_price"));
                    order.setTotalPrice(rs.getDouble("total_price"));
                    order.setProductName(rs.getString("Product_name"));
                    order.setTableNumber(rs.getString("Table_number"));
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    public List<Order> getOrdersByProduct(int productId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = """
            SELECT o.*, p.Product_name, rt.Table_number
            FROM Orders o
            JOIN Product p ON o.Product_id = p.Product_id
            JOIN RestaurantTable rt ON o.Table_id = rt.Table_id
            WHERE o.Product_id = ?
            ORDER BY o.Date_time DESC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("Order_id"));
                    order.setTableId(rs.getInt("Table_id"));
                    order.setProductId(rs.getInt("Product_id"));
                    order.setQty(rs.getInt("Qty"));
                    order.setDateTime(rs.getTimestamp("Date_time").toLocalDateTime());
                    order.setStatus(rs.getString("Status"));
                    order.setPaymentMethod(rs.getString("Payment_method"));
                    order.setUnitPrice(rs.getDouble("unit_price"));
                    order.setTotalPrice(rs.getDouble("total_price"));
                    order.setProductName(rs.getString("Product_name"));
                    order.setTableNumber(rs.getString("Table_number"));
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    public List<Order> getTodaysOrders() throws SQLException {
        return getOrdersByDateRange(LocalDate.now(), LocalDate.now());
    }

    public List<Order> getPendingOrders() throws SQLException {
        return getOrdersByStatus("PENDING");
    }

    public List<Order> getCompletedOrders() throws SQLException {
        return getOrdersByStatus("SERVED");
    }

    public double getTotalRevenueByDateRange(LocalDate fromDate, LocalDate toDate) throws SQLException {
        String query = """
            SELECT COALESCE(SUM(total_price), 0) as total_revenue
            FROM Orders
            WHERE DATE(Date_time) BETWEEN ? AND ?
            AND Status = 'SERVED'
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(fromDate));
            stmt.setDate(2, Date.valueOf(toDate));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_revenue");
                }
            }
        }
        return 0.0;
    }

    public double getTotalRevenueByTable(int tableId, LocalDate fromDate, LocalDate toDate) throws SQLException {
        String query = """
            SELECT COALESCE(SUM(total_price), 0) as total_revenue
            FROM Orders
            WHERE Table_id = ? AND DATE(Date_time) BETWEEN ? AND ?
            AND Status = 'SERVED'
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, tableId);
            stmt.setDate(2, Date.valueOf(fromDate));
            stmt.setDate(3, Date.valueOf(toDate));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_revenue");
                }
            }
        }
        return 0.0;
    }

    public int getOrderCountByTable(int tableId, LocalDate fromDate, LocalDate toDate) throws SQLException {
        String query = """
            SELECT COUNT(*) as order_count
            FROM Orders
            WHERE Table_id = ? AND DATE(Date_time) BETWEEN ? AND ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, tableId);
            stmt.setDate(2, Date.valueOf(fromDate));
            stmt.setDate(3, Date.valueOf(toDate));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("order_count");
                }
            }
        }
        return 0;
    }

    public LocalDateTime getLastOrderTimeByTable(int tableId) throws SQLException {
        String query = """
            SELECT MAX(Date_time) as last_order
            FROM Orders
            WHERE Table_id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, tableId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp timestamp = rs.getTimestamp("last_order");
                    return timestamp != null ? timestamp.toLocalDateTime() : null;
                }
            }
        }
        return null;
    }

    public List<Order> searchOrders(String searchTerm) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = """
            SELECT o.*, p.Product_name, rt.Table_number
            FROM Orders o
            JOIN Product p ON o.Product_id = p.Product_id
            JOIN RestaurantTable rt ON o.Table_id = rt.Table_id
            WHERE p.Product_name LIKE ? OR rt.Table_number LIKE ? OR o.Status LIKE ?
            ORDER BY o.Date_time DESC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("Order_id"));
                    order.setTableId(rs.getInt("Table_id"));
                    order.setProductId(rs.getInt("Product_id"));
                    order.setQty(rs.getInt("Qty"));
                    order.setDateTime(rs.getTimestamp("Date_time").toLocalDateTime());
                    order.setStatus(rs.getString("Status"));
                    order.setPaymentMethod(rs.getString("Payment_method"));
                    order.setUnitPrice(rs.getDouble("unit_price"));
                    order.setTotalPrice(rs.getDouble("total_price"));
                    order.setProductName(rs.getString("Product_name"));
                    order.setTableNumber(rs.getString("Table_number"));
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    // Statistics methods
    public int getTotalOrdersCount() throws SQLException {
        String query = "SELECT COUNT(*) as total_orders FROM Orders";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total_orders");
            }
        }
        return 0;
    }

    public int getTotalOrdersCountByStatus(String status) throws SQLException {
        String query = "SELECT COUNT(*) as total_orders FROM Orders WHERE Status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_orders");
                }
            }
        }
        return 0;
    }

    public double getAverageOrderValue() throws SQLException {
        String query = "SELECT AVG(total_price) as avg_order_value FROM Orders WHERE Status = 'SERVED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("avg_order_value");
            }
        }
        return 0.0;
    }

    public double getTotalRevenue() throws SQLException {
        String query = "SELECT COALESCE(SUM(total_price), 0) as total_revenue FROM Orders WHERE Status = 'SERVED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("total_revenue");
            }
        }
        return 0.0;
    }

    /**
     * Adds a group of orders to the database, performing stock validation and deduction
     * within a single transaction.
     *
     * @param orders The list of individual orders to be added.
     * @param orderGroupId The unique ID for this order group.
     * @return true if all orders were added and stock deducted successfully, false otherwise.
     * @throws SQLException If a database error occurs or stock is insufficient.
     */
    public boolean addOrderGroup(List<Order> orders, String orderGroupId) throws SQLException {
        String insertOrderQuery = "INSERT INTO Orders (Table_id, Product_id, Qty, Status, Payment_method, unit_price, order_group_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Step 1: Validate and Deduct Stock for each order
            for (Order order : orders) {
                // Get recipes for the product (stock) associated with this order
                // Assuming Product_id in Orders table corresponds to Stock.id
                List<Recipe> recipes = recipeDAO.getRecipesByStockId(order.getProductId());

                if (recipes.isEmpty()) {
                    throw new SQLException("Product '" + order.getProductName() + "' (ID: " + order.getProductId() + ") has no recipe defined. Cannot validate stock.");
                }

                // Check and deduct ingredients for each recipe component
                for (Recipe recipe : recipes) {
                    Ingredient ingredient = ingredientDAO.getIngredientById(recipe.getIngredientId());
                    if (ingredient == null) {
                        throw new SQLException("Ingredient with ID " + recipe.getIngredientId() + " not found for recipe of product " + order.getProductName());
                    }

                    double requiredQtyForOrder = recipe.getRequiredQuantity() * order.getQty();

                    if (ingredient.getQuantity() < requiredQtyForOrder) {
                        throw new SQLException("Insufficient stock for ingredient: " + ingredient.getName() + ". Required: " + requiredQtyForOrder + " " + ingredient.getUnit() + ", Available: " + ingredient.getQuantity() + " " + ingredient.getUnit());
                    }

                    // Deduct ingredient quantity
                    double newQuantity = ingredient.getQuantity() - requiredQtyForOrder;
                    ingredientDAO.updateIngredientQuantity(ingredient.getId(), newQuantity);

                    // Log ingredient deduction (already handled by updateIngredientQuantity)
                }
            }

            // Step 2: Insert Orders into the database
            try (PreparedStatement stmt = conn.prepareStatement(insertOrderQuery)) {
                for (Order order : orders) {
                    stmt.setInt(1, order.getTableId());
                    stmt.setInt(2, order.getProductId());
                    stmt.setInt(3, order.getQty());
                    stmt.setString(4, order.getStatus());
                    stmt.setString(5, order.getPaymentMethod());
                    stmt.setDouble(6, order.getUnitPrice());
                    stmt.setString(7, orderGroupId);
                    stmt.addBatch();
                }
                int[] results = stmt.executeBatch();
                conn.commit(); // Commit transaction

                // Check if all inserts were successful
                for (int result : results) {
                    if (result <= 0) {
                        conn.rollback(); // Rollback if any insert failed
                        return false;
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Rollback on any error
            }
            throw e; // Re-throw the exception
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true); // Reset auto-commit
                conn.close(); // Close connection
            }
        }
    }

    public List<OrderGroup> getAllOrderGroups() throws SQLException {
        List<OrderGroup> orderGroups = new ArrayList<>();
        String query = """
            SELECT
                o.order_group_id,
                o.Table_id,
                rt.Table_number,
                GROUP_CONCAT(p.Product_name ORDER BY o.Order_id SEPARATOR ', ') as product_names,
                GROUP_CONCAT(o.Qty ORDER BY o.Order_id SEPARATOR ', ') as quantities,
                SUM(o.total_price) as total_price,
                o.Status,
                o.Payment_method,
                MIN(o.Date_time) as date_time
            FROM Orders o
            JOIN Product p ON o.Product_id = p.Product_id
            JOIN RestaurantTable rt ON o.Table_id = rt.Table_id
            WHERE o.order_group_id IS NOT NULL
            GROUP BY o.order_group_id, o.Table_id, rt.Table_number, o.Status, o.Payment_method
            ORDER BY MIN(o.Date_time) DESC
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                OrderGroup orderGroup = new OrderGroup(
                    rs.getString("order_group_id"),
                    rs.getInt("Table_id"),
                    rs.getString("Table_number"),
                    rs.getString("product_names"),
                    rs.getString("quantities"),
                    rs.getDouble("total_price"),
                    rs.getString("Status"),
                    rs.getString("Payment_method"),
                    rs.getTimestamp("date_time").toLocalDateTime()
                );
                orderGroups.add(orderGroup);
            }
        }
        return orderGroups;
    }

    public boolean updateOrderGroupStatus(String orderGroupId, String status) throws SQLException {
        String query = "UPDATE Orders SET Status = ? WHERE order_group_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setString(2, orderGroupId);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Order> getOrdersByGroupId(String orderGroupId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = """
            SELECT o.*, p.Product_name, rt.Table_number
            FROM Orders o
            JOIN Product p ON o.Product_id = p.Product_id
            JOIN RestaurantTable rt ON o.Table_id = rt.Table_id
            WHERE o.order_group_id = ?
            ORDER BY o.Order_id
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, orderGroupId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("Order_id"));
                    order.setTableId(rs.getInt("Table_id"));
                    order.setProductId(rs.getInt("Product_id"));
                    order.setQty(rs.getInt("Qty"));
                    order.setDateTime(rs.getTimestamp("Date_time").toLocalDateTime());
                    order.setStatus(rs.getString("Status"));
                    order.setPaymentMethod(rs.getString("Payment_method"));
                    order.setUnitPrice(rs.getDouble("unit_price"));
                    order.setTotalPrice(rs.getDouble("total_price"));
                    order.setProductName(rs.getString("Product_name"));
                    order.setTableNumber(rs.getString("Table_number"));
                    orders.add(order);
                }
            }
        }
        return orders;
    }
}
