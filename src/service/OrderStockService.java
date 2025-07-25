package service;

import dao.IngredientDAO;
import dao.RecipeDAO;
import dao.StockDAO;
import model.Ingredient;
import model.Recipe;
import model.Stock;

import java.sql.SQLException;
import java.util.List;

public class OrderStockService {
    
    private IngredientDAO ingredientDAO;
    private RecipeDAO recipeDAO;
    private StockDAO stockDAO;
    
    public OrderStockService() {
        this.ingredientDAO = new IngredientDAO();
        this.recipeDAO = new RecipeDAO();
        this.stockDAO = new StockDAO();
    }
    
    /**
     * Consumes ingredients when an order is placed
     * @param stockId The stock item ID that was ordered
     * @param quantity The quantity ordered
     * @return true if ingredients were successfully consumed, false otherwise
     */
    public boolean consumeIngredientsForOrder(int stockId, int quantity) throws SQLException {
        // Get recipes for this stock item
        List<Recipe> recipes = recipeDAO.getRecipesByStockId(stockId);
        
        if (recipes.isEmpty()) {
            // No recipe required, order can proceed
            return true;
        }
        
        // Check if we have enough ingredients first
        for (Recipe recipe : recipes) {
            Ingredient ingredient = ingredientDAO.getIngredientById(recipe.getIngredientId());
            if (ingredient == null) {
                throw new SQLException("Required ingredient not found: " + recipe.getIngredientId());
            }
            
            double requiredQuantity = recipe.getRequiredQuantity() * quantity;
            if (ingredient.getQuantity() < requiredQuantity) {
                return false; // Not enough ingredients
            }
        }
        
        // Consume ingredients
        for (Recipe recipe : recipes) {
            Ingredient ingredient = ingredientDAO.getIngredientById(recipe.getIngredientId());
            double requiredQuantity = recipe.getRequiredQuantity() * quantity;
            
            // Update ingredient quantity
            ingredient.setQuantity(ingredient.getQuantity() - requiredQuantity);
            ingredientDAO.updateIngredient(ingredient);
            
            System.out.println("Consumed " + requiredQuantity + " " + ingredient.getUnit() + 
                             " of " + ingredient.getName() + " for order");
        }
        
        return true;
    }
    
    /**
     * Checks if an order can be fulfilled based on available ingredients
     * @param stockId The stock item ID
     * @param quantity The quantity to check
     * @return true if order can be fulfilled, false otherwise
     */
    public boolean canFulfillOrder(int stockId, int quantity) throws SQLException {
        List<Recipe> recipes = recipeDAO.getRecipesByStockId(stockId);
        
        if (recipes.isEmpty()) {
            return true; // No recipe required
        }
        
        for (Recipe recipe : recipes) {
            Ingredient ingredient = ingredientDAO.getIngredientById(recipe.getIngredientId());
            if (ingredient == null) {
                return false;
            }
            
            double requiredQuantity = recipe.getRequiredQuantity() * quantity;
            if (ingredient.getQuantity() < requiredQuantity) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Gets the maximum quantity that can be ordered for a stock item
     * @param stockId The stock item ID
     * @return maximum quantity that can be ordered
     */
    public int getMaxOrderableQuantity(int stockId) throws SQLException {
        List<Recipe> recipes = recipeDAO.getRecipesByStockId(stockId);
        
        if (recipes.isEmpty()) {
            return Integer.MAX_VALUE; // No recipe limitation
        }
        
        int maxQuantity = Integer.MAX_VALUE;
        
        for (Recipe recipe : recipes) {
            Ingredient ingredient = ingredientDAO.getIngredientById(recipe.getIngredientId());
            if (ingredient == null) {
                return 0;
            }
            
            int possibleQuantity = (int) Math.floor(ingredient.getQuantity() / recipe.getRequiredQuantity());
            maxQuantity = Math.min(maxQuantity, possibleQuantity);
        }
        
        return Math.max(0, maxQuantity);
    }
    
    /**
     * Gets detailed information about what ingredients are needed for an order
     * @param stockId The stock item ID
     * @param quantity The quantity to check
     * @return List of ingredient requirements
     */
    public List<IngredientRequirement> getIngredientRequirements(int stockId, int quantity) throws SQLException {
        List<IngredientRequirement> requirements = new java.util.ArrayList<>();
        List<Recipe> recipes = recipeDAO.getRecipesByStockId(stockId);
        
        for (Recipe recipe : recipes) {
            Ingredient ingredient = ingredientDAO.getIngredientById(recipe.getIngredientId());
            if (ingredient != null) {
                double requiredQuantity = recipe.getRequiredQuantity() * quantity;
                boolean sufficient = ingredient.getQuantity() >= requiredQuantity;
                
                IngredientRequirement requirement = new IngredientRequirement(
                    ingredient, requiredQuantity, ingredient.getQuantity(), sufficient
                );
                requirements.add(requirement);
            }
        }
        
        return requirements;
    }
    
    /**
     * Restores ingredients when an order is cancelled
     * @param stockId The stock item ID
     * @param quantity The quantity to restore
     * @return true if ingredients were successfully restored
     */
    public boolean restoreIngredientsForCancelledOrder(int stockId, int quantity) throws SQLException {
        List<Recipe> recipes = recipeDAO.getRecipesByStockId(stockId);
        
        if (recipes.isEmpty()) {
            return true; // No ingredients to restore
        }
        
        // Restore ingredients
        for (Recipe recipe : recipes) {
            Ingredient ingredient = ingredientDAO.getIngredientById(recipe.getIngredientId());
            if (ingredient != null) {
                double restoredQuantity = recipe.getRequiredQuantity() * quantity;
                ingredient.setQuantity(ingredient.getQuantity() + restoredQuantity);
                ingredientDAO.updateIngredient(ingredient);
                
                System.out.println("Restored " + restoredQuantity + " " + ingredient.getUnit() + 
                                 " of " + ingredient.getName() + " from cancelled order");
            }
        }
        
        return true;
    }
    
    // Inner class for ingredient requirements
    public static class IngredientRequirement {
        private Ingredient ingredient;
        private double requiredQuantity;
        private double availableQuantity;
        private boolean sufficient;
        
        public IngredientRequirement(Ingredient ingredient, double requiredQuantity, 
                                   double availableQuantity, boolean sufficient) {
            this.ingredient = ingredient;
            this.requiredQuantity = requiredQuantity;
            this.availableQuantity = availableQuantity;
            this.sufficient = sufficient;
        }
        
        // Getters
        public Ingredient getIngredient() { return ingredient; }
        public double getRequiredQuantity() { return requiredQuantity; }
        public double getAvailableQuantity() { return availableQuantity; }
        public boolean isSufficient() { return sufficient; }
    }
}
