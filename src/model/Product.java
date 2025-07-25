package model;

import javafx.beans.property.*;

public class Product {
    private final IntegerProperty productId;
    private final StringProperty productName;
    private final IntegerProperty categoryId;
    private final DoubleProperty productPrice;
    private final StringProperty status;
    
    public Product() {
        this.productId = new SimpleIntegerProperty();
        this.productName = new SimpleStringProperty();
        this.categoryId = new SimpleIntegerProperty();
        this.productPrice = new SimpleDoubleProperty();
        this.status = new SimpleStringProperty();
    }
    
    public Product(int productId, String productName, int categoryId, double productPrice, String status) {
        this();
        setProductId(productId);
        setProductName(productName);
        setCategoryId(categoryId);
        setProductPrice(productPrice);
        setStatus(status);
    }
    
    // Property getters
    public IntegerProperty productIdProperty() { return productId; }
    public StringProperty productNameProperty() { return productName; }
    public IntegerProperty categoryIdProperty() { return categoryId; }
    public DoubleProperty productPriceProperty() { return productPrice; }
    public StringProperty statusProperty() { return status; }
    
    // Getters
    public int getProductId() { return productId.get(); }
    public String getProductName() { return productName.get(); }
    public int getCategoryId() { return categoryId.get(); }
    public double getProductPrice() { return productPrice.get(); }
    public String getStatus() { return status.get(); }
    
    // Setters
    public void setProductId(int productId) { this.productId.set(productId); }
    public void setProductName(String productName) { this.productName.set(productName); }
    public void setCategoryId(int categoryId) { this.categoryId.set(categoryId); }
    public void setProductPrice(double productPrice) { this.productPrice.set(productPrice); }
    public void setStatus(String status) { this.status.set(status); }
    
    @Override
    public String toString() {
        return getProductName() + " - " + String.format("%.2f", getProductPrice()) +" mmk";
    }
}
