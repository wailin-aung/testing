package service;

import dao.ProductDAO;
import dao.StockDAO;
import dao.RecipeDAO;
import dao.IngredientDAO;
import model.Product;
import model.Stock;
import model.Recipe;
import model.Ingredient;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductStockService {
    
    private ProductDAO productDAO;
    private StockDAO stockDAO;
    private RecipeDAO recipeDAO;
    private IngredientDAO ingredientDAO;
    
    public ProductStockService() {
        this.productDAO = new ProductDAO();
        this.stockDAO = new StockDAO();
        this.recipeDAO = new RecipeDAO();
        this.ingredientDAO = new IngredientDAO();
    }
    
    /**
     * Updates all product statuses based on stock availability
     */
    public void updateAllProductStatuses() throws SQLException {
        List<Product> products = productDAO.getAllProducts();
        
        for (Product product : products) {
            // Skip if product is already discontinued
            if ("DISCONTINUED".equals(product.getStatus())) {
                continue;
            }
            
            boolean isAvailable = checkProductAvailability(product);
            String newStatus = isAvailable ? "AVAILABLE" : "UNAVAILABLE";
            
            // Update only if status has changed
            if (!newStatus.equals(product.getStatus())) {
                product.setStatus(newStatus);
                productDAO.updateProduct(product);
                
                // Log the status change
                System.out.println("Product '" + product.getProductName() + 
                                 "' status updated to: " + newStatus);
            }
        }
    }
    
    /**
     * Updates a specific product's status based on stock availability
     */
    public void updateProductStatus(int productId) throws SQLException {
        Product product = productDAO.getProductById(productId);
        if (product == null || "DISCONTINUED".equals(product.getStatus())) {
            return;
        }
        
        boolean isAvailable = checkProductAvailability(product);
        String newStatus = isAvailable ? "AVAILABLE" : "UNAVAILABLE";
        
        if (!newStatus.equals(product.getStatus())) {
            product.setStatus(newStatus);
            productDAO.updateProduct(product);
            
            System.out.println("Product '" + product.getProductName() + 
                             "' status updated to: " + newStatus);
        }
    }
    
    /**   
     * Checks if a product can be made based on available ingredients
     */
    private boolean checkProductAvailability(Product product) throws SQLException {
        // Get the stock item for this product (assuming product name matches stock name)
        List<Stock> stocks = stockDAO.getAllStock();
        Stock productStock = null;
        
        for (Stock stock : stocks) {
            if (stock.getProductName().equalsIgnoreCase(product.getProductName())) {
                productStock = stock;
                break;
            }
        }
        
        if (productStock == null) {
            return false; // No stock item found for this product
        }
        
        // Get recipes for this stock item
        List<Recipe> recipes = recipeDAO.getRecipesByStockId(productStock.getId());
        
        if (recipes.isEmpty()) {
            return false; // No recipe required, assume available
        }
        
        // Check if all required ingredients are available
        for (Recipe recipe : recipes) {
            Ingredient ingredient = ingredientDAO.getIngredientById(recipe.getIngredientId());
            if (ingredient == null) {
                return false; // Required ingredient not found
            }
            
            if (ingredient.getQuantity() < recipe.getRequiredQuantity()) {
                return false; // Not enough of this ingredient
            }
        }
        
        return true; // All ingredients are available
    }
    
    /**
     * Gets detailed availability information for a product
     */
    public ProductAvailabilityInfo getProductAvailabilityInfo(int productId) throws SQLException {
        Product product = productDAO.getProductById(productId);
        if (product == null) {
            return null;
        }
        
        ProductAvailabilityInfo info = new ProductAvailabilityInfo();
        info.setProduct(product);
        info.setAvailable(checkProductAvailability(product));
        
        // Get stock item
        List<Stock> stocks = stockDAO.getAllStock();
        for (Stock stock : stocks) {
            if (stock.getProductName().equalsIgnoreCase(product.getProductName())) {
                info.setStock(stock);
                
                // Get recipe details
                List<Recipe> recipes = recipeDAO.getRecipesByStockId(stock.getId());
                for (Recipe recipe : recipes) {
                    Ingredient ingredient = ingredientDAO.getIngredientById(recipe.getIngredientId());
                    if (ingredient != null) {
                        IngredientAvailability ingredientAvail = new IngredientAvailability();
                        ingredientAvail.setIngredient(ingredient);
                        ingredientAvail.setRequiredQuantity(recipe.getRequiredQuantity());
                        ingredientAvail.setAvailableQuantity(ingredient.getQuantity());
                        ingredientAvail.setSufficient(ingredient.getQuantity() >= recipe.getRequiredQuantity());
                        
                        info.addIngredientAvailability(ingredientAvail);
                    }
                }
                break;
            }
        }
        
        return info;
    }
    
    // Inner classes for detailed availability information
    public static class ProductAvailabilityInfo {
        private Product product;
        private Stock stock;
        private boolean available;
        private List<IngredientAvailability> ingredientAvailabilities = new ArrayList<>();
        
        // Getters and setters
        public Product getProduct() { return product; }
        public void setProduct(Product product) { this.product = product; }
        
        public Stock getStock() { return stock; }
        public void setStock(Stock stock) { this.stock = stock; }
        
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
        
        public List<IngredientAvailability> getIngredientAvailabilities() { return ingredientAvailabilities; }
        public void addIngredientAvailability(IngredientAvailability availability) { 
            this.ingredientAvailabilities.add(availability); 
        }
    }
    
    public static class IngredientAvailability {
        private Ingredient ingredient;
        private double requiredQuantity;
        private double availableQuantity;
        private boolean sufficient;
        
        // Getters and setters
        public Ingredient getIngredient() { return ingredient; }
        public void setIngredient(Ingredient ingredient) { this.ingredient = ingredient; }
        
        public double getRequiredQuantity() { return requiredQuantity; }
        public void setRequiredQuantity(double requiredQuantity) { this.requiredQuantity = requiredQuantity; }
        
        public double getAvailableQuantity() { return availableQuantity; }
        public void setAvailableQuantity(double availableQuantity) { this.availableQuantity = availableQuantity; }
        
        public boolean isSufficient() { return sufficient; }
        public void setSufficient(boolean sufficient) { this.sufficient = sufficient; }
    }
}
