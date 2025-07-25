package dao;

import util.DatabaseConnection;
import model.Stock;
import service.ReportService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockDAO {
    private final ReportService reportService;
    
    public StockDAO() {
        this.reportService = new ReportService();
    }
    
    public void addStock(Stock stock) throws SQLException {
        String sql = "INSERT INTO stock (product_name, description, max_servings) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, stock.getProductName());
            pstmt.setString(2, stock.getDescription());
            pstmt.setInt(3, stock.getMaxServings());
            pstmt.executeUpdate();
            
            // Get generated ID
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int stockId = generatedKeys.getInt(1);
                stock.setId(stockId);
                
                // Log the action
                reportService.logStockAdd(stockId, stock.getProductName(), stock.getDescription());
            }
        }
    }
    
    public static List<Stock> getAllStock() throws SQLException {
        List<Stock> stockList = new ArrayList<>();
        String sql = "SELECT * FROM stock ORDER BY product_name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Stock stock = new Stock(
                    rs.getInt("id"),
                    rs.getString("product_name"),
                    rs.getString("description"),
                    rs.getInt("max_servings")
                );
                stockList.add(stock);
            }
        }
        return stockList;
    }
    
    public void updateStock(Stock stock) throws SQLException {
        // Get old values first
        Stock oldStock = getStockById(stock.getId());
        
        String sql = "UPDATE stock SET product_name = ?, description = ?, max_servings = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, stock.getProductName());
            pstmt.setString(2, stock.getDescription());
            pstmt.setInt(3, stock.getMaxServings());
            pstmt.setInt(4, stock.getId());
            pstmt.executeUpdate();
            
            // Log the action only if there's a significant change
            if (oldStock != null && 
                (!oldStock.getDescription().equals(stock.getDescription()) || 
                 oldStock.getMaxServings() != stock.getMaxServings())) {
                reportService.logStockUpdate(stock.getId(), stock.getProductName(),
                                           oldStock.getDescription(), oldStock.getMaxServings(),
                                           stock.getDescription(), stock.getMaxServings());
            }
        }
    }
    
    public void deleteStock(int id) throws SQLException {
        // Get stock details before deletion
        Stock stock = getStockById(id);
        
        String sql = "DELETE FROM stock WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            
            // Log the action
            if (stock != null) {
                reportService.logStockDelete(id, stock.getProductName(), stock.getDescription());
            }
        }
    }
    
    public Stock getStockById(int id) throws SQLException {
        String sql = "SELECT * FROM stock WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Stock(
                    rs.getInt("id"),
                    rs.getString("product_name"),
                    rs.getString("description"),
                    rs.getInt("max_servings")
                );
            }
        }
        return null;
    }
}