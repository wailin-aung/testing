package dao;

import model.Product;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    
    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM Product ORDER BY Product_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Product product = new Product(
                    rs.getInt("Product_id"),
                    rs.getString("Product_name"),
                    rs.getInt("Category_id"),
                    rs.getDouble("Product_price"),
                    rs.getString("Status")
                );
                products.add(product);
            }
        }
        return products;
    }
    
    public List<Product> getProductsByCategory(int categoryId) throws SQLException {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM Product WHERE Category_id = ? ORDER BY Product_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product(
                        rs.getInt("Product_id"),
                        rs.getString("Product_name"),
                        rs.getInt("Category_id"),
                        rs.getDouble("Product_price"),
                        rs.getString("Status")
                    );
                    products.add(product);
                }
            }
        }
        return products;
    }
    
    public boolean addProduct(Product product) throws SQLException {
        String query = "INSERT INTO Product (Product_name, Category_id, Product_price, Status) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, product.getProductName());
            stmt.setInt(2, product.getCategoryId());
            stmt.setDouble(3, product.getProductPrice());
            stmt.setString(4, product.getStatus());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean updateProduct(Product product) throws SQLException {
        String query = "UPDATE Product SET Product_name = ?, Category_id = ?, Product_price = ?, Status = ? WHERE Product_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, product.getProductName());
            stmt.setInt(2, product.getCategoryId());
            stmt.setDouble(3, product.getProductPrice());
            stmt.setString(4, product.getStatus());
            stmt.setInt(5, product.getProductId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean deleteProduct(int productId) throws SQLException {
        String query = "DELETE FROM Product WHERE Product_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public Product getProductById(int productId) throws SQLException {
        String sql = "SELECT * FROM product WHERE product_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, productId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Product product = new Product();
                    product.setProductId(rs.getInt("Product_id"));
                    product.setProductName(rs.getString("Product_name"));
                    product.setCategoryId(rs.getInt("Category_id"));
                    product.setProductPrice(rs.getDouble("Product_price"));
                    product.setStatus(rs.getString("Status"));
                    return product;
                }
            }
        }
        
        return null;
    }
    
    public List<Product> getProductsByStatus(String status) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM product WHERE status = ? ORDER BY product_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product();
                    product.setProductId(rs.getInt("Product_id"));
                    product.setProductName(rs.getString("Product_name"));
                    product.setCategoryId(rs.getInt("Category_id"));
                    product.setProductPrice(rs.getDouble("Product_price"));
                    product.setStatus(rs.getString("Status"));
                    products.add(product);
                }
            }
        }
        
        return products;
    }
}
