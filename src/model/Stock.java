package model;

public class Stock {
    private int id;
    private String productName;
    private String description;
    private int maxServings;

    public Stock() {}

    public Stock(String productName, String description) {
        this.productName = productName;
        this.description = description;
        this.maxServings = 0;
    }

    public Stock(int id, String productName, String description, int maxServings) {
        this.id = id;
        this.productName = productName;
        this.description = description;
        this.maxServings = maxServings;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getMaxServings() { return maxServings; }
    public void setMaxServings(int maxServings) { this.maxServings = maxServings; }

    @Override
    public String toString() {
        return productName + " - " + description +
               (maxServings > 0 ? " (" + maxServings + " servings)" : "");
    }
}
