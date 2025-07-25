package dao;

import model.RestaurantTable;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RestaurantTableDAO {
    
    public static List<RestaurantTable> getAllTables() throws SQLException {
        List<RestaurantTable> tables = new ArrayList<>();
        String query = "SELECT * FROM RestaurantTable ORDER BY Table_number";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                RestaurantTable table = new RestaurantTable(
                    rs.getInt("Table_id"),
                    rs.getString("Table_number"),
                    rs.getInt("capacity"),
                    rs.getString("status"),
                    rs.getString("location")
                );
                tables.add(table);
            }
        }
        return tables;
    }
    
    public RestaurantTable getTableById(int tableId) throws SQLException {
        String query = "SELECT * FROM RestaurantTable WHERE Table_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, tableId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new RestaurantTable(
                        rs.getInt("Table_id"),
                        rs.getString("Table_number"),
                        rs.getInt("capacity"),
                        rs.getString("status"),
                        rs.getString("location")
                    );
                }
            }
        }
        return null;
    }
    
    public boolean addTable(RestaurantTable table) throws SQLException {
        String query = "INSERT INTO RestaurantTable (Table_number, capacity, status, location) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, table.getTableNumber());
            stmt.setInt(2, table.getCapacity());
            stmt.setString(3, table.getStatus());
            stmt.setString(4, table.getLocation());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean updateTable(RestaurantTable table) throws SQLException {
        String query = "UPDATE RestaurantTable SET Table_number = ?, capacity = ?, status = ?, location = ? WHERE Table_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, table.getTableNumber());
            stmt.setInt(2, table.getCapacity());
            stmt.setString(3, table.getStatus());
            stmt.setString(4, table.getLocation());
            stmt.setInt(5, table.getTableId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean updateTableStatus(int tableId, String status) throws SQLException {
        String query = "UPDATE RestaurantTable SET status = ? WHERE Table_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, tableId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean deleteTable(int tableId) throws SQLException {
        String query = "DELETE FROM RestaurantTable WHERE Table_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, tableId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public static List<RestaurantTable> getAvailableTables() throws SQLException {
        List<RestaurantTable> tables = new ArrayList<>();
        String query = "SELECT * FROM RestaurantTable WHERE status = 'AVAILABLE' ORDER BY Table_number";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                RestaurantTable table = new RestaurantTable(
                    rs.getInt("Table_id"),
                    rs.getString("Table_number"),
                    rs.getInt("capacity"),
                    rs.getString("status"),
                    rs.getString("location")
                );
                tables.add(table);
            }
        }
        return tables;
    }
    
    

    public static List<RestaurantTable> getOccupiedTables() throws SQLException {
        List<RestaurantTable> tables = new ArrayList<>();
        String query = "SELECT * FROM RestaurantTable WHERE status = 'OCCUPIED' ORDER BY Table_number";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                RestaurantTable table = new RestaurantTable(
                    rs.getInt("Table_id"),
                    rs.getString("Table_number"),
                    rs.getInt("capacity"),
                    rs.getString("status"),
                    rs.getString("location")
                );
                tables.add(table);
            }
        }
        return tables;
    }
}
