package model;

public class Recipe {
    private int id;
    private int stockId;
    private int ingredientId;
    private double requiredQuantity;
    private String ingredientName;
    private String stockName;

    public Recipe() {}

    public Recipe(int stockId, int ingredientId, double requiredQuantity) {
        this.stockId = stockId;
        this.ingredientId = ingredientId;
        this.requiredQuantity = requiredQuantity;
    }

    public Recipe(int id, int stockId, int ingredientId, double requiredQuantity) {
        this.id = id;
        this.stockId = stockId;
        this.ingredientId = ingredientId;
        this.requiredQuantity = requiredQuantity;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStockId() { return stockId; }
    public void setStockId(int stockId) { this.stockId = stockId; }

    public int getIngredientId() { return ingredientId; }
    public void setIngredientId(int ingredientId) { this.ingredientId = ingredientId; }

    public double getRequiredQuantity() { return requiredQuantity; }
    public void setRequiredQuantity(double requiredQuantity) { this.requiredQuantity = requiredQuantity; }

    public String getIngredientName() { return ingredientName; }
    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }

    public String getStockName() { return stockName; }
    public void setStockName(String stockName) { this.stockName = stockName; }

    @Override
    public String toString() {
        return ingredientName + " - " + requiredQuantity;
    }
}