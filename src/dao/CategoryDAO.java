package dao;

import model.Category;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    
    public List<Category> getAllCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT * FROM Category ORDER BY Category_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Category category = new Category(
                    rs.getInt("Category_id"),
                    rs.getString("Category_name")
                );
                categories.add(category);
            }
        }
        return categories;
    }
    
    public Category getCategoryById(int categoryId) throws SQLException {
        String query = "SELECT * FROM Category WHERE Category_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Category(
                        rs.getInt("Category_id"),
                        rs.getString("Category_name")
                    );
                }
            }
        }
        return null;
    }
    
    public boolean addCategory(Category category) throws SQLException {
        String query = "INSERT INTO Category (Category_name) VALUES (?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, category.getCategoryName());
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean updateCategory(Category category) throws SQLException {
        String query = "UPDATE Category SET Category_name = ? WHERE Category_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, category.getCategoryName());
            stmt.setInt(2, category.getCategoryId());
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean deleteCategory(int categoryId) throws SQLException {
        String query = "DELETE FROM Category WHERE Category_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, categoryId);
            return stmt.executeUpdate() > 0;
        }
    }
}
