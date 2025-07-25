package dao;

import util.DatabaseConnection;
import model.Ingredient;
import service.ReportService; // Assuming ReportService is available
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IngredientDAO {
    private final ReportService reportService;

    public IngredientDAO() {
        this.reportService = new ReportService();
    }

    public void addIngredient(Ingredient ingredient) throws SQLException {
        String sql = "INSERT INTO ingredients (name, quantity, unit) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, ingredient.getName());
            pstmt.setDouble(2, ingredient.getQuantity());
            pstmt.setString(3, ingredient.getUnit());
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int ingredientId = generatedKeys.getInt(1);
                ingredient.setId(ingredientId);
                reportService.logIngredientAdd(ingredientId, ingredient.getName(), ingredient.getQuantity(), ingredient.getUnit());
            }
        }
    }

    public List<Ingredient> getAllIngredients() throws SQLException {
        List<Ingredient> ingredients = new ArrayList<>();
        String sql = "SELECT * FROM ingredients ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ingredients.add(new Ingredient(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("quantity"),
                    rs.getString("unit")
                ));
            }
        }
        return ingredients;
    }

    public void updateIngredient(Ingredient ingredient) throws SQLException {
        Ingredient oldIngredient = getIngredientById(ingredient.getId()); // Get old values for logging

        String sql = "UPDATE ingredients SET name = ?, quantity = ?, unit = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ingredient.getName());
            pstmt.setDouble(2, ingredient.getQuantity());
            pstmt.setString(3, ingredient.getUnit());
            pstmt.setInt(4, ingredient.getId());
            pstmt.executeUpdate();

            if (oldIngredient != null) {
                reportService.logIngredientUpdate(
                    ingredient.getId(),
                    oldIngredient.getName(),
                    oldIngredient.getQuantity(),
                    "UPDATE",
                    ingredient.getQuantity(),
                    "Ingredient details updated"
                );
            }
        }
    }

    public void deleteIngredient(int id) throws SQLException {
        Ingredient ingredient = getIngredientById(id); // Get details before deletion

        String sql = "DELETE FROM ingredients WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();

            if (ingredient != null) {
                reportService.logIngredientDelete(id, ingredient.getName(), ingredient.getQuantity(), ingredient.getUnit());
            }
        }
    }

    /**
     * Retrieves an ingredient by its ID.
     * @param id The ID of the ingredient.
     * @return The Ingredient object, or null if not found.
     * @throws SQLException If a database access error occurs.
     */
    public Ingredient getIngredientById(int id) throws SQLException {
        String sql = "SELECT * FROM ingredients WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Ingredient(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("quantity"),
                    rs.getString("unit")
                );
            }
        }
        return null;
    }

    /**
     * Updates the quantity of a specific ingredient.
     * Logs the change using ReportService.
     * @param id The ID of the ingredient to update.
     * @param newQuantity The new quantity for the ingredient.
     * @throws SQLException If a database access error occurs.
     */
    public void updateIngredientQuantity(int id, double newQuantity) throws SQLException {
        // Get old quantity for logging
        Ingredient oldIngredient = getIngredientById(id);
        double oldQuantity = oldIngredient != null ? oldIngredient.getQuantity() : 0.0;

        String sql = "UPDATE ingredients SET quantity = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newQuantity);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();

            // Log the quantity change
            if (oldIngredient != null) {
                reportService.logIngredientUpdate(
                    id,
                    oldIngredient.getName(),
                    oldQuantity,
                    (newQuantity > oldQuantity ? "ADD" : "DEDUCT"),
                    newQuantity,
                    "Quantity updated programmatically"
                );
            }
        }
    }
}
