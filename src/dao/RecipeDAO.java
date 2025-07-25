package dao;

import util.DatabaseConnection;
import model.Recipe;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecipeDAO {
    
    public void addRecipe(Recipe recipe) throws SQLException {
        String sql = "INSERT INTO recipes (stock_id, ingredient_id, required_quantity) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, recipe.getStockId());
            pstmt.setInt(2, recipe.getIngredientId());
            pstmt.setDouble(3, recipe.getRequiredQuantity());
            pstmt.executeUpdate();
        }
    }
    
    public List<Recipe> getRecipesByStockId(int stockId) throws SQLException {
        List<Recipe> recipes = new ArrayList<>();
        String sql = """
            SELECT r.*, i.name as ingredient_name, s.product_name as stock_name
            FROM recipes r
            JOIN ingredients i ON r.ingredient_id = i.id
            JOIN stock s ON r.stock_id = s.id
            WHERE r.stock_id = ?
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, stockId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Recipe recipe = new Recipe(
                    rs.getInt("id"),
                    rs.getInt("stock_id"),
                    rs.getInt("ingredient_id"),
                    rs.getDouble("required_quantity")
                );
                recipe.setIngredientName(rs.getString("ingredient_name"));
                recipe.setStockName(rs.getString("stock_name"));
                recipes.add(recipe);
            }
        }
        return recipes;
    }
    
    public List<Recipe> getAllRecipes() throws SQLException {
        List<Recipe> recipes = new ArrayList<>();
        String sql = """
            SELECT r.*, i.name as ingredient_name, s.product_name as stock_name
            FROM recipes r
            JOIN ingredients i ON r.ingredient_id = i.id
            JOIN stock s ON r.stock_id = s.id
            ORDER BY s.product_name, i.name
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Recipe recipe = new Recipe(
                    rs.getInt("id"),
                    rs.getInt("stock_id"),
                    rs.getInt("ingredient_id"),
                    rs.getDouble("required_quantity")
                );
                recipe.setIngredientName(rs.getString("ingredient_name"));
                recipe.setStockName(rs.getString("stock_name"));
                recipes.add(recipe);
            }
        }
        return recipes;
    }
    
    public void deleteRecipe(int id) throws SQLException {
        String sql = "DELETE FROM recipes WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
    
    public void deleteRecipesByStockId(int stockId) throws SQLException {
        String sql = "DELETE FROM recipes WHERE stock_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, stockId);
            pstmt.executeUpdate();
        }
    }
    
    public void updateRecipe(Recipe recipe) throws SQLException {
        String sql = "UPDATE recipes SET stock_id = ?, ingredient_id = ?, required_quantity = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, recipe.getStockId());
            pstmt.setInt(2, recipe.getIngredientId());
            pstmt.setDouble(3, recipe.getRequiredQuantity());
            pstmt.setInt(4, recipe.getId());
            pstmt.executeUpdate();
        }
    }
}